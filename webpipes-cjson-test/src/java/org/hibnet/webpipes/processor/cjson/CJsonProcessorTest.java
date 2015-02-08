/*
 *  Copyright 2014-2015 WebPipes contributors
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
package org.hibnet.webpipes.processor.cjson;

import org.hibnet.webpipes.processor.AbstractProcessorTest;
import org.junit.Test;

public class CJsonProcessorTest extends AbstractProcessorTest {

    @Test
    public void testPack() throws Exception {
        CJsonProcessor processor = new CJsonProcessor(resourceFactory, true);
        testFiles(packageDir + "/test/*.js", packageDir + "/pack/*.js", processor);
    }

    @Test
    public void testUnpack() throws Exception {
        CJsonProcessor processor = new CJsonProcessor(resourceFactory, false);
        testFiles(packageDir + "/pack/*.js", packageDir + "/unpack/*.js", processor);
    }

}
