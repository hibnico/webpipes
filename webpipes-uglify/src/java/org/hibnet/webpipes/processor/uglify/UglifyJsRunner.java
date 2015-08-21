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
import org.hibnet.webpipes.processor.rhino.RhinoRunner;
import org.hibnet.webpipes.resource.ClasspathResource;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

/**
 * Compress js using uglifyJs utility.
 */
public class UglifyJsRunner extends RhinoRunner {

    private ClasspathResource invokeResource;

    private ClasspathResource defaultOptionsResource;

    public UglifyJsRunner() {
        invokeResource = new ClasspathResource("invoke.js", UglifyJsRunner.class);
        defaultOptionsResource = new ClasspathResource("options.js", UglifyJsRunner.class);
    }

    @Override
    protected void initScope(Context context, ScriptableObject globalScope) throws Exception {
        evaluateFromClasspath(context, globalScope, "/org/hibnet/webpipes/processor/uglify/init.js");
        evaluateFromClasspath(context, globalScope, "/org/hibnet/webpipes/processor/uglify/uglifyJs.min.js");
    }

    public String run(Webpipe webpipe, boolean uglify, String reservedNames) throws Exception {
        String content = webpipe.getOutput().getContent();
        Context context = enterContext();
        try {
            ScriptableObject scope = createLocalScope(context);
            String optionsAsJson = String.format(defaultOptionsResource.getOutput().getContent(), !uglify, reservedNames);
            String script = String.format(invokeResource.getOutput().getContent(), toJSMultiLineString(content), optionsAsJson);
            return evaluate(context, scope, script);
        } finally {
            Context.exit();
        }
    }
}
