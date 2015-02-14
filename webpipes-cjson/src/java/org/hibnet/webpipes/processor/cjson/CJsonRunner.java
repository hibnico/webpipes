package org.hibnet.webpipes.processor.cjson;

import org.hibnet.webpipes.Webpipe;
import org.hibnet.webpipes.processor.rhino.RhinoRunner;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

public class CJsonRunner extends RhinoRunner {

    @Override
    protected void initScope(Context context, ScriptableObject globalScope) throws Exception {
        addCommon(context, globalScope);
        addClientSideEnvironment(context, globalScope);
        evaluateFromClasspath(context, globalScope, "/org/hibnet/webpipes/processor/cjson/cjson.min.js");
    }

    public String run(Webpipe webpipe, boolean pack) throws Exception {
        String content = webpipe.getContent().getMain();
        Context context = enterContext();
        try {
            ScriptableObject scope = createLocalScope(context);
            StringBuilder script = new StringBuilder();
            if (pack) {
                script.append("CJSON.stringify(JSON.parse(");
            } else {
                script.append("JSON.stringify(CJSON.parse(");
            }
            script.append(toJSMultiLineString(content));
            script.append("));");
            content = evaluate(context, scope, script.toString());
        } finally {
            Context.exit();
        }
        return content;
    }
}