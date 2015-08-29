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
package org.hibnet.webpipes.processor.uglify;

import org.hibnet.webpipes.Webpipe;
import org.hibnet.webpipes.WebpipeOutput;
import org.hibnet.webpipes.processor.ProcessingWebpipe;
import org.hibnet.webpipes.processor.ProcessingWebpipeFactory;

/**
 * Compress js using uglifyJs utility.
 */
public class UglifyJsProcessor {

    private UglifyJsRunner uglifyJsRunner;

    public UglifyJsProcessor() {
        this(new UglifyJsRunner());
    }

    public UglifyJsProcessor(UglifyJsRunner uglifyJsRunner) {
        this.uglifyJsRunner = uglifyJsRunner;
    }

    private final class UglifyJsWebpipe extends ProcessingWebpipe {

        private boolean uglify;

        private String revervedNames;

        private UglifyJsWebpipe(Webpipe webpipe, boolean uglify, String revervedNames) {
            super(webpipe);
            this.uglify = uglify;
            this.revervedNames = revervedNames;
        }

        @Override
        protected WebpipeOutput fetchOutput() throws Exception {
            return new WebpipeOutput(uglifyJsRunner.run(webpipe, uglify, revervedNames));
        }
    }

    public Webpipe createProcessingWebpipe(Webpipe source, boolean uglify, String revervedNames) {
        return new UglifyJsWebpipe(source, uglify, revervedNames);
    }

    public ProcessingWebpipeFactory createFactory(final boolean uglify, final String revervedNames) {
        return new ProcessingWebpipeFactory() {
            @Override
            public Webpipe createProcessingWebpipe(Webpipe source) {
                return new UglifyJsWebpipe(source, uglify, revervedNames);
            }
        };
    }

}
