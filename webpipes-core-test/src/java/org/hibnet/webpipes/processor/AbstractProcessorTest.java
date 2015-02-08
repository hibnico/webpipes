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
package org.hibnet.webpipes.processor;

import java.util.List;

import org.hibnet.webpipes.WebpipeUtils;
import org.hibnet.webpipes.resource.Resource;
import org.hibnet.webpipes.resource.pattern.AntPathMatcher;
import org.hibnet.webpipes.resource.pattern.PatternHelper;
import org.junit.Assert;

public class AbstractProcessorTest {

    protected String commonTestFilesPattern = "/org/hibnet/webpipes/processor/test/*.js";

    protected String packageDir = WebpipeUtils.getPackageDir(getClass());

    protected void testFiles(ProcessingWebpipeFactory processor, String testExtension, String expectedExtension) throws Exception {
        testFiles(packageDir, processor, testExtension, expectedExtension);
    }

    protected void testFiles(String dir, ProcessingWebpipeFactory processor, String testExtension, String expectedExtension) throws Exception {
        testFiles(dir + "/test/*" + testExtension, dir + "/expected/*" + expectedExtension, processor);
    }

    private String getExtensionLessName(Resource resource) {
        String name = resource.getName();
        int i = name.lastIndexOf(".");
        if (i > 0) {
            return name.substring(0, i);
        }
        return name;
    }

    protected void testFiles(String testFilesPattern, String expectedFilesPattern, ProcessingWebpipeFactory processor) throws Exception {
        List<Resource> testFiles = PatternHelper.getClasspathResources(new AntPathMatcher(), null, testFilesPattern);
        if (testFiles.isEmpty()) {
            throw new RuntimeException("No test files");
        }
        List<Resource> expectedFiles = PatternHelper.getClasspathResources(new AntPathMatcher(), null, expectedFilesPattern);
        int iTestFile = 0;
        int iExpectedFile = 0;
        for (; iTestFile < testFiles.size() && iExpectedFile < expectedFiles.size(); iTestFile++) {
            Resource testFile = testFiles.get(iTestFile);
            Resource expectedFile = expectedFiles.get(iExpectedFile);
            if (!getExtensionLessName(testFile).equals(getExtensionLessName(expectedFile))) {
                continue;
            }
            iExpectedFile++;
            System.out.println("Testing " + testFile);
            String result = processor.createProcessingWebpipe(testFile).getContent();
            String expected = expectedFile.getContent();

            result = result.replaceAll("\\t", "  ").replaceAll("(\\r|\\n)+", " ").trim();
            expected = expected.replaceAll("\\t", "  ").replaceAll("(\\r|\\n)+", " ").trim();

            Assert.assertEquals("Processing " + testFile.getId(), expected, result);
        }
    }

    protected void testInvalidFiles(String testFilesPattern, ProcessingWebpipeFactory processor) throws Exception {
        List<Resource> testFiles = PatternHelper.getClasspathResources(new AntPathMatcher(), null, testFilesPattern);
        for (Resource testFile : testFiles) {
            try {
                processor.createProcessingWebpipe(testFile).getContent();
                Assert.fail("Expected error on " + testFile.getId());
            } catch (Exception e) {
                // OK!
            }
        }
    }

}
