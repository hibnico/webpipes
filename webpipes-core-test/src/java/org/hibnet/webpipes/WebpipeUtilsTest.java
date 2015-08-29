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
package org.hibnet.webpipes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class WebpipeUtilsTest {

    @Test
    public void testAddSuffix() throws Exception {
        assertEquals("", WebpipeUtils.addSuffix("", ""));
        assertEquals("file", WebpipeUtils.addSuffix("file", ""));
        assertEquals("file.js", WebpipeUtils.addSuffix("file.js", ""));
        assertEquals("-test", WebpipeUtils.addSuffix("", "-test"));
        assertEquals("file-test", WebpipeUtils.addSuffix("file", "-test"));
        assertEquals("file-test.js", WebpipeUtils.addSuffix("file.js", "-test"));
    }

    @Test
    public void testGetDotExtension() throws Exception {
        assertEquals("", WebpipeUtils.getDotExtension(""));
        assertEquals("", WebpipeUtils.getDotExtension("file"));
        assertEquals(".", WebpipeUtils.getDotExtension("file."));
        assertEquals(".extension", WebpipeUtils.getDotExtension("file.extension"));
        assertEquals(".js", WebpipeUtils.getDotExtension("file.min.js"));
    }
}
