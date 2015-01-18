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
package org.hibnet.webpipes.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.hibnet.webpipes.Webpipe;

public class GenerateFilesTask extends Task {

    private String webpipesBuilder;

    private Charset encoding = Charset.forName("UTF8");

    private File dir;

    public void setWebpipesBuilder(String webpipesBuilder) {
        this.webpipesBuilder = webpipesBuilder;
    }

    public void setDir(File dir) {
        this.dir = dir;
    }

    public void setEncoding(String charsetName) {
        this.encoding = Charset.forName(charsetName);
    }

    @Override
    public void execute() throws BuildException {
        if (webpipesBuilder == null) {
            throw new BuildException("The required parameter 'webpipesBuilder' is missing");
        }
        if (dir == null) {
            throw new BuildException("The required parameter 'dir' is missing");
        }
        if (!dir.exists()) {
            dir.mkdirs();
        } else if (!dir.isDirectory()) {
            throw new BuildException("The parameter 'dir' must be a folder");
        }
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            Class<?> cl;
            try {
                cl = Class.forName(webpipesBuilder);
            } catch (ClassNotFoundException e) {
                throw new BuildException("Could not find " + webpipesBuilder + ". Make sure it is in the same classpath as the ant task", e);
            }
            Object o;
            try {
                o = cl.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new BuildException("Could not instanciate " + webpipesBuilder + ". Make sure it has a constructor without arguments", e);
            }
            Method m;
            try {
                m = cl.getMethod("buildWebpipes");
            } catch (NoSuchMethodException | SecurityException e) {
                throw new BuildException("Could not get webpipes out of " + webpipesBuilder
                        + ". Make sure it has a method 'buildWebpipes' with no arguments", e);
            }
            Object res;
            try {
                res = m.invoke(o);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new BuildException("Error calling 'buildWebpipes' on " + webpipesBuilder
                        + " (switch to verbose to see a full stack trace with -v)", e);
            }
            List<Webpipe> webpipes;
            try {
                @SuppressWarnings("unchecked")
                List<Webpipe> casted = (List<Webpipe>) res;
                webpipes = casted;
            } catch (ClassCastException e) {
                throw new BuildException("The method 'buildWebpipes' on " + webpipesBuilder + " is not returning a List<Webpipe>", e);
            }
            for (Webpipe webpipe : webpipes) {
                List<String> contents;
                try {
                    contents = webpipe.getContents();
                } catch (IOException e) {
                    throw new BuildException("IO error while getting contents from webpipe " + webpipe.getPaths() + ": " + e.getMessage(), e);
                }
                for (int i = 0; i < webpipe.getPaths().size(); i++) {
                    String path = webpipe.getPaths().get(i);
                    path = path.replaceAll("/", File.separator).replaceAll("\\", File.separator);
                    File dest = new File(dir, path);
                    if (!dest.getParentFile().exists()) {
                        dest.getParentFile().mkdirs();
                    } else if (!dest.getParentFile().isDirectory()) {
                        dest.getParentFile().delete();
                        dest.getParentFile().mkdirs();
                    }
                    try (OutputStream out = new FileOutputStream(dest)) {
                        log("Generating " + dest.getAbsolutePath());
                        out.write(contents.get(i).getBytes(encoding));
                    } catch (IOException e) {
                        throw new BuildException("IO error while writing the file " + dest.getAbsolutePath(), e);
                    }
                }
            }
            log(webpipes.size() + " webpipes to processed");
        } finally {
            Thread.currentThread().setContextClassLoader(ccl);
        }
    }
}
