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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.LessCompiler.Problem;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.LessSource.StringSource;
import com.github.sommeri.less4j.core.DefaultLessCompiler;
import org.hibnet.webpipes.processor.ResourceProcessor;
import org.hibnet.webpipes.resource.Resource;

/**
 * Yet another processor which compiles less to css. This implementation uses open source java library called less4j.
 */
public class Less4jProcessor extends ResourceProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(Less4jProcessor.class);

    /**
     * Required to use the less4j import mechanism.
     */
    private static class RelativeAwareLessSource extends StringSource {
        private final Resource resource;

        public RelativeAwareLessSource(Resource resource, String content) {
            super(content);
            this.resource = resource;
        }

        @Override
        public LessSource relativeSource(String relativePath) throws StringSourceException {
            try {
                Resource relativeResource = resource.resolve(relativePath);
                return new RelativeAwareLessSource(relativeResource, relativeResource.getContent());
            } catch (IOException e) {
                LOG.error("Failed to compute relative resource: {}", resource, e);
                throw new StringSourceException();
            }
        }
    }

    private final LessCompiler compiler = new DefaultLessCompiler();

    @Override
    public String process(Resource resource, String content) throws Exception {
        StringSource lessSource;
        if (resource != null) {
            lessSource = new RelativeAwareLessSource(resource, content);
        } else {
            lessSource = new StringSource(content);
        }
        CompilationResult result = compiler.compile(lessSource);
        logWarnings(result);
        content = result.getCss();
        return content;
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
