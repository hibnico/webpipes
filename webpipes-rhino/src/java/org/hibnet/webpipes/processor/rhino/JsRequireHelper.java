package org.hibnet.webpipes.processor.rhino;

import org.hibnet.webpipes.resource.ClasspathResource;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class JsRequireHelper extends ScriptableObject {

    static final String VAR_NAME = "__JsRequireHelper__";

    private static final long serialVersionUID = 1L;

    private Class<?> clazz;

    private ClassLoader cl;

    public JsRequireHelper(Class<?> clazz) {
        this.clazz = clazz;
    }

    public JsRequireHelper(ClassLoader cl) {
        this.cl = cl;
    }

    @Override
    public String getClassName() {
        return "JsRequireHelper";
    }

    public static void print(Context context, Scriptable scope, Object[] args, Function funObj) throws Exception {
        System.out.println(Context.toString(args[0]));
    }

    public static void load(Context context, Scriptable scope, Object[] args, Function funObj) throws Exception {
        JsRequireHelper helper = (JsRequireHelper) scope.get(VAR_NAME, null);
        for (int i = 0; i < args.length; i++) {
            String filename = Context.toString(args[i]);
            helper.load(context, scope, filename);
        }
    }

    private void load(Context context, Scriptable scope, String filename) throws Exception {
        ClasspathResource ressource;
        if (clazz != null) {
            ressource = new ClasspathResource(filename, clazz);
        } else {
            ressource = new ClasspathResource(filename, cl);
        }
        context.evaluateString(scope, ressource.getContent().getMain(), filename, 1, null);
    }
}