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
package org.hibnet.webpipes.processor.uglify;

import java.io.IOException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import org.hibnet.webpipes.processor.rhino.RhinoBasedProcessor;
import org.hibnet.webpipes.resource.ClasspathResource;
import org.hibnet.webpipes.resource.Resource;
import org.hibnet.webpipes.resource.ResourceFactory;

/**
 * Compress js using uglifyJs utility.
 */
public class UglifyJsProcessor extends RhinoBasedProcessor {

    private boolean uglify;

    private String reservedNames;

    private ClasspathResource invokeResource;

    private ClasspathResource defaultOptionsResource;

    public UglifyJsProcessor(boolean uglify, ResourceFactory resourceFactory) {
        super(resourceFactory);
        this.uglify = uglify;
        invokeResource = new ClasspathResource("invoke.js", UglifyJsProcessor.class);
        defaultOptionsResource = new ClasspathResource("options.js", UglifyJsProcessor.class);
    }

    /**
     * some libraries rely on certain names to be used, so this option allow you to exclude such names from the mangler. For example, to keep names
     * require and $super intact you'd specify –reserved-names "require,$super".
     * 
     * @param reservedNames the reservedNames to set
     */
    public void setReservedNames(String reservedNames) {
        this.reservedNames = reservedNames;
    }

    @Override
    protected void initScope(Context context, ScriptableObject globalScope) throws IOException {
        evaluateFromClasspath(context, globalScope, "/org/hibnet/webpipes/processor/uglify/init.js");
        evaluateFromClasspath(context, globalScope, "/org/hibnet/webpipes/processor/uglify/uglifyJs.min.js");
    }

    @Override
    protected String process(Context context, Scriptable scope, Resource resource, String content) throws Exception {
        String optionsAsJson = createOptionsAsJson();
        String script = String.format(invokeResource.getContent(), toJSMultiLineString(content), optionsAsJson);
        return evaluate(context, scope, script);
    }

    /**
     * @return not null value representing reservedNames.
     */
    private String getReservedNames() {
        return this.reservedNames == null ? "" : reservedNames;
    }

    protected String createOptionsAsJson() throws IOException {
        return String.format(defaultOptionsResource.getContent(), !uglify, getReservedNames());
    }
}
