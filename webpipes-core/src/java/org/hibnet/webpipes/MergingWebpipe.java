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

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

public class MergingWebpipe extends Webpipe {

    private List<Webpipe> webpipes;

    private String id;

    public MergingWebpipe(Webpipe... webpipes) {
        this(Arrays.asList(webpipes));
    }

    public MergingWebpipe(List<Webpipe> webpipes) {
        this.webpipes = webpipes;

        StringBuilder buffer = new StringBuilder("merging");
        for (Webpipe webpipe : webpipes) {
            buffer.append("-");
            buffer.append(webpipe.getName());
        }
        id = buffer.toString();
    }

    @Override
    protected List<Webpipe> buildChildrenList() throws IOException {
        return webpipes;
    }

    @Override
    public String getName() {
        return "merging";
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    protected WebpipeOutput fetchContent() throws Exception {
        return fetchChildrenContent();
    }

    @Override
    public boolean refresh() throws IOException {
        return refreshChildren();
    }

    @Override
    public void updateDigest(MessageDigest digest) throws Exception {
        updateChildrenDigest(digest);
    }
}
