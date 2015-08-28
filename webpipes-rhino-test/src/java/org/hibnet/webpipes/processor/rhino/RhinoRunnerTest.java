package org.hibnet.webpipes.processor.rhino;

import org.junit.Assert;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

public class RhinoRunnerTest {

    @Test
    public void testRequire() throws Exception {
        RhinoRunner runner = new RhinoRunner() {
            @Override
            protected void initScope(Context context, ScriptableObject globalScope) throws Exception {
                addRequire(context, globalScope, new JsRequireHelper(RhinoRunnerTest.class, null));
                runRequire(context, globalScope, "lib.js");
            }
        };

        Context context = runner.enterContext();
        try {
            ScriptableObject scope = runner.createLocalScope(context);
            Object res = runner.evaluate(context, scope, "someVar");
            Assert.assertEquals("foo", Context.toString(res));
        } finally {
            Context.exit();
        }

        runner = new RhinoRunner() {
            @Override
            protected void initScope(Context context, ScriptableObject globalScope) throws Exception {
                addRequire(context, globalScope, new JsRequireHelper(RhinoRunnerTest.class, null));
                runRequire(context, globalScope, "lib/lib2.js");
            }
        };

        context = runner.enterContext();
        try {
            ScriptableObject scope = runner.createLocalScope(context);
            Object res = runner.evaluate(context, scope, "someOtherVar");
            Assert.assertEquals("bar", Context.toString(res));
        } finally {
            Context.exit();
        }
    }
}
