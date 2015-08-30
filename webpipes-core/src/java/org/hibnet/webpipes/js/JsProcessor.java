package org.hibnet.webpipes.js;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.hibnet.webpipes.Webpipe;
import org.hibnet.webpipes.WebpipeOutput;
import org.hibnet.webpipes.WebpipeUtils;
import org.hibnet.webpipes.resource.ClasspathResource;
import org.hibnet.webpipes.resource.Resource;
import org.hibnet.webpipes.resource.WebJarHelper;

public abstract class JsProcessor {

    protected static Resource envScript = new ClasspathResource("env.nashorn.1.2.js", JsProcessor.class);

    protected static Resource sourceMapScript = new ClasspathResource("source-map.min.js", JsProcessor.class);

    public ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("nashorn");

    private CompiledScript getContentScript;

    private CompiledScript getSourceMapScript;

    {
        try {
            getContentScript = ((Compilable) jsEngine).compile("res.content");
            getSourceMapScript = ((Compilable) jsEngine).compile("res.sourceMap");
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }

    public JsProcessor() {
        try {
            initEngine();
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize the Javascript engine", e);
        }
    }

    abstract protected void initEngine() throws Exception;

    protected void addClientSideEnvironment() throws Exception {
        eval(envScript);
    }

    protected void addSourceMap() throws Exception {
        eval(sourceMapScript);
    }

    protected <T> T evalFromClasspath(String path) throws Exception {
        return eval(new ClasspathResource(path));
    }

    protected <T> T evalFromWebjar(String path) throws Exception {
        return eval(WebJarHelper.getResource(path));
    }

    protected <T> T eval(Resource script) throws Exception {
        return eval(script.getOutput().getContent());
    }

    protected <T> T eval(String script) throws ScriptException {
        return eval(new StringReader(script), null);
    }

    @SuppressWarnings("unchecked")
    protected <T> T eval(Reader script, Bindings bindings) throws ScriptException {
        if (bindings != null) {
            return (T) jsEngine.eval(script, bindings);
        } else {
            return (T) jsEngine.eval(script);
        }
    }

    protected Bindings createLocal(Bindings globalBindings) throws ScriptException {
        Bindings localBindings = jsEngine.createBindings();
        localBindings.putAll(globalBindings);
        return localBindings;
    }

    @SuppressWarnings("unchecked")
    protected <T> T invokeFunction(String function, Object... args) throws Exception {
        Invocable invocable = (Invocable) jsEngine;
        return (T) invocable.invokeFunction(function, args);
    }

    @SuppressWarnings("unchecked")
    protected <T> T invokeMethod(String object, String method, Object... args) throws Exception {
        Invocable invocable = (Invocable) jsEngine;
        return (T) invocable.invokeMethod(jsEngine.get(object), method, args);
    }

    protected WebpipeOutput callRunner(Object... args) throws Exception {
        Object res = invokeFunction("webpipes_runner", args);
        Bindings bindings = jsEngine.createBindings();
        bindings.put("res", res);
        String content = (String) getContentScript.eval(bindings);
        String sourceMap = (String) getSourceMapScript.eval(bindings);
        return new WebpipeOutput(content, WebpipeUtils.parseSourceMap(sourceMap));
    }

    protected String getVarName(Webpipe webpipe) {
        String name = webpipe.getPath();
        int i = name.lastIndexOf(".");
        if (i > 0) {
            name = name.substring(0, i);
        }
        i = name.lastIndexOf("/");
        if (i > 0) {
            name = name.substring(i + 1, name.length());
        }
        return name;
    }

    protected Map<String, Object> jsMap(Object... keyAndValues) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < keyAndValues.length; i += 2) {
            map.put((String) keyAndValues[i], keyAndValues[i + 1]);
        }
        return map;
    }
}
