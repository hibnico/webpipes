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
package org.hibnet.webpipes.processor.cjson;

import org.hibnet.webpipes.Webpipe;
import org.hibnet.webpipes.processor.ProcessingWebpipe;
import org.hibnet.webpipes.processor.ProcessingWebpipeFactory;

/**
 * A processor using cjson compression algorithm {@link http://stevehanov.ca/blog/index.php?id=104}.
 */
public class CJsonProcessor {

    private CJsonRunner cJsonRunner;

    public CJsonProcessor() {
        this(new CJsonRunner());
    }

    public CJsonProcessor(CJsonRunner cJsonRunner) {
        this.cJsonRunner = cJsonRunner;
    }

    private final class CJsonWebpipe extends ProcessingWebpipe {

        private boolean pack;

        private CJsonWebpipe(Webpipe webpipe, boolean pack) {
            super(webpipe);
            this.pack = pack;
        }

        @Override
        protected String fetchContent() throws Exception {
            return cJsonRunner.run(webpipe, pack);
        }
    }

    public Webpipe createProcessingWebpipe(Webpipe source, boolean pack) {
        return new CJsonWebpipe(source, pack);
    }

    public ProcessingWebpipeFactory createFactory(final boolean pack) {
        return new ProcessingWebpipeFactory() {
            @Override
            public Webpipe createProcessingWebpipe(Webpipe source) {
                return new CJsonWebpipe(source, pack);
            }
        };
    }

}
