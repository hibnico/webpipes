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
package org.hibnet.webpipes.processor.handlebarsjs;

import static org.junit.Assert.assertTrue;

import org.hibnet.webpipes.processor.AbstractProcessorTest;
import org.hibnet.webpipes.resource.Resource;
import org.hibnet.webpipes.resource.StringResource;
import org.junit.Test;

public class HandlebarsJsProcessorTest extends AbstractProcessorTest {

    private HandlebarsJsProcessor processor = new HandlebarsJsProcessor();

    @Test
    public void testSimpleString() throws Exception {
        Resource r = new StringResource("test", "Hello {name}!");
        String result = processor.process(r).getMain();
        assertTrue(result.contains("return \"Hello {name}!\\n\";"));
    }

    @Test
    public void testFiles() throws Exception {
        testFiles(processor, ".handlebars", ".handlebars");
    }

}
