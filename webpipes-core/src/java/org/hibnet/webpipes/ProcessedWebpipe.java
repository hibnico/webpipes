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
import java.util.List;

import org.hibnet.webpipes.processor.WebpipeProcessor;

public class ProcessedWebpipe extends Webpipe {

    private List<WebpipeProcessor> processors;

    private Webpipe webpipe;

    private boolean ignoreFailingProcessor = false;

    private String id;

    public ProcessedWebpipe(Webpipe webpipe, List<WebpipeProcessor> processors) {
        this.webpipe = webpipe;
        this.processors = processors;

        StringBuilder buffer = new StringBuilder();
        for (WebpipeProcessor processor : processors) {
            buffer.append(processor.getClass().getSimpleName());
            buffer.append("-");
        }
        buffer.append(webpipe.getName());
        id = buffer.toString();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return webpipe.getName();
    }

    @Override
    protected void updateDigest(MessageDigest digest) throws Exception {
        webpipe.updateDigest(digest);
    }

    @Override
    public boolean refresh() throws IOException {
        boolean needUpdate = webpipe.refresh();
        invalidateContentCache();
        return needUpdate;
    }

    @Override
    protected String fetchContent() throws Exception {
        String content = webpipe.getContent();
        for (WebpipeProcessor processor : processors) {
            try {
                content = processor.process(webpipe, content);
            } catch (Exception e) {
                if (ignoreFailingProcessor) {
                    LOG.error("Exception while applying pre processor {} on the webpipe {}; ignoring it", processor.getClass().getSimpleName(),
                            webpipe.getId(), e);
                } else {
                    throw e;
                }
            }
        }
        return content;
    }
}
