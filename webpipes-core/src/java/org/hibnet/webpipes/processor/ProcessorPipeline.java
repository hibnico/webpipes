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
package org.hibnet.webpipes.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibnet.webpipes.resource.Resource;

public class ProcessorPipeline {

    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private List<ResourceProcessor> preProcessors = new ArrayList<>();

    private List<ResourceProcessor> postProcessors = new ArrayList<>();

    private boolean ignoreMissingResources = false;

    private boolean ignoreFailingProcessor = false;

    public void addPreProcessors(ResourceProcessor preProcessor) {
        this.preProcessors.add(preProcessor);
    }

    public void addPostProcessors(ResourceProcessor postProcessor) {
        this.postProcessors.add(postProcessor);
    }

    public void setPreProcessors(List<ResourceProcessor> preProcessors) {
        this.preProcessors = preProcessors;
    }

    public void setPostProcessors(List<ResourceProcessor> postProcessors) {
        this.postProcessors = postProcessors;
    }

    public void setIgnoreMissingResources(boolean ignoreMissingResources) {
        this.ignoreMissingResources = ignoreMissingResources;
    }

    public void setIgnoreFailingProcessor(boolean ignoreFailingProcessor) {
        this.ignoreFailingProcessor = ignoreFailingProcessor;
    }

    public List<String> buildContents(List<Resource> resources) throws Exception {
        String merged = processAndMerge(resources);
        String result = applyPostProcessors(merged);
        return Arrays.asList(result);
    }

    private String processAndMerge(List<Resource> resources) throws Exception {
        StringBuffer result = new StringBuffer();
        for (Resource resource : resources) {
            result.append(applyPreProcessors(resource));
        }
        return result.toString();
    }

    private String applyPreProcessors(Resource resource) throws Exception {
        String content = null;
        try {
            content = resource.getContent();
        } catch (IOException e) {
            if (ignoreMissingResources) {
                return "";
            } else {
                throw e;
            }
        }
        for (ResourceProcessor processor : preProcessors) {
            try {
                content = processor.process(resource, content);
            } catch (Exception e) {
                if (ignoreFailingProcessor) {
                    LOG.error("Exception while applying pre processor {} on the resource {}; ignoring it", processor.getClass().getSimpleName(),
                            resource, e);
                } else {
                    throw e;
                }
            }
            return content;
        }
        // add explicitly new line at the end to avoid unexpected comment issue
        return content + "\n";
    }

    private String applyPostProcessors(String content) throws Exception {
        for (ResourceProcessor processor : postProcessors) {
            try {
                content = processor.process(null, content);
            } catch (Exception e) {
                if (ignoreFailingProcessor) {
                    LOG.warn("Exception while applying post processor {} on the merged resource; ignoring it", processor.getClass().getSimpleName(),
                            e);
                } else {
                    throw e;
                }
            }
        }
        return content;
    }

}
