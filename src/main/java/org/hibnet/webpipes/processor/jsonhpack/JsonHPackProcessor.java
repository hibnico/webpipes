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
package org.hibnet.webpipes.processor.jsonhpack;

import java.io.IOException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import org.hibnet.webpipes.processor.rhino.RhinoBasedProcessor;
import org.hibnet.webpipes.resource.Resource;
import org.hibnet.webpipes.resource.ResourceFactory;

/**
 * A processor using json.hpack compression algorithm: @see https://github.com/WebReflection/json.hpack
 */
public class JsonHPackProcessor extends RhinoBasedProcessor {

    private boolean pack;

    public JsonHPackProcessor(boolean pack, ResourceFactory resourceFactory) {
        super(resourceFactory);
        this.pack = pack;
    }

    @Override
    protected void initScope(Context context, ScriptableObject globalScope) throws IOException {
        addCommon(context, globalScope);
        evaluateFromClasspath(context, globalScope, "/org/hibnet/webpipes/processor/jsonhpack/json.hpack.min.js");
    }

    @Override
    protected String process(Context context, Scriptable scope, Resource resource, String content) throws Exception {
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

        StringBuilder script = new StringBuilder();
        if (pack) {
            script.append("JSON.stringify(JSON.hpack(eval(");
        } else {
            script.append("JSON.stringify(JSON.hunpack(eval(");
        }
        script.append(toJSMultiLineString(content));
        if (pack) {
            script.append("), 4));");
        } else {
            script.append(")));");
        }

        String result = evaluate(context, scope, script.toString());

        if (!isEnclosed) {
            // remove [] characters in which the json is enclosed
            result = removeEnclosedArray(result);
        }

        return result;
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
     * @param rawData string to test.
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
