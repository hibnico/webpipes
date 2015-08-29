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
package org.hibnet.webpipes.resource.pattern;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.hibnet.webpipes.Webpipe;
import org.hibnet.webpipes.WebpipeOutput;
import org.hibnet.webpipes.resource.Resource;

public class FilePatternResource extends Resource {

    private String pattern;

    private PathMatcher pathMatcher = new AntPathMatcher();

    private URI uri;

    public FilePatternResource(String pattern) {
        this.pattern = pattern;
        this.uri = URI.create("files:" + pattern);
    }

    public void setPathMatcher(PathMatcher pathMatcher) {
        this.pathMatcher = pathMatcher;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Resource resolve(String relativePath) {
        return null;
    }

    @Override
    public boolean refresh() throws IOException {
        return refreshChildren();
    }

    @Override
    protected WebpipeOutput fetchOutput() throws Exception {
        return fetchChildrenOutput();
    }

    @Override
    protected List<Webpipe> buildChildrenList() throws IOException {
        List<Webpipe> children = new ArrayList<>();

        String rootPath = PatternHelper.determineRootPath(pathMatcher, pattern);
        String subPattern = pattern.substring(rootPath.length());

        File root = new File(rootPath);
        children.addAll(PatternHelper.getFilesystemResources(pathMatcher, root, subPattern));

        return children;
    }
}
