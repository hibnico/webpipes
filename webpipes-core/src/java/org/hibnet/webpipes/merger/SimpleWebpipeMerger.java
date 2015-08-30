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
package org.hibnet.webpipes.merger;

import java.util.List;

import org.hibnet.jsourcemap.Code;
import org.hibnet.jsourcemap.SourceMap;
import org.hibnet.jsourcemap.SourceMapConsumer;
import org.hibnet.jsourcemap.SourceNode;
import org.hibnet.webpipes.Webpipe;
import org.hibnet.webpipes.WebpipeOutput;

public class SimpleWebpipeMerger implements WebpipeMerger {

    private boolean addNewLineOnMerge = true;

    public boolean isAddNewLineOnMerge() {
        return addNewLineOnMerge;
    }

    public void setAddNewLineOnMerge(boolean addNewLineOnMerge) {
        this.addNewLineOnMerge = addNewLineOnMerge;
    }

    @Override
    public WebpipeOutput merge(List<Webpipe> webpipes) throws Exception {
        SourceNode sourceNode = new SourceNode();

        for (Webpipe webpipe : webpipes) {
            WebpipeOutput output = webpipe.getOutput();

            String content = output.getContent();

            SourceMap subSourceMap = output.getSourceMap();
            if (subSourceMap != null) {
                SourceMapConsumer sourceMapConsumer = SourceMapConsumer.create(subSourceMap);
                sourceNode.add(SourceNode.fromStringWithSourceMap(content, sourceMapConsumer, null));
            } else {
                int line = 1;
                int lastPos = -1;
                for (int i = 0; i < content.length(); i++) {
                    char c = content.charAt(i);
                    if (c == '\n') {
                        sourceNode.add(new SourceNode(line, 0, webpipe.getName(), content.substring(lastPos + 1, i + 1), null));
                        lastPos = i;
                        line++;
                    }
                }

            }

            if (addNewLineOnMerge) {
                sourceNode.add(new SourceNode(1, 0, webpipe.getName(), "\n", null));
            }

            sourceNode.setSourceContent(webpipe.getName(), content);
        }

        Code code = sourceNode.toStringWithSourceMap(null, null);
        return new WebpipeOutput(code.getSource(), code.getMap().toJSON());
    }
}
