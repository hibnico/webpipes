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
package org.hibnet.webpipes.resource;

import java.io.IOException;
import java.net.URI;

import org.hibnet.webpipes.WebpipeOutput;
import org.hibnet.webpipes.WebpipeUtils;

public class StringResource extends Resource {

    private WebpipeOutput content;

    private String name;

    private URI uri;

    public StringResource(String name, String content) {
        this.name = name;
        this.content = new WebpipeOutput(content);
        this.uri = URI.create("string:" + WebpipeUtils.sha1HexEncoded(content));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public Resource resolve(String relativePath) {
        return null;
    }

    @Override
    public WebpipeOutput fetchContent() throws IOException {
        return content;
    }

    @Override
    public boolean refresh() throws IOException {
        return false;
    }

}
