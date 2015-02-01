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

import java.io.File;

import org.hibnet.webpipes.resource.FileResource;
import org.hibnet.webpipes.resource.ResourceFactory;
import org.junit.Assert;

public class AbstractProcessorTest {

    protected ResourceFactory resourceFactory = new ResourceFactory();

    protected File commonTestDir = new File(AbstractProcessorTest.class.getResource("test").getPath());

    public File getClasspathDir(String path) throws Exception {
        return new File(getClass().getResource(path).getPath());
    }

    public void testFiles(WebpipeProcessor processor, String testExtension, String expectedExtension) throws Exception {
        testFiles(getClasspathDir("."), processor, testExtension, expectedExtension);
    }

    public void testFiles(File dir, WebpipeProcessor processor, String testExtension, String expectedExtension) throws Exception {
        File testDir = new File(dir, "test");
        File expectedDir = new File(dir, "expected");
        testFiles(testDir, expectedDir, processor, testExtension, expectedExtension);
    }

    public void testFiles(File testDir, File expectedDir, WebpipeProcessor processor, String testExtension, String expectedExtension)
            throws Exception {
        for (File testFile : testDir.listFiles()) {
            String baseName = testFile.getName().substring(0, testFile.getName().length() - testExtension.length());
            File expectedFile;
            if (expectedExtension == null) {
                expectedFile = testFile;
            } else {
                expectedFile = new File(expectedDir, baseName + expectedExtension);
                if (!expectedFile.exists()) {
                    continue;
                }
            }
            System.out.println("Testing " + testFile);
            FileResource testResource = new FileResource(testFile);
            String result = processor.process(testResource, testResource.getContent());
            String expected = new FileResource(expectedFile).getContent();

            result = result.replaceAll("\\t", "  ").replaceAll("(\\r|\\n)+", " ").trim();
            expected = expected.replaceAll("\\t", "  ").replaceAll("(\\r|\\n)+", " ").trim();

            Assert.assertEquals("Processing " + testFile.getName(), expected, result);
        }
    }

    public void testInvalidFiles(File testDir, WebpipeProcessor processor) throws Exception {
        for (File testFile : testDir.listFiles()) {
            FileResource testResource = new FileResource(testFile);
            try {
                processor.process(testResource, testResource.getContent());
                Assert.fail("Expected error on " + testFile.getName());
            } catch (Exception e) {
                // OK!
            }
        }
    }

}
