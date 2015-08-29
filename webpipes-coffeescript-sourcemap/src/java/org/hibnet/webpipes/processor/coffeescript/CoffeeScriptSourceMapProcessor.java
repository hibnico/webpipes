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
package org.hibnet.webpipes.processor.coffeescript;

import org.hibnet.webpipes.Webpipe;
import org.hibnet.webpipes.WebpipeOutput;
import org.hibnet.webpipes.processor.ProcessingWebpipe;
import org.hibnet.webpipes.processor.ProcessingWebpipeFactory;

/**
 * Uses coffee script library loaded from the webjar to compile to javascript code.
 */
public class CoffeeScriptSourceMapProcessor {

    private CoffeeScriptSourceMapRunner coffeeScriptRunner;

    public CoffeeScriptSourceMapProcessor() {
        this(new CoffeeScriptSourceMapRunner());
    }

    public CoffeeScriptSourceMapProcessor(CoffeeScriptSourceMapRunner coffeeScriptRunner) {
        this.coffeeScriptRunner = coffeeScriptRunner;
    }

    private final class CoffeeScriptWebpipe extends ProcessingWebpipe {

        private String[] options;

        private CoffeeScriptWebpipe(Webpipe webpipe, String[] options) {
            super(webpipe);
            this.options = options;
        }

        @Override
        protected WebpipeOutput fetchOutput() throws Exception {
            return new WebpipeOutput(coffeeScriptRunner.run(webpipe, options));
        }
    }

    public Webpipe createProcessingWebpipe(Webpipe source, String[] options) {
        return new CoffeeScriptWebpipe(source, options);
    }

    public ProcessingWebpipeFactory createFactory(final String[] options) {
        return new ProcessingWebpipeFactory() {
            @Override
            public Webpipe createProcessingWebpipe(Webpipe source) {
                return new CoffeeScriptWebpipe(source, options);
            }
        };
    }

}
