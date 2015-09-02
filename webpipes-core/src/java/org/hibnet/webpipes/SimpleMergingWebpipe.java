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
import java.util.Base64;
import java.util.List;

public class SimpleMergingWebpipe extends MergingWebpipe {

    private List<Webpipe> webpipes;

    public SimpleMergingWebpipe(String path, Webpipe... webpipes) {
        this(path, Arrays.asList(webpipes));
    }

    public SimpleMergingWebpipe(String path, List<Webpipe> webpipes) {
        super(WebpipeUtils.idOf(SimpleMergingWebpipe.class, webpipes), WebpipeUtils.pathOf(path, "/webpipes", buildId(webpipes)));
        this.webpipes = webpipes;
    }

    private static String buildId(List<Webpipe> webpipes) {
        if (webpipes.isEmpty()) {
            return "empty";
        }
        MessageDigest digest = WebpipeUtils.buildSHA1Digest();
        for (Webpipe webpipe : webpipes) {
            digest.update(webpipe.getPath().getBytes(WebpipeUtils.UTF8));
        }
        return Base64.getEncoder().encodeToString(digest.digest());
    }

    @Override
    protected List<Webpipe> buildChildrenList() throws IOException {
        return webpipes;
    }

}
