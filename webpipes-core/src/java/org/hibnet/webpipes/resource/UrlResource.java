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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

public class UrlResource extends StreamResource {

    private int timeout;

    private URL url;

    private String name;

    private URI uri;

    public UrlResource(URL url) {
        this.url = url;
        this.name = url.getPath().substring(url.getPath().lastIndexOf("/"));
        try {
            this.uri = url.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
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
        try {
            return new UrlResource(new URL(this.url, relativePath));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
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

}
