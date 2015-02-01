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

import java.util.List;

public class WebpipeMerger extends WebpipeCollection {

    private List<Webpipe> webpipes;

    private String id;

    public WebpipeMerger(List<Webpipe> webpipes) {
        this.webpipes = webpipes;

        StringBuilder buffer = new StringBuilder("merged");
        for (Webpipe webpipe : webpipes) {
            buffer.append("-");
            buffer.append(webpipe.getId());
        }
        id = buffer.toString();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    protected List<Webpipe> buildCollection() {
        return webpipes;
    }

    @Override
    protected boolean refreshCollection() {
        return false;
    }

}
