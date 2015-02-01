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
import org.hibnet.webpipes.processor.rhino.RhinoBasedProcessor;
import org.hibnet.webpipes.resource.ResourceFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * Uses coffee script library loaded from the webjar to compile to javascript code.
 */
public class CoffeeScriptProcessor extends RhinoBasedProcessor {

    private String[] options;

    public CoffeeScriptProcessor(ResourceFactory resourceFactory) {
        super(resourceFactory);
    }

    public void setOptions(String... options) {
        this.options = options;
    }

    @Override
    protected void initScope(Context context, ScriptableObject globalScope) throws Exception {
        evaluateFromWebjar(context, globalScope, "coffee-script.min.js");
    }

    @Override
    protected String process(Context context, Scriptable scope, Webpipe webpipe, String content) throws Exception {
        StringBuilder script = new StringBuilder("CoffeeScript.compile(");
        script.append(toJSMultiLineString(content));
        script.append(",{");
        if (options != null) {
            for (int i = 0; i < options.length; i++) {
                script.append(options[i]).append(": true");
                if (i < options.length - 1) {
                    script.append(",");
                }
            }
        }
        script.append("});");
        return evaluate(context, scope, script.toString());
    }

}
