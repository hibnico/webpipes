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
package org.hibnet.webpipes.processor.uglify2;

import org.hibnet.webpipes.Webpipe;
import org.hibnet.webpipes.WebpipeOutput;
import org.hibnet.webpipes.processor.ProcessingWebpipe;
import org.hibnet.webpipes.processor.ProcessingWebpipeFactory;

/**
 * Compress js using uglifyJs utility.
 */
public class UglifyJs2Processor {

    private UglifyJs2Runner uglifyJs2Runner;

    public UglifyJs2Processor() {
        this(new UglifyJs2Runner());
    }

    public UglifyJs2Processor(UglifyJs2Runner uglifyJs2Runner) {
        this.uglifyJs2Runner = uglifyJs2Runner;
    }

    private final class UglifyJs2Webpipe extends ProcessingWebpipe {

        private boolean uglify;

        private UglifyJs2Webpipe(Webpipe webpipe, boolean uglify) {
            super(webpipe);
            this.uglify = uglify;
        }

        @Override
        protected WebpipeOutput fetchContent() throws Exception {
            return uglifyJs2Runner.run(webpipe, uglify);
        }
    }

    public Webpipe createProcessingWebpipe(Webpipe source, boolean uglify) {
        return new UglifyJs2Webpipe(source, uglify);
    }

    public ProcessingWebpipeFactory createFactory(final boolean uglify) {
        return new ProcessingWebpipeFactory() {
            @Override
            public Webpipe createProcessingWebpipe(Webpipe source) {
                return new UglifyJs2Webpipe(source, uglify);
            }
        };
    }

}
