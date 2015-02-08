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

public class WebJarHelper {

    private static WebJarAssetLocator webJarAssetLocator = new WebJarAssetLocator(WebJarAssetLocator.getFullPathIndex(Pattern.compile(".*"),
            WebJarHelper.class.getClassLoader()));

    public static WebJarAssetLocator getWebJarAssetLocator() {
        return webJarAssetLocator;
    }

    public static Resource getResource(String path) {
        return new ClasspathResource(webJarAssetLocator.getFullPath(path));
    }

}
