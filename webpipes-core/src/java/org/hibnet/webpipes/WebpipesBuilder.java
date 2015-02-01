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
package org.hibnet.webpipes;

import java.util.List;

import org.hibnet.webpipes.resource.Resource;
import org.hibnet.webpipes.resource.ResourceFactory;

public abstract class WebpipesBuilder {

    protected ResourceFactory resourceFactory = buildResourceFactory();

    private WebpipeRefresher webpipeRefresher;

    protected ResourceFactory buildResourceFactory() {
        return new ResourceFactory();
    }

    public void enableResourceRefresher() {
        webpipeRefresher = new WebpipeRefresher();
        webpipeRefresher.startWatcher();
    }

    abstract public List<Webpipe> buildWebpipes();

    protected Resource get(String type, String path) {
        return resourceFactory.get(type, path);
    }

}
