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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

public class UrlResource extends StreamResource {

    private int timeout;

    private URL url;

    public UrlResource(URL url) {
        this.url = url;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public String getName() {
        return url.toExternalForm();
    }

    @Override
    public InputStream fetchStream() throws IOException {
        URLConnection connection = url.openConnection();
        // avoid jar file locking on Windows.
        connection.setUseCaches(false);

        // setting these timeouts ensures the client does not deadlock indefinitely
        // when the server has problems.
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);

        return new BufferedInputStream(connection.getInputStream());
    }

    @Override
    public boolean refresh() throws IOException {
        return false;
    }

    @Override
    public Resource resolve(String relativePath) throws IOException {
        try {
            return new UrlResource(url.toURI().resolve(relativePath).toURL());
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
