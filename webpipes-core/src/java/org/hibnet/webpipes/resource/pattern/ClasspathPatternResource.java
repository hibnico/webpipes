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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.hibnet.webpipes.MergingWebpipe;
import org.hibnet.webpipes.Webpipe;
import org.hibnet.webpipes.resource.Resource;

public class ClasspathPatternResource extends MergingWebpipe {

    private PathMatcher pathMatcher = new AntPathMatcher();

    private String pattern;

    public ClasspathPatternResource(String pattern) {
        super("/webpipes/cps/" + pattern);
        this.pattern = pattern;
    }

    public void setPathMatcher(PathMatcher pathMatcher) {
        this.pathMatcher = pathMatcher;
    }

    @Override
    protected List<Webpipe> buildChildrenList() throws IOException {
        List<Resource> resources = PatternHelper.getClasspathResources(pathMatcher, null, pattern);
        return new ArrayList<Webpipe>(resources);
    }

}
