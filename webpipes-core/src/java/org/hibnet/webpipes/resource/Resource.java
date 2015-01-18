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

/**
 * Encapsulates information about a resource. This class is not final because we need to mock it in unit tests.
 */
public abstract class Resource {

    private volatile String content;

    public synchronized String getContent() throws IOException {
        if (content == null) {
            content = fetchContent();
        }
        return content;
    }

    public abstract String getName();

    public abstract String fetchContent() throws IOException;

    public abstract boolean refresh() throws IOException;

    @SuppressWarnings("unused")
    public Resource resolve(String relativePath) throws IOException {
        return null;
    }

    public synchronized void invalidateCachedContent() {
        content = null;
    }

}
