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

import org.hibnet.jsourcemap.SourceMap;

public class WebpipeOutput {

    private String content;

    private SourceMap sourceMap;

    public WebpipeOutput(String content) {
        this.content = content;
    }

    public WebpipeOutput(String content, SourceMap sourceMap) {
        this.content = content;
        this.sourceMap = sourceMap;
    }

    public String getContent() {
        return content;
    }

    public SourceMap getSourceMap() {
        return sourceMap;
    }
}
