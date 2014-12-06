/*
 *  Copyright 2014 WebPipes contributors
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
package org.hibnet.webpipes.processor.typescript;

import java.io.IOException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import org.hibnet.webpipes.processor.rhino.RhinoBasedProcessor;
import org.hibnet.webpipes.resource.Resource;
import org.hibnet.webpipes.resource.ResourceFactory;

/**
 * Compiles TypeScript into javascript in a cross platform manner. Uses rhino to interpret javascript implementation of the compiler.
 */
public class TypeScriptProcessor extends RhinoBasedProcessor {

    private final String ecmaScriptVersion = "TypeScript.CodeGenTarget.ES5";

    private static final String PARAM_ERRORS = "errors";

    private static final String PARAM_SOURCE = "source";

    public TypeScriptProcessor(ResourceFactory resourceFactory) {
        super(resourceFactory);
    }

    @Override
    protected void initScope(Context context, ScriptableObject globalScope) throws IOException {
        addCommon(context, globalScope);
        evaluateFromClasspath(context, globalScope, "/org/hibnet/webpipes/processor/typescript/typescript-0.8.js");
        evaluateFromClasspath(context, globalScope, "/org/hibnet/webpipes/processor/typescript/typescript.compile-0.3.js");
    }

    @Override
    protected String process(Context context, Scriptable scope, Resource resource, String content) throws Exception {
        String script = buildSimpleRunScript("compilerWrapper.compile", content, ecmaScriptVersion);
        NativeObject result = evaluate(context, scope, script);
        final NativeArray errors = (NativeArray) result.get(PARAM_ERRORS);
        if (errors.size() > 0) {
            final StringBuilder sb = new StringBuilder();
            for (final Object error : errors) {
                sb.append(error.toString()).append("\n");
            }
            throw new RuntimeException(sb.toString());
        }
        return result.get(PARAM_SOURCE).toString();
    }

}
