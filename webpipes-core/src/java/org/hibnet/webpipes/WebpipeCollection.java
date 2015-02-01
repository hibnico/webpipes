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
import java.util.ArrayList;
import java.util.List;

public abstract class WebpipeCollection extends Webpipe {

    private List<Webpipe> NO_WEBPIPES = new ArrayList<>();

    private List<Webpipe> webpipes;

    private List<Webpipe> getWebpipes() {
        if (webpipes == NO_WEBPIPES) {
            synchronized (webpipes) {
                if (webpipes == NO_WEBPIPES) {
                    webpipes = buildCollection();
                }
            }
        }
        return webpipes;
    }

    abstract protected List<Webpipe> buildCollection();

    @Override
    public boolean refresh() throws IOException {
        boolean refresh = false;
        for (Webpipe webpipe : getWebpipes()) {
            refresh = refresh || webpipe.refresh();
        }
        refresh = refresh || refreshCollection();
        if (refresh) {
            invalidateCache();
            synchronized (webpipes) {
                webpipes = NO_WEBPIPES;
            }
        }
        return refresh;
    }

    abstract protected boolean refreshCollection();

    @Override
    protected void updateDigest(MessageDigest digest) throws Exception {
        for (Webpipe webpipe : getWebpipes()) {
            webpipe.updateDigest(digest);
            digest.update((byte) '\n');
        }
    }

    @Override
    protected String fetchContent() throws Exception {
        StringBuilder buffer = new StringBuilder();
        for (Webpipe webpipe : getWebpipes()) {
            buffer.append(webpipe.getContent());
            buffer.append("\n");
        }
        return buffer.toString();
    }

}
