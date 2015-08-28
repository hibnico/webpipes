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
package org.hibnet.webpipes.processor.rhino;

import org.hibnet.webpipes.resource.ClasspathResource;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class JsRequireHelper extends AbstractJsRequireHelper {

    private static final long serialVersionUID = 1L;

    private Class<?> clazz;

    private ClassLoader cl;

    private String path;

    public JsRequireHelper(Class<?> clazz, String path) {
        this.clazz = clazz;
        this.path = path;
    }

    public JsRequireHelper(ClassLoader cl, String path) {
        this.cl = cl;
        this.path = path;
    }

    @Override
    protected void load(Context context, Scriptable scope, String filename) throws Exception {
        ClasspathResource ressource;
        if (path != null) {
            filename = path + filename;
        }
        if (clazz != null) {
            ressource = new ClasspathResource(filename, clazz);
        } else {
            ressource = new ClasspathResource(filename, cl);
        }
        context.evaluateString(scope, ressource.getOutput().getContent(), filename, 1, null);
    }
}