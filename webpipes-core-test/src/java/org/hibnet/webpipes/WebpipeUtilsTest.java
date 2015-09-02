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

import static org.hibnet.webpipes.WebpipeUtils.addSuffix;
import static org.hibnet.webpipes.WebpipeUtils.getDotExtension;
import static org.hibnet.webpipes.WebpipeUtils.idOf;
import static org.hibnet.webpipes.WebpipeUtils.pathOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.util.Arrays;

import org.junit.Test;

public class WebpipeUtilsTest {

    @Test
    public void testAddSuffix() throws Exception {
        assertEquals("", addSuffix("", ""));
        assertEquals("file", addSuffix("file", ""));
        assertEquals("file.js", addSuffix("file.js", ""));
        assertEquals("-test", addSuffix("", "-test"));
        assertEquals("file-test", addSuffix("file", "-test"));
        assertEquals("file-test.js", addSuffix("file.js", "-test"));
    }

    @Test
    public void testGetDotExtension() throws Exception {
        assertEquals("", getDotExtension(""));
        assertEquals("", getDotExtension("file"));
        assertEquals(".", getDotExtension("file."));
        assertEquals(".extension", getDotExtension("file.extension"));
        assertEquals(".js", getDotExtension("file.min.js"));
    }

    @Test
    public void testPathOf() throws Exception {
        assertEquals("", pathOf(""));
        assertEquals("iu/\\!UI8$^`ç§'§", pathOf("iu/\\!UI8$^`ç§'§", "/some", "/path"));
        assertEquals("/some/path", pathOf(null, "/some", "path"));
        assertEquals("/some/path", pathOf(null, "/some", "/path"));
        assertEquals("/some/path", pathOf(null, "/some", "/path/"));
        assertEquals("/some/path", pathOf(null, "/some/", "/path/"));
        assertEquals("/some/path", pathOf(null, "/some/", "path/"));
        assertEquals("some/path", pathOf(null, "some/", "path/"));
    }

    @Test
    public void testIdOf() throws Exception {
        assertEquals(idOf(WebpipeUtilsTest.class), idOf(WebpipeUtilsTest.class));
        assertEquals(idOf(WebpipeUtilsTest.class, "test"), idOf(WebpipeUtilsTest.class, "test"));
        assertNotSame(idOf(WebpipeUtilsTest.class, "diff"), idOf(WebpipeUtilsTest.class, "test"));
        assertNotSame(idOf(WebpipeUtilsTest.class, "test", "diff"), idOf(WebpipeUtilsTest.class, "test"));
        assertEquals(idOf(WebpipeUtilsTest.class, "test", "diff"), idOf(WebpipeUtilsTest.class, "test", "diff"));
        assertNotSame(idOf(WebpipeUtilsTest.class, "test", "diff"), idOf(WebpipeUtilsTest.class, "diff", "test"));
        assertEquals(idOf(WebpipeUtilsTest.class, Arrays.asList("test", "diff")), idOf(WebpipeUtilsTest.class, Arrays.asList("test", "diff")));
        assertNotSame(idOf(WebpipeUtilsTest.class, Arrays.asList("test", "diff")), idOf(WebpipeUtilsTest.class, Arrays.asList("diff", "test")));
    }
}
