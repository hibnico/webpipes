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

import java.util.List;

import org.hibnet.webpipes.Webpipe;
import org.hibnet.webpipes.WebpipeOutput;
import org.hibnet.webpipes.WebpipeUtils;
import org.hibnet.webpipes.js.JsProcessor;
import org.hibnet.webpipes.processor.ProcessingWebpipe;
import org.hibnet.webpipes.processor.ProcessingWebpipeFactory;

/**
 * Compress js using uglifyJs utility.
 */
public class UglifyJsProcessor extends JsProcessor {

    @Override
    protected void initEngine() throws Exception {
        eval("var exports = {}; function require() { return exports; }; var process = { version : 0.1 };");
        evalFromClasspath("/org/hibnet/webpipes/processor/uglify/uglifyJs.min.js");
        evalFromClasspath("/org/hibnet/webpipes/processor/uglify/webpipes_runner.js");
    }

    private WebpipeOutput process(Webpipe webpipe, boolean uglify, List<String> reservedNames) throws Exception {
        return callRunner(webpipe.getOutput().getContent(), uglify, reservedNames);
    }

    private final class UglifyJsWebpipe extends ProcessingWebpipe {

        private boolean uglify;

        private List<String> revervedNames;

        private UglifyJsWebpipe(String path, Webpipe webpipe, boolean uglify, List<String> revervedNames) {
            super(WebpipeUtils.idOf(UglifyJsProcessor.class, webpipe, uglify, revervedNames), path, "uglify", webpipe);
            this.uglify = uglify;
            this.revervedNames = revervedNames;
        }

        @Override
        protected WebpipeOutput fetchOutput() throws Exception {
            return process(getChildWebpipe(), uglify, revervedNames);
        }
    }

    public Webpipe createProcessingWebpipe(String path, Webpipe source, boolean uglify, List<String> revervedNames) {
        return new UglifyJsWebpipe(path, source, uglify, revervedNames);
    }

    public ProcessingWebpipeFactory createFactory(final boolean uglify, final List<String> revervedNames) {
        return new ProcessingWebpipeFactory() {
            @Override
            public Webpipe createProcessingWebpipe(String path, Webpipe source) {
                return new UglifyJsWebpipe(path, source, uglify, revervedNames);
            }
        };
    }

}
