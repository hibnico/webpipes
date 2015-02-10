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

import java.net.URI;
import java.security.MessageDigest;

import org.hibnet.webpipes.Webpipe;
import org.hibnet.webpipes.WebpipeOutput;
import org.hibnet.webpipes.WebpipeUtils;

/**
 * Encapsulates information about a resource. This class is not final because we need to mock it in unit tests.
 */
public abstract class Resource extends Webpipe {

    abstract public Resource resolve(String relativePath);

    abstract public URI getURI();

    @Override
    public String getId() {
        return getURI().toASCIIString();
    }

    @Override
    public void updateDigest(MessageDigest digest) throws Exception {
        WebpipeOutput content = getContent();
        digest.digest(content.getMain().getBytes(WebpipeUtils.UTF8));
    }

}
