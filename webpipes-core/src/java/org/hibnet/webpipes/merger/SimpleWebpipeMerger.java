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

import java.util.ArrayList;
import java.util.List;

import org.hibnet.jsourcemap.Position;
import org.hibnet.jsourcemap.Section;
import org.hibnet.jsourcemap.SourceMap;
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
        StringBuilder buffer = new StringBuilder();
        SourceMap sourceMap = new SourceMap();
        sourceMap.version = 3;
        sourceMap.file = "merged";
        sourceMap.sections = new ArrayList<>();
        Position pos = new Position(0, 0);
        for (Webpipe webpipe : webpipes) {
            WebpipeOutput output = webpipe.getOutput();

            String content = output.getContent();
            buffer.append(content);

            SourceMap subSourceMap = output.getSourceMap();
            if (subSourceMap != null) {
                Section section = new Section();
                section.map = output.getSourceMap();
                section.offset = new Position(pos.line, pos.column);
                sourceMap.sections.add(section);
            }

            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);
                if (c == '\n') {
                    pos.line++;
                    pos.column = 0;
                } else {
                    pos.column++;
                }
            }

            if (addNewLineOnMerge) {
                buffer.append("\n");
                pos.line++;
                pos.column = 0;
            }
        }

        if (sourceMap.sections.isEmpty()) {
            sourceMap = null;
        }

        return new WebpipeOutput(buffer.toString(), sourceMap);
    }
}
