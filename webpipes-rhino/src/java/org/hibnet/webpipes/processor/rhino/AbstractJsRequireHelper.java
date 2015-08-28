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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public abstract class AbstractJsRequireHelper extends ScriptableObject {

    static final String VAR_NAME = "__JsRequireHelper__";

    private static final long serialVersionUID = 1L;

    @Override
    public String getClassName() {
        return this.getClass().getSimpleName();
    }

    public static void print(Context context, Scriptable scope, Object[] args, Function funObj) throws Exception {
        System.out.println(Context.toString(args[0]));
    }

    public static void load(Context context, Scriptable scope, Object[] args, Function funObj) throws Exception {
        AbstractJsRequireHelper helper = (AbstractJsRequireHelper) scope.get(VAR_NAME, null);
        for (int i = 0; i < args.length; i++) {
            String filename = Context.toString(args[i]);
            helper.load(context, scope, filename);
        }
    }

    abstract protected void load(Context context, Scriptable scope, String filename) throws Exception;

}