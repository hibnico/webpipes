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
package org.hibnet.webpipes.processor.rubysasscss;

import static org.junit.Assert.fail;

import org.hibnet.webpipes.processor.AbstractProcessorTest;
import org.hibnet.webpipes.processor.sass.RubySassCssProcessor;
import org.hibnet.webpipes.resource.Resource;
import org.hibnet.webpipes.resource.StringResource;
import org.junit.Test;

public class TestRubySassCssProcessor extends AbstractProcessorTest {

    private RubySassCssProcessor processor = new RubySassCssProcessor();

    @Test
    public void testFiles() throws Exception {
        testFiles(processor, ".css", ".css");
    }

    @Test
    public void shouldFailWhenInvalidSassCssIsProcessed() throws Exception {
        testInvalidFiles(getClasspathDir("invalid"), processor);
    }

    @Test
    public void shouldSucceedAfterAFailure() throws Exception {
        try {
            Resource r = new StringResource("$base= #f938ab;");
            processor.process(r, r.getContent());
            fail("Should have failed");
        } catch (final Exception e) {

        }
        String sass = ".valid {color: red}  @mixin rounded($side, $radius: 10px) { border-#{$side}-radius: $radius; -moz-border-radius-#{$side}: $radius; -webkit-border-#{$side}-radius: $radius;}#navbar li { @include rounded(top); }";
        Resource r = new StringResource(sass);
        processor.process(r, r.getContent());
    }

}
