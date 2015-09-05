/*
 *  Copyright 2015 WebPipes contributors
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
package org.hibnet.webpipes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibnet.webpipes.processor.SourceMapStripperWebpipe;
import org.hibnet.webpipes.resource.ClasspathResource;
import org.hibnet.webpipes.resource.FileResource;
import org.hibnet.webpipes.resource.StringResource;
import org.hibnet.webpipes.resource.WebJarHelper;
import org.hibnet.webpipes.resource.pattern.ClasspathPatternResource;
import org.hibnet.webpipes.resource.pattern.FilePatternResource;

public abstract class WebpipesBuilder {

    private static final Map<String, Webpipe> NO_WEBPIPES = new HashMap<>();

    private volatile Map<String, Webpipe> webpipesMap = NO_WEBPIPES;

    public Map<String, Webpipe> getWebpipes() {
        if (webpipesMap == NO_WEBPIPES) {
            synchronized (webpipesMap) {
                if (webpipesMap == NO_WEBPIPES) {
                    webpipesMap = buildMap(buildWebpipes());
                }
            }
        }
        return webpipesMap;
    }

    private Map<String, Webpipe> buildMap(List<Webpipe> webpipes) {
        Map<String, Webpipe> map = new HashMap<>();
        for (Webpipe webpipe : webpipes) {
            map.put(webpipe.getPath(), webpipe);
        }
        return map;
    }

    public boolean refreshedWebpipes() {
        Map<String, Webpipe> freshes = diffWebpipes();
        if (freshes != null) {
            synchronized (webpipesMap) {
                webpipesMap = freshes;
                return true;
            }
        }
        return false;
    }

    private Map<String, Webpipe> diffWebpipes() {
        Map<String, Webpipe> existings = getWebpipes();
        Map<String, Webpipe> freshes = buildMap(buildWebpipes());

        // will contains the new webpipes from 'freshes' and the unchanged ones from 'existings'
        Map<String, Webpipe> diff = new HashMap<>();

        boolean hasDiff = existings.size() != freshes.size();

        for (Entry<String, Webpipe> fresh : freshes.entrySet()) {
            Webpipe existing = existings.get(fresh.getKey());
            if (existing == null || !fresh.getValue().getId().equals(existing.getId())) {
                hasDiff = true;
                diff.put(fresh.getKey(), fresh.getValue());
            } else {
                diff.put(fresh.getKey(), existing);
            }
        }

        if (hasDiff) {
            return diff;
        }

        return null;
    }

    abstract protected List<Webpipe> buildWebpipes();

    protected Webpipe string(String content) {
        return string(null, content);
    }

    protected Webpipe string(String path, String content) {
        return new StringResource(path, content);
    }

    protected Webpipe file(String filePath) {
        return classpath(null, filePath);
    }

    protected Webpipe file(String path, String filePath) {
        return new FileResource(path, filePath);
    }

    protected Webpipe files(String filePaths) {
        return classpaths(null, filePaths);
    }

    protected Webpipe files(String path, String pattern) {
        return new FilePatternResource(path, pattern);
    }

    protected Webpipe classpath(String classpath) {
        return classpath(null, classpath);
    }

    protected Webpipe classpath(String path, String classpath) {
        return new ClasspathResource(path, classpath);
    }

    protected Webpipe classpaths(String pattern) {
        return classpaths(null, pattern);
    }

    protected Webpipe classpaths(String path, String pattern) {
        return new ClasspathPatternResource(path, pattern);
    }

    protected Webpipe webjar(String name, String version, String webjarpath) {
        return webjar(null, name, version, webjarpath);
    }

    protected Webpipe webjar(String path, String name, String version, String webjarpath) {
        return new ClasspathResource(path, "/META-INF/resources/webjars/" + name + "/" + version + "/" + webjarpath);
    }

    protected Webpipe webjar(String webjarpath) {
        return webjar(null, webjarpath);
    }

    protected Webpipe webjar(String path, String webjarpath) {
        return WebJarHelper.getResource(path, webjarpath);
    }

    protected Webpipe sourceMapStripped(Webpipe webpipe) {
        return sourceMapStripped(null, webpipe);
    }

    protected Webpipe sourceMapStripped(String path, Webpipe webpipe) {
        return new SourceMapStripperWebpipe(path, webpipe);
    }

    protected Webpipe merge(Webpipe... webpipes) {
        return merge(null, webpipes);
    }

    protected Webpipe merge(String path, Webpipe... webpipes) {
        return new SimpleMergingWebpipe(path, webpipes);
    }

}
