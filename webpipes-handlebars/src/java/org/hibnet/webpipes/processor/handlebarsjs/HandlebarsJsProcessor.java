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
package org.hibnet.webpipes.processor.handlebarsjs;

import org.hibnet.webpipes.Webpipe;
import org.hibnet.webpipes.processor.rhino.RhinoBasedProcessor;
import org.hibnet.webpipes.resource.ResourceFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * Compiles HandlebarsJS templates to javascript.
 */
public class HandlebarsJsProcessor extends RhinoBasedProcessor {

    public HandlebarsJsProcessor(ResourceFactory resourceFactory) {
        super(resourceFactory);
    }

    @Override
    protected void initScope(Context context, ScriptableObject globalScope) throws Exception {
        addCommon(context, globalScope);
        evaluateFromWebjar(context, globalScope, "handlebars.js");
    }

    @Override
    protected String process(Context context, Scriptable scope, Webpipe webpipe, String content) throws Exception {
        String script = buildSimpleRunScript("Handlebars.precompile", content);
        String result = evaluate(context, scope, script);
        return "(function() { var template = Handlebars.template, templates = Handlebars.templates = Handlebars.templates || {};templates['"
                + webpipe.getId()
                + "'] = template(" + result + " ); })();";
    }

}
