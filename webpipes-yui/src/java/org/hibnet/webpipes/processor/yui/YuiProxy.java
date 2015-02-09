package org.hibnet.webpipes.processor.yui;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

class YuiProxy {

    static final YuiProxy Instance = new YuiProxy();

    private URLClassLoader cl;

    private Constructor<?> jsCompressorConstructor;

    private Method jsCompressMethod;

    private Constructor<?> cssCompressorConstructor;

    private Method cssCompressMethod;

    public YuiProxy() {
        cl = new URLClassLoader(new URL[] { this.getClass().getResource("jar/yuicompressor-2.4.8.jar") });
        try {
            Class<?> jsCompressorClass = cl.loadClass("com.yahoo.platform.yui.compressor.JavaScriptCompressor");
            Class<?> errorReporterClass = cl.loadClass("org.mozilla.javascript.ErrorReporter");
            jsCompressorConstructor = jsCompressorClass.getConstructor(Reader.class, errorReporterClass);
            jsCompressMethod = jsCompressorClass.getMethod("compress", Writer.class, Integer.TYPE, Boolean.TYPE, Boolean.TYPE, Boolean.TYPE,
                    Boolean.TYPE);
            Class<?> cssCompressorClass = cl.loadClass("com.yahoo.platform.yui.compressor.CssCompressor");
            cssCompressorConstructor = cssCompressorClass.getConstructor(Reader.class);
            cssCompressMethod = cssCompressorClass.getMethod("compress", Writer.class, Integer.TYPE);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public String compressJavascript(Reader reader, Writer writer, int linebreak, boolean munge, boolean verbose, boolean preserveAllSemiColons,
            boolean disableOptimizations) {
        Object res;
        try {
            Object jsCompressor = jsCompressorConstructor.newInstance(reader, null);
            res = jsCompressMethod.invoke(jsCompressor, writer, linebreak, munge, verbose, preserveAllSemiColons, disableOptimizations);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return (String) res;
    }

    public String compressCSS(Reader reader, Writer writer, int linebreak) {
        Object res;
        try {
            Object cssCompressor = cssCompressorConstructor.newInstance(reader);
            res = cssCompressMethod.invoke(cssCompressor, writer, linebreak);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return (String) res;
    }

}
