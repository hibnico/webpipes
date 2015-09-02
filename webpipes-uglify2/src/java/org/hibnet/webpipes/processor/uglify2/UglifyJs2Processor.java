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
import org.hibnet.webpipes.WebpipeUtils;
import org.hibnet.webpipes.js.JsProcessor;
import org.hibnet.webpipes.processor.ProcessingWebpipe;
import org.hibnet.webpipes.processor.ProcessingWebpipeFactory;
import org.hibnet.webpipes.resource.WebJarHelper;

/**
 * Compress js using uglifyJs utility.
 */
public class UglifyJs2Processor extends JsProcessor {

    private static String[] libs;

    static {
        String uglifyjsPath = WebJarHelper.getWebJarAssetLocator().getFullPath("uglifyjs");
        String uglifyjsDir = "/" + uglifyjsPath.substring(0, uglifyjsPath.length() - 13);
        // @formatter:off
        libs = new String[] {
                    uglifyjsDir + "/lib/utils.js",
                    uglifyjsDir + "/lib/ast.js",
                    uglifyjsDir + "/lib/parse.js",
                    uglifyjsDir + "/lib/transform.js",
                    uglifyjsDir + "/lib/scope.js",
                    uglifyjsDir + "/lib/output.js",
                    uglifyjsDir + "/lib/compress.js",
                    uglifyjsDir + "/lib/sourcemap.js",
                    uglifyjsDir + "/lib/mozilla-ast.js",
                    uglifyjsDir + "/lib/propmangle.js"
                   };
        // @formatter:on
    }

    @Override
    protected void initEngine() throws Exception {
        addSourceMap();
        eval("MOZ_SourceMap = sourceMap;");
        for (String lib : libs) {
            evalFromClasspath(lib);
        }
        evalFromClasspath("/org/hibnet/webpipes/processor/uglify2/webpipes_runner.js");
    }

    private WebpipeOutput process(Webpipe webpipe, boolean uglify) throws Exception {
        return callRunner(uglify, webpipe.getPath(), webpipe.getOutput().getContent(), webpipe.getOutput().getSourceMap());
    }

    private final class UglifyJs2Webpipe extends ProcessingWebpipe {

        private boolean uglify;

        private UglifyJs2Webpipe(String path, Webpipe webpipe, boolean uglify) {
            super(WebpipeUtils.idOf(UglifyJs2Processor.class, webpipe, uglify), path, "uglify2", webpipe);
            this.uglify = uglify;
        }

        @Override
        protected WebpipeOutput fetchOutput() throws Exception {
            return process(getChildWebpipe(), uglify);
        }
    }

    public Webpipe createProcessingWebpipe(String path, Webpipe source, boolean uglify) {
        return new UglifyJs2Webpipe(path, source, uglify);
    }

    public ProcessingWebpipeFactory createFactory(final boolean uglify) {
        return new ProcessingWebpipeFactory() {
            @Override
            public Webpipe createProcessingWebpipe(String path, Webpipe source) {
                return new UglifyJs2Webpipe(path, source, uglify);
            }
        };
    }

}
