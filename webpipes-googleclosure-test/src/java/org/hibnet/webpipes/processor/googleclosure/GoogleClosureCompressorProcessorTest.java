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
package org.hibnet.webpipes.processor.googleclosure;

import org.hibnet.webpipes.processor.AbstractProcessorTest;
import org.junit.Test;

import com.google.javascript.jscomp.CompilationLevel;

public class GoogleClosureCompressorProcessorTest extends AbstractProcessorTest {

    private GoogleClosureCompressorProcessor processor = new GoogleClosureCompressorProcessor();

    @Test
    public void testWhiteSpaceOnly() throws Exception {
        testFiles(commonTestFilesPattern, packageDir + "/expectedWhitespaceOnly/*.js", processor.createFactory(CompilationLevel.WHITESPACE_ONLY));
    }

    @Test
    public void testSimpleOptimization() throws Exception {
        testFiles(commonTestFilesPattern, packageDir + "/expectedSimple/*.js", processor.createFactory(CompilationLevel.SIMPLE_OPTIMIZATIONS));
    }

    @Test
    public void testAdvancedOptimization() throws Exception {
        testFiles(commonTestFilesPattern, packageDir + "/expectedAdvanced/*.js", processor.createFactory(CompilationLevel.ADVANCED_OPTIMIZATIONS));
    }

}
