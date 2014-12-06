/*
 *  Copyright 2014 WebPipes contributors
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResourceFactory {

    private Map<String, TypedResourceFactory> factories = new ConcurrentHashMap<>();

    private Map<String, Map<String, Resource>> cache = new ConcurrentHashMap<>();

    private ResourceRefresher resourceRefresher = null;

    public ResourceFactory() {
        this(true);
    }

    public ResourceFactory(boolean addDefaults) {
        if (addDefaults) {
            addDefaults();
        }
    }

    public void addDefaults() {
        register(new WebJarResourceFactory());
        register(new ClasspathResourceFactory());
    }

    public void setFactories(Map<String, TypedResourceFactory> factories) {
        this.factories.putAll(factories);
    }

    public void register(TypedResourceFactory factory) {
        factories.put(factory.getType(), factory);
    }

    public void setResourceRefresher(ResourceRefresher resourceRefresher) {
        this.resourceRefresher = resourceRefresher;
    }

    public Resource get(String type, String path) {
        Map<String, Resource> resources = cache.get(type);
        if (resources == null) {
            resources = new ConcurrentHashMap<>();
            cache.put(path, resources);
        }
        Resource resource = resources.get(path);
        if (resource == null) {
            TypedResourceFactory factory = factories.get(type);
            if (factory == null) {
                throw new IllegalArgumentException("Unknown resource factory type '" + type + "'");
            }
            resource = factory.get(path);
            resources.put(path, resource);
            if (resourceRefresher != null) {
                resourceRefresher.addResource(resource);
            }
        }
        return resource;
    }

}
