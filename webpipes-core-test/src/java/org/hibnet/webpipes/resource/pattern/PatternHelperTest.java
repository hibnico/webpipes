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
package org.hibnet.webpipes.resource.pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class PatternHelperTest {

    private PathMatcher pathMatcher = new AntPathMatcher();

    @Test
    public void testDetermineRootPath() throws Exception {
        assertEquals("/", PatternHelper.determineRootPath(pathMatcher, "/"));
        assertEquals("/", PatternHelper.determineRootPath(pathMatcher, "/*.js"));
        assertEquals("/test/", PatternHelper.determineRootPath(pathMatcher, "/test/*.js"));
        assertEquals("/test/", PatternHelper.determineRootPath(pathMatcher, "/test/*/test.js"));
        assertEquals("/", PatternHelper.determineRootPath(pathMatcher, "/**/test.js"));
    }

    @Test
    public void testFilesystemResources() throws Exception {
        assertTrue(PatternHelper.getFilesystemResources(pathMatcher, new File("."), "*.nonexisting").isEmpty());
        assertEquals(3, PatternHelper.getFilesystemResources(pathMatcher, new File("."), "*.xml").size());
        assertEquals(4, PatternHelper.getFilesystemResources(pathMatcher, new File("src/java/org/hibnet/webpipes/processor/test/"), "array*.js")
                .size());
        assertEquals(4, PatternHelper.getFilesystemResources(pathMatcher, new File("src/java/org/hibnet/"), "**/array*.js").size());
        assertEquals(4, PatternHelper.getFilesystemResources(pathMatcher, new File("src/java/org/hibnet/webpipes/processor/test/"), "**/array*.js")
                .size());
    }

    @Test
    public void testFileClasspathResources() throws Exception {
        assertTrue(PatternHelper.getClasspathResources(pathMatcher, null, "*.nonexisting").isEmpty());
        assertEquals(4, PatternHelper.getClasspathResources(pathMatcher, null, "org/hibnet/webpipes/processor/test/array*.js").size());
        assertEquals(4, PatternHelper.getClasspathResources(pathMatcher, null, "org/hibnet/**/array*.js").size());
        assertEquals(4, PatternHelper.getClasspathResources(pathMatcher, null, "org/hibnet/webpipes/processor/test/**/array*.js").size());
        assertEquals(4, PatternHelper.getClasspathResources(pathMatcher, null, "/org/hibnet/webpipes/processor/test/array*.js").size());
    }

    @Test
    public void testJarClasspathResources() throws Exception {
        assertEquals(2, PatternHelper.getClasspathResources(pathMatcher, null, "org/junit/After*").size());
        assertEquals(2, PatternHelper.getClasspathResources(pathMatcher, null, "org/*/After*").size());
        assertEquals(2, PatternHelper.getClasspathResources(pathMatcher, null, "org/**/After*").size());
        assertEquals(2, PatternHelper.getClasspathResources(pathMatcher, null, "org/junit/**/After*").size());
        assertEquals(2, PatternHelper.getClasspathResources(pathMatcher, null, "/org/junit/After*").size());
    }

}
