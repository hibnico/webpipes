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
package org.hibnet.webpipes.processor.cjson;

import java.io.IOException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import org.hibnet.webpipes.processor.rhino.RhinoBasedProcessor;
import org.hibnet.webpipes.resource.Resource;
import org.hibnet.webpipes.resource.ResourceFactory;

/**
 * A processor using cjson compression algorithm
 * {@link http://stevehanov.ca/blog/index.php?id=104}.
 */
public class CJsonProcessor extends RhinoBasedProcessor {

    private boolean pack;

    public CJsonProcessor(ResourceFactory resourceFactory, boolean pack) {
        super(resourceFactory);
        this.pack = pack;
    }

    @Override
    protected void initScope(Context context, ScriptableObject globalScope) throws IOException {
        addCommon(context, globalScope);
        addClientSideEnvironment(context, globalScope);
        evaluateFromClasspath(context, globalScope, "/org/hibnet/webpipes/processor/cjson/cjson.min.js");
    }

    @Override
    protected String process(Context context, Scriptable scope, Resource resource, String content) throws Exception {
        StringBuilder script = new StringBuilder();
        if (pack) {
            script.append("CJSON.stringify(JSON.parse(");
        } else {
            script.append("JSON.stringify(CJSON.parse(");
        }
        script.append(toJSMultiLineString(content));
        script.append("));");
        return evaluate(context, scope, script.toString());
    }

}
