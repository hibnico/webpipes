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
import org.hibnet.webpipes.processor.rhino.RhinoRunner;
import org.hibnet.webpipes.resource.WebJarHelper;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

/**
 * Compress js using uglifyJs utility.
 */
public class UglifyJs2Runner extends RhinoRunner {

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
                    uglifyjsDir + "/lib/mozilla-ast.js"
                   };
        // @formatter:on
    }

    @Override
    protected void initScope(Context context, ScriptableObject globalScope) throws Exception {
        // addRequireJS(context, globalScope);
        // runRequire(context, globalScope, libs);
        for (String lib : libs) {
            evaluateFromClasspath(context, globalScope, lib);
        }
        // evaluate(context, globalScope, new FileResource("/Users/nlalevee/dev/js/uglify-js2/git/uglifyjs-self.js"));
    }

    public String run(Webpipe webpipe, boolean uglify) throws Exception {
        String content = webpipe.getOutput().getContent();
        Context context = enterContext();
        try {
            ScriptableObject scope = createLocalScope(context);

            StringBuilder script = new StringBuilder();
            script.append("var a = parse(");
            appendJSMultiLineString(script, content);
            script.append(");");
            script.append("var c = Compressor({});");
            script.append("a = a.transform(c);");
            script.append("a.print_to_string();");

            return evaluate(context, scope, script.toString());
        } finally {
            Context.exit();
        }
    }
}
