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
package org.hibnet.webpipes.processor.emberjs;

import org.hibnet.webpipes.Webpipe;
import org.hibnet.webpipes.processor.rhino.SimpleRhinoRunner;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * Compiles EmberJS templates to javascript. The processor loads emberJs library and all its dependencies from the webjar.
 */
public class EmberJsRunner extends SimpleRhinoRunner {

    @Override
    protected void initScope(Context context, ScriptableObject globalScope) throws Exception {
        addCommon(context, globalScope);
        addClientSideEnvironment(context, globalScope);
        evaluateFromWebjar(context, globalScope, "jquery.js");
        evaluateFromWebjar(context, globalScope, "handlebars.js");
        evaluateFromWebjar(context, globalScope, "ember.js");
        // The Ember Template Compiler is built for CommonJs environment, but Rhino doesn't comply with CommonJs Standard There is no 'exports' object
        // in Rhino, so this file creates it, as well as an helper function
        evaluateFromClasspath(context, globalScope, "/org/hibnet/webpipes/processor/emberjs/headless-rhino.js");
    }

    @Override
    protected String run(Webpipe webpipe, Context context, Scriptable scope) throws Exception {
        String content = webpipe.getContent().getMain();
        String script = buildSimpleRunScript("precompile", content);
        String result = evaluate(context, scope, script);
        return "(function() {Ember.TEMPLATES['" + getVarName(webpipe) + "'] = Ember.Handlebars.template(" + result + ")})();";
    }

}