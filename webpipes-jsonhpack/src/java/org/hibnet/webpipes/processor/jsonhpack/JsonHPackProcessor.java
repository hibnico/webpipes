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
package org.hibnet.webpipes.processor.jsonhpack;

import org.hibnet.webpipes.Webpipe;
import org.hibnet.webpipes.WebpipeOutput;
import org.hibnet.webpipes.processor.ProcessingWebpipe;
import org.hibnet.webpipes.processor.ProcessingWebpipeFactory;

/**
 * A processor using json.hpack compression algorithm: @see https://github.com/WebReflection/json.hpack
 */
public class JsonHPackProcessor {

    private JsonHPackRunner jsonHPackRunner;

    public JsonHPackProcessor() {
        this(new JsonHPackRunner());
    }

    public JsonHPackProcessor(JsonHPackRunner jsonHPackRunner) {
        this.jsonHPackRunner = jsonHPackRunner;
    }

    private final class JsonHPackWebpipe extends ProcessingWebpipe {

        private boolean pack;

        private JsonHPackWebpipe(Webpipe webpipe, boolean pack) {
            super(webpipe);
            this.pack = pack;
        }

        @Override
        protected WebpipeOutput fetchOutput() throws Exception {
            return new WebpipeOutput(jsonHPackRunner.run(webpipe, pack));
        }
    }

    public Webpipe createProcessingWebpipe(Webpipe source, boolean pack) {
        return new JsonHPackWebpipe(source, pack);
    }

    public ProcessingWebpipeFactory createFactory(final boolean pack) {
        return new ProcessingWebpipeFactory() {
            @Override
            public Webpipe createProcessingWebpipe(Webpipe source) {
                return new JsonHPackWebpipe(source, pack);
            }
        };
    }

}
