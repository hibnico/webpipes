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
package org.hibnet.webpipes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hibnet.webpipes.processor.ProcessorPipeline;
import org.hibnet.webpipes.processor.ResourceProcessor;
import org.hibnet.webpipes.resource.Resource;
import org.hibnet.webpipes.resource.ResourceFactory;
import org.hibnet.webpipes.resource.ResourceRefresher;

public class Webpipe {

    private final List<Resource> resources = Collections.synchronizedList(new ArrayList<Resource>());

    private ResourceRefresher resourceRefresher;

    private ResourceFactory resourceFactory;

    private ProcessorPipeline processorPipeline = new ProcessorPipeline();

    private volatile boolean firstFetch = true;

    private WebpipeCache webpipeCache = null;

    private volatile List<String> contents;

    private List<String> paths;

    public Webpipe(ResourceFactory resourceFactory, ResourceRefresher resourceRefresher, String... paths) {
        this.resourceFactory = resourceFactory;
        this.resourceRefresher = resourceRefresher;
        if (resourceRefresher != null) {
            resourceRefresher.addWebpipe(this);
        }
        this.paths = Arrays.asList(paths);
    }

    public void setWebpipeCache(WebpipeCache webpipeCache) {
        this.webpipeCache = webpipeCache;
    }

    public void setProcessorPipeline(ProcessorPipeline pipeline) {
        this.processorPipeline = pipeline;
    }

    public List<Resource> getResources() {
        return new ArrayList<Resource>(resources);
    }

    public List<String> getPaths() {
        return paths;
    }

    public List<String> getContents() throws IOException {
        if (contents == null) {
            synchronized (this) {
                if (contents == null) {
                    if (firstFetch) {
                        // content is null and first fetch, then try to load from disc
                        if (webpipeCache != null) {
                            contents = webpipeCache.getContents(this);
                        }
                        firstFetch = false;
                    }
                    if (contents == null) {
                        contents = fetchContents();
                    }
                    if (webpipeCache != null) {
                        webpipeCache.storeContents(this, contents);
                    }
                }
            }
        }
        return contents;
    }

    protected List<String> fetchContents() throws IOException {
        if (processorPipeline == null) {
            return null;
        }
        try {
            return processorPipeline.buildContents(getResources());
        } catch (Exception e) {
            throw new IOException("Failure of the pipeline of processors", e);
        }
    }

    public synchronized void invalidateCachedContent() {
        contents = null;
    }

    public synchronized Webpipe addResource(Resource resource) {
        if (resourceRefresher != null) {
            resourceRefresher.addResource(resource);
        }
        resources.add(resource);
        invalidateCachedContent();
        return this;
    }

    public void addResource(String resource) {
        if (resourceFactory == null) {
            throw new IllegalStateException("Unconfigured resource factory");
        }
        int i = resource.indexOf(":");
        if (i < 0) {
            throw new IllegalArgumentException();
        }
        String type = resource.substring(0, i);
        String path = resource.substring(i + 1);
        addResource(resourceFactory.get(type, path));
    }

    public synchronized void setResources(List<String> resources) {
        this.resources.clear();
        for (String resource : resources) {
            addResource(resource);
        }
    }

    public void setResource(String resource) {
        setResources(Collections.singletonList(resource));
    }

    public void setPreProcessors(List<ResourceProcessor> preProcessors) {
        processorPipeline.setPreProcessors(preProcessors);
    }

    public void setPostProcessors(List<ResourceProcessor> postProcessors) {
        processorPipeline.setPostProcessors(postProcessors);
    }

    public void setPreProcessor(ResourceProcessor preProcessor) {
        setPreProcessors(Collections.singletonList(preProcessor));
    }

    public void setPostProcessor(ResourceProcessor postProcessor) {
        setPostProcessors(Collections.singletonList(postProcessor));
    }

}
