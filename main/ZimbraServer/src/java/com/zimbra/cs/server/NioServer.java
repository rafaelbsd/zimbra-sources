/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2007, 2008, 2009, 2010, 2011, 2013 VMware, Inc.
 * 
 * The contents of this file are subject to the Zimbra Public License
 * Version 1.3 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * ***** END LICENSE BLOCK *****
 */

package com.zimbra.cs.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.security.KeyStore;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.SimpleIoProcessorPool;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.ssl.SslFilter;
import org.apache.mina.transport.socket.nio.NioProcessor;
import org.apache.mina.transport.socket.nio.NioSession;
import org.apache.mina.transport.socket.nio.ZimbraSocketAcceptor;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.NetUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.util.Zimbra;

/**
 * Base class for MINA-based IMAP/POP3/LMTP servers. Handles creation of new NIO request and connection handler
 * instances.
 */
public abstract class NioServer implements Server {
    private static final Multimap<Class<? extends NioServer>, IoFilter> FILTERS = ArrayListMultimap.create();
    protected static final ProtocolEncoder DEFAULT_ENCODER = new DefaultEncoder();
    protected final ExecutorFilter executorFilter;
    protected final ZimbraSocketAcceptor acceptor;
    protected final ServerConfig config;

    private static SSLContext sslContext;
    private static String[] mSslEnabledCipherSuites;

    // There is one IoProcessor pool shared by all protocol handlers
    private static final IoProcessor<NioSession> IO_PROCESSOR_POOL =
        new SimpleIoProcessorPool<NioSession>(NioProcessor.class, Executors.newCachedThreadPool(
                new ThreadFactoryBuilder().setNameFormat("NioProcessor-%d").build()));

    /**
     * Extensions may add a custom {@link IoFilter} to the filter chain. Must call before the server starts.
     *
     * @param server {@link NioServer} implementation to which the filter is added
     * @param filter a custom filter
     */
    public static void addFilter(Class<? extends NioServer> server, IoFilter filter) {
        FILTERS.put(server, filter);
    }

    private static synchronized SSLContext getSSLContext() {
        if (sslContext == null) {
            try {
                sslContext = initSSLContext();
            } catch (Exception e) {
                Zimbra.halt("exception initializing SSL context", e);
            }
        }
        return sslContext;
    }

    private static SSLContext initSSLContext() throws Exception {
        FileInputStream fis = null;
        try {
	        KeyStore ks = KeyStore.getInstance("JKS");
	        char[] pass = LC.mailboxd_keystore_password.value().toCharArray();
	        fis = new FileInputStream(LC.mailboxd_keystore.value());
	        ks.load(fis, pass);
	        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
	        kmf.init(ks, pass);
	        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
	        tmf.init(ks);
	        SSLContext context = SSLContext.getInstance("TLS");
	        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
	        return context;
        } finally {
            ByteUtil.closeStream(fis);
        }
    }

    /**
     * Our cipher config attribute zimbraSSLExcludeCipherSuites specifies a list of ciphers that should be
     * disabled instead of enabled.  This is because we want the same attribute to control all SSL protocols
     * running on mailbox servers.  For https, Jetty configuration only supports an excluded list.
     * Therefore we adapted the same scheme for zimbraSSLExcludeCipherSuites, which is written to jetty.xml
     * by config rewrite, and will be used for protocols (imaps/pop3s) handled by Zimbra code.
     *
     * For MINA based servers/handlers, NioTcpServer uses SSLFilter for SSL communication.  SSLFilter wraps
     * an SSLEngine that actually does all the work.  SSLFilter.setEnabledCipherSuites() sets the list of
     * cipher suites to be enabled when the underlying SSLEngine is initialized.  Since we only have an
     * excluded list, we need to exclude those from the ciphers suites which are currently enabled for use
     * on a engine.
     *
     * Since we do not directly interact with a SSLEngine while sessions are handled,  and there is
     * no SSLFilter API to alter the SSLEngine it wraps, we workaround this by doing the following:
     *   - create a dummy SSLEngine from the same SSLContext instance that will be used for all SSL communication.
     *   - get the enabled ciphers from SSLEngine.getEnabledCipherSuites()
     *   - exclude the ciphers we need to exclude from the enabled ciphers, so we now have a net enabled ciphers
     *     list.
     * The above is only done once and we keep a singleton of this cipher list.  We then can pass it to
     * SSLFilter.setEnabledCipherSuites() for SSL and StartTLS session.
     *
     * @param sslCtxt ssl context
     * @param serverConfig server config
     * @return array of enabled ciphers, or empty array (note, not null) if all default ciphers should be enabled.
     *         (i.e. we do not need to alter the default ciphers)
     */
    private static synchronized String[] getSSLEnabledCiphers(SSLContext sslCtxt, ServerConfig serverConfig) {
        if (mSslEnabledCipherSuites == null) {
            try {
                String[] excludeCiphers = serverConfig.getSslExcludedCiphers();

                if (excludeCiphers != null && excludeCiphers.length > 0) {
                    // create a SSLEngine to get the ciphers enabled for the engine
                    SSLEngine sslEng = sslCtxt.createSSLEngine();
                    String[] enabledCiphers = sslEng.getEnabledCipherSuites();
                    mSslEnabledCipherSuites = NetUtil.computeEnabledCipherSuites(enabledCiphers, excludeCiphers);
                }

                /*
                 * if null, it means we do not need to alter the ciphers - either excluded cipher is not configures,
                 * or the original SSLEngine default is null/empty (can this happen?).
                 *
                 * set it to empty array to indicate that mSslEnabledCipherSuites has been initialized.
                 */
                if (mSslEnabledCipherSuites == null)
                    mSslEnabledCipherSuites = new String[0];

            } catch (Exception e) {
                Zimbra.halt("exception initializing SSL enabled ciphers", e);
            }
        }
        return mSslEnabledCipherSuites;
    }

    public SslFilter newSSLFilter() {
        SSLContext sslCtxt = getSSLContext();
        SslFilter sslFilter = new SslFilter(sslCtxt);
        String[] enabledCiphers = getSSLEnabledCiphers(sslCtxt, config);
        if (enabledCiphers.length > 0)
            sslFilter.setEnabledCipherSuites(enabledCiphers);
        return sslFilter;
    }

    /**
     * Creates a new server for the specified configuration.
     *
     * @param config the ServerConfig for the server
     * @throws ServiceException if a ServiceException occured
     */
    protected NioServer(ServerConfig config) throws ServiceException {
        this.config = config;
        acceptor = new ZimbraSocketAcceptor(config.getServerSocketChannel(), IO_PROCESSOR_POOL);
        executorFilter = new ExecutorFilter(1, config.getMaxThreads(),
                config.getThreadKeepAliveTime(), TimeUnit.SECONDS,
                new ThreadFactoryBuilder().setNameFormat(getName() + "-%d").build());
    }

    /**
     * Returns the configuration for this server.
     *
     * @return the ServerConfig for this server
     */
    @Override
    public ServerConfig getConfig() {
        return config;
    }

    /**
     * Starts the server. Binds the server port and starts the connection
     * handler. Optionally adds an SSLFilter if ssl is enabled.
     *
     * @throws IOException if an I/O error occured while starting the server
     */
    @Override
    public void start() {
        ServerConfig sc = getConfig();
        DefaultIoFilterChainBuilder fc = acceptor.getFilterChain();
        if (sc.isSslEnabled()) {
            fc.addFirst("ssl", newSSLFilter());
        }
        fc.addLast("codec", new ProtocolCodecFilter(getProtocolCodecFactory()));
        fc.addLast("executer", executorFilter);
        for (IoFilter filter : FILTERS.get(getClass())) { // insert custom filters
            fc.addLast(filter.getClass().getName(), filter);
        }
        fc.addLast("logger", new NioLoggingFilter(this, false));
        acceptor.getSessionConfig().setBothIdleTime(sc.getMaxIdleTime());
        acceptor.getSessionConfig().setWriteTimeout(sc.getWriteTimeout());
        acceptor.setHandler(new NioHandlerDispatcher(this));
        try {
            acceptor.bind();
        } catch (Throwable e) {
            Zimbra.halt(getName() + " failed to start", e);
        }
        getLog().info("Starting %s on %s", getName(), acceptor.getLocalAddress());
    }

    /**
     * Shuts down the server. Waits up to 'graceSecs' seconds for the server to stop, otherwise the server is forced to
     * shut down.
     *
     * @param timeout number of seconds to wait before forced shutdown
     */
    @Override
    public void stop(int timeout) {
        getLog().info("Initiating shutdown");
        // Would prefer to unbind first then cleanly close active connections, but mina unbind seems to automatically
        // close the active sessions so we must close connections then unbind, which does expose us to a potential race
        // condition.
        closeSessions();
        acceptor.unbind();
        ExecutorService executor = (ExecutorService) executorFilter.getExecutor();
        executor.shutdown();
        try {
            executor.awaitTermination(timeout, TimeUnit.SECONDS); // Wait for handlers to cleanly terminate
        } catch (InterruptedException ignore) {
        }
        executor.shutdownNow(); // Force shutdown after grace period has expired
    }

    @Override
    public void stop() {
        stop(getConfig().getShutdownTimeout());
    }

    private void closeSessions() {
        for (IoSession session : getSessions().values()) {
            getLog().info("Closing session = " + session);
            NioHandler handler = NioHandlerDispatcher.getHandler(session);
            if (handler != null) {
                try {
                    handler.dropConnection();
                } catch (IOException ignore) {
                }
            }
        }
    }

    public Map<Long, IoSession> getSessions() {
        return acceptor.getManagedSessions();
    }

    /**
     * Creates a new handler for handling requests received by the specified NIO session (connection).
     *
     * @param session the I/O session (connection) being opened
     * @return the {@link NioHandler} for handling requests
     */
    protected abstract NioHandler createHandler(NioConnection conn);

    protected abstract ProtocolCodecFactory getProtocolCodecFactory();

    protected void registerMBean(String type) {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            mbs.registerMBean(new NioServerStats(this), new ObjectName("ZimbraCollaborationSuite:type=" + type));
        } catch (Exception e) {
            getLog().warn("Unable to register NioServerStats mbean", e);
        }
    }

    public Log getLog() {
        return getConfig().getLog();
    }

    private static final class DefaultEncoder extends ProtocolEncoderAdapter {
        @Override
        public void encode(IoSession session, Object msg, ProtocolEncoderOutput out) {
            if (msg instanceof IoBuffer) {
                IoBuffer buf = (IoBuffer) msg;
                out.write(buf);
            }
        }
    };

    /**
     * Returns the number of connections currently established.
     */
    protected int getNumConnections() {
        return acceptor.getManagedSessionCount();
    }

    /**
     * Returns the number of threads in the thread pool.
     */
    protected int getNumThreads() {
        Executor ex = executorFilter.getExecutor();
        if (!(ex instanceof ThreadPoolExecutor)) {
            ZimbraLog.perf.debug("Unexpected Executor type %s.  NioServer.getNumThreads() returning 0.",
                ex.getClass().getName());
            return 0;
        }
        return ((ThreadPoolExecutor) ex).getPoolSize();
    }
}
