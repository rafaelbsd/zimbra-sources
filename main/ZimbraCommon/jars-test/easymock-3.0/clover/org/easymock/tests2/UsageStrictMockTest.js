/*
 * ***** BEGIN LICENSE BLOCK *****
 * 
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2011 VMware, Inc.
 * 
 * The contents of this file are subject to the Zimbra Public License
 * Version 1.3 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * 
 * ***** END LICENSE BLOCK *****
 */
var clover = new Object();

// JSON: {classes : [{name, id, sl, el,  methods : [{sl, el}, ...]}, ...]}
clover.pageData = {"classes":[{"el":208,"id":4956,"methods":[{"el":40,"sc":5,"sl":34},{"el":47,"sc":5,"sl":42},{"el":60,"sc":5,"sl":49},{"el":76,"sc":5,"sl":62},{"el":92,"sc":5,"sl":78},{"el":139,"sc":5,"sl":94},{"el":177,"sc":5,"sl":141},{"el":207,"sc":5,"sl":179}],"name":"UsageStrictMockTest","sl":31}]}

// JSON: {test_ID : {"methods": [ID1, ID2, ID3...], "name" : "testXXX() void"}, ...};
clover.testTargets = {"test_1011":{"methods":[{"sl":94}],"name":"differentMethods","pass":true,"statements":[{"sl":97},{"sl":99},{"sl":100},{"sl":101},{"sl":102},{"sl":103},{"sl":104},{"sl":105},{"sl":107},{"sl":108},{"sl":109},{"sl":111},{"sl":112},{"sl":113},{"sl":115},{"sl":116},{"sl":121},{"sl":125},{"sl":127},{"sl":128},{"sl":129},{"sl":131},{"sl":132},{"sl":136}]},"test_150":{"methods":[{"sl":49}],"name":"unorderedCallsFailure","pass":true,"statements":[{"sl":51},{"sl":52},{"sl":53},{"sl":55},{"sl":57}]},"test_249":{"methods":[{"sl":179}],"name":"stubBehavior","pass":true,"statements":[{"sl":181},{"sl":183},{"sl":184},{"sl":185},{"sl":186},{"sl":188},{"sl":190},{"sl":191},{"sl":192},{"sl":193},{"sl":194},{"sl":196},{"sl":197},{"sl":198},{"sl":200},{"sl":201},{"sl":204}]},"test_265":{"methods":[{"sl":42}],"name":"orderedCallsSucces","pass":true,"statements":[{"sl":44},{"sl":45},{"sl":46}]},"test_266":{"methods":[{"sl":141}],"name":"range","pass":true,"statements":[{"sl":144},{"sl":146},{"sl":147},{"sl":148},{"sl":149},{"sl":150},{"sl":151},{"sl":152},{"sl":153},{"sl":155},{"sl":157},{"sl":158},{"sl":160},{"sl":161},{"sl":162},{"sl":164},{"sl":166},{"sl":167},{"sl":169},{"sl":170},{"sl":174}]},"test_603":{"methods":[{"sl":49}],"name":"unorderedCallsFailure","pass":true,"statements":[{"sl":51},{"sl":52},{"sl":53},{"sl":55},{"sl":57}]},"test_61":{"methods":[{"sl":62}],"name":"tooManyCallsFailure","pass":true,"statements":[{"sl":64},{"sl":65},{"sl":67},{"sl":68},{"sl":69},{"sl":71},{"sl":73}]},"test_645":{"methods":[{"sl":42}],"name":"orderedCallsSucces","pass":true,"statements":[{"sl":44},{"sl":45},{"sl":46}]},"test_655":{"methods":[{"sl":78}],"name":"tooFewCallsFailure","pass":true,"statements":[{"sl":80},{"sl":81},{"sl":82},{"sl":83},{"sl":85},{"sl":86},{"sl":89}]},"test_845":{"methods":[{"sl":94}],"name":"differentMethods","pass":true,"statements":[{"sl":97},{"sl":99},{"sl":100},{"sl":101},{"sl":102},{"sl":103},{"sl":104},{"sl":105},{"sl":107},{"sl":108},{"sl":109},{"sl":111},{"sl":112},{"sl":113},{"sl":115},{"sl":116},{"sl":121},{"sl":125},{"sl":127},{"sl":128},{"sl":129},{"sl":131},{"sl":132},{"sl":136}]},"test_858":{"methods":[{"sl":179}],"name":"stubBehavior","pass":true,"statements":[{"sl":181},{"sl":183},{"sl":184},{"sl":185},{"sl":186},{"sl":188},{"sl":190},{"sl":191},{"sl":192},{"sl":193},{"sl":194},{"sl":196},{"sl":197},{"sl":198},{"sl":200},{"sl":201},{"sl":204}]},"test_935":{"methods":[{"sl":62}],"name":"tooManyCallsFailure","pass":true,"statements":[{"sl":64},{"sl":65},{"sl":67},{"sl":68},{"sl":69},{"sl":71},{"sl":73}]},"test_983":{"methods":[{"sl":78}],"name":"tooFewCallsFailure","pass":true,"statements":[{"sl":80},{"sl":81},{"sl":82},{"sl":83},{"sl":85},{"sl":86},{"sl":89}]},"test_998":{"methods":[{"sl":141}],"name":"range","pass":true,"statements":[{"sl":144},{"sl":146},{"sl":147},{"sl":148},{"sl":149},{"sl":150},{"sl":151},{"sl":152},{"sl":153},{"sl":155},{"sl":157},{"sl":158},{"sl":160},{"sl":161},{"sl":162},{"sl":164},{"sl":166},{"sl":167},{"sl":169},{"sl":170},{"sl":174}]}}

// JSON: { lines : [{tests : [testid1, testid2, testid3, ...]}, ...]};
clover.srcFileLines = [[], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [265, 645], [], [265, 645], [265, 645], [265, 645], [], [], [603, 150], [], [603, 150], [603, 150], [603, 150], [], [603, 150], [], [603, 150], [], [], [], [], [61, 935], [], [61, 935], [61, 935], [], [61, 935], [61, 935], [61, 935], [], [61, 935], [], [61, 935], [], [], [], [], [655, 983], [], [655, 983], [655, 983], [655, 983], [655, 983], [], [655, 983], [655, 983], [], [], [655, 983], [], [], [], [], [845, 1011], [], [], [845, 1011], [], [845, 1011], [845, 1011], [845, 1011], [845, 1011], [845, 1011], [845, 1011], [845, 1011], [], [845, 1011], [845, 1011], [845, 1011], [], [845, 1011], [845, 1011], [845, 1011], [], [845, 1011], [845, 1011], [], [], [], [], [845, 1011], [], [], [], [845, 1011], [], [845, 1011], [845, 1011], [845, 1011], [], [845, 1011], [845, 1011], [], [], [], [845, 1011], [], [], [], [], [998, 266], [], [], [998, 266], [], [998, 266], [998, 266], [998, 266], [998, 266], [998, 266], [998, 266], [998, 266], [998, 266], [], [998, 266], [], [998, 266], [998, 266], [], [998, 266], [998, 266], [998, 266], [], [998, 266], [], [998, 266], [998, 266], [], [998, 266], [998, 266], [], [], [], [998, 266], [], [], [], [], [858, 249], [], [858, 249], [], [858, 249], [858, 249], [858, 249], [858, 249], [], [858, 249], [], [858, 249], [858, 249], [858, 249], [858, 249], [858, 249], [], [858, 249], [858, 249], [858, 249], [], [858, 249], [858, 249], [], [], [858, 249], [], [], [], []]
