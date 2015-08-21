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
package org.hibnet.webpipes.processor.less;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.hibnet.webpipes.Webpipe;
import org.hibnet.webpipes.WebpipeOutput;
import org.hibnet.webpipes.WebpipeUtils;
import org.hibnet.webpipes.processor.ProcessingWebpipe;
import org.hibnet.webpipes.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.LessCompiler.Problem;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.LessSource.StringSource;
import com.github.sommeri.less4j.core.DefaultLessCompiler;

public class Less4jWebpipe extends ProcessingWebpipe {

    private static final Logger LOG = LoggerFactory.getLogger(Less4jProcessor.class);

    private static final LessCompiler compiler = new DefaultLessCompiler();

    private List<Resource> importedResources = new ArrayList<>();

    /**
     * Required to use the less4j import mechanism.
     */
    private class RelativeAwareLessSource extends StringSource {

        private Resource resource;

        public RelativeAwareLessSource(Resource resource, String content) {
            super(content);
            this.resource = resource;
        }

        @Override
        public LessSource relativeSource(String relativePath) throws StringSourceException {
            try {
                Resource relativeResource = resource.resolve(relativePath);
                importedResources.add(relativeResource);
                return new RelativeAwareLessSource(relativeResource, relativeResource.getOutput().getContent());
            } catch (Exception e) {
                LOG.error("Failed to compute relative resource: {}", resource, e);
                throw new StringSourceException();
            }
        }
    }

    public Less4jWebpipe(Webpipe webpipe) {
        super(webpipe);
    }

    @Override
    protected WebpipeOutput fetchContent() throws Exception {
        synchronized (importedResources) {
            importedResources.clear();

            String content = webpipe.getOutput().getContent();
            StringSource lessSource;
            if (webpipe instanceof Resource) {
                lessSource = new RelativeAwareLessSource((Resource) webpipe, content);
            } else {
                lessSource = new StringSource(content);
            }
            CompilationResult result = compiler.compile(lessSource);
            logWarnings(result);
            return new WebpipeOutput(result.getCss(), WebpipeUtils.parseSourceMap(result.getSourceMap()));
        }
    }

    @Override
    public boolean refresh() throws IOException {
        boolean needUpdate = refreshChildren();
        needUpdate = needUpdate || webpipe.refresh();
        if (needUpdate) {
            invalidateContentCache();
        }
        return needUpdate;
    }

    @Override
    protected List<Webpipe> buildChildrenList() throws IOException {
        synchronized (importedResources) {
            return new ArrayList<Webpipe>(importedResources);
        }
    }

    private void logWarnings(CompilationResult result) {
        if (!result.getWarnings().isEmpty()) {
            LOG.warn("Less warnings are:");
            for (Problem problem : result.getWarnings()) {
                LOG.warn(problemAsString(problem));
            }
        }
    }

    private String problemAsString(Problem problem) {
        return String.format("%s:%s %s.", problem.getLine(), problem.getCharacter(), problem.getMessage());
    }
}
