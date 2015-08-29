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
import org.hibnet.webpipes.js.JsProcessor;
import org.hibnet.webpipes.processor.ProcessingWebpipe;
import org.hibnet.webpipes.processor.ProcessingWebpipeFactory;

/**
 * A processor using json.hpack compression algorithm: @see https://github.com/WebReflection/json.hpack
 */
public class JsonHPackProcessor extends JsProcessor {

    @Override
    protected void initEngine() throws Exception {
        evalFromClasspath("/org/hibnet/webpipes/processor/jsonhpack/json.hpack.min.js");
        evalFromClasspath("/org/hibnet/webpipes/processor/jsonhpack/webpipes_runner.js");
    }

    private WebpipeOutput process(Webpipe webpipe, boolean pack) throws Exception {
        String content = webpipe.getOutput().getContent();
        boolean isEnclosed;
        if (pack) {
            isEnclosed = isEnclosedInArray(content);
            if (!isEnclosed) {
                content = "[" + content + "]";
            }
        } else {
            isEnclosed = isEnclosedInDoubleArray(content);
            if (!isEnclosed) {
                content = "[" + content + "]";
            }
        }
        WebpipeOutput res = callRunner(content, pack);
        if (!isEnclosed) {
            return new WebpipeOutput(removeEnclosedArray(res.getContent()), res.getSourceMap());
        }
        return res;
    }

    private final class JsonHPackWebpipe extends ProcessingWebpipe {

        private boolean pack;

        private JsonHPackWebpipe(Webpipe webpipe, boolean pack) {
            super(webpipe);
            this.pack = pack;
        }

        @Override
        protected WebpipeOutput fetchOutput() throws Exception {
            return process(webpipe, pack);
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

    /**
     * Remove first and last occurrence of '[' and ']' characters.
     */
    private String removeEnclosedArray(final String resultAsString) {
        return resultAsString.replaceFirst("(?ims)\\[", "").replaceFirst("(?ims)\\](?!.*\\])", "");
    }

    /**
     * Check if the string is enclosed with [] (array).
     * 
     * @param rawData
     *            string to test.
     */
    private boolean isEnclosedInArray(final String rawData) {
        return rawData.matches("(?ims)^\\s*\\[.*\\]");
    }

    /**
     * Check if the string is enclosed with [[]] (double array).
     * 
     * @param rawData string to test.
     */
    private boolean isEnclosedInDoubleArray(final String rawData) {
        return rawData.matches("(?ims)^\\s*\\[\\[.*\\]\\]");
    }
}
