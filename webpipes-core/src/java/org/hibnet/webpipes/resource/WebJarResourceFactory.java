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

import java.util.regex.Pattern;

import org.webjars.WebJarAssetLocator;

public class WebJarResourceFactory implements TypedResourceFactory {

    public static final String TYPE = "webjar";

    private WebJarAssetLocator webJarAssetLocator = new WebJarAssetLocator(WebJarAssetLocator.getFullPathIndex(Pattern.compile(".*"),
            WebJarResourceFactory.class.getClassLoader()));

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Resource get(String path) {
        return new ClasspathResource(webJarAssetLocator.getFullPath(path));
    }

}
