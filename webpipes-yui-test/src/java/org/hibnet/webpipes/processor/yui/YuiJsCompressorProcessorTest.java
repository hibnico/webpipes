/*
 *  Copyright 2015 WebPipes contributors
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hibnet.webpipes.processor.yui;

import org.hibnet.webpipes.processor.AbstractProcessorTest;
import org.junit.Test;

public class YuiJsCompressorProcessorTest extends AbstractProcessorTest {

    private YuiJsCompressorProcessor processor = new YuiJsCompressorProcessor();

    @Test
    public void testMunge() throws Exception {
        testFiles(commonTestFilesPattern, packageDir + "/expectedMunge/*.js", processor.createFactory(-1, true, false, false, false));
    }

    @Test
    public void testNomunge() throws Exception {
        testFiles(commonTestFilesPattern, packageDir + "/expectedNomunge/*.js", processor.createFactory(-1, false, false, false, false));
    }

}
