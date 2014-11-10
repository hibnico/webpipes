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
package org.hibnet.webpipes.processor;

import java.io.Reader;
import java.io.Writer;

import org.hibnet.webpipes.resource.Resource;

/**
 * Perform actual processing of the resource content from the {@link Reader} and writes the processed content to the {@link Writer}. The processor
 * should focus only on transformation. If a processing fails, it is preferred to propagate the exception, because wro4j can allow custom behavior for
 * this situation. It is possible to treat the exceptions by leaving the processed output unchanged, but in this case you may not benefit of global
 * configuration.
 */
public abstract class ResourceProcessor {

    /**
     * Process a content supplied by a reader and perform some sort of processing. It is important to know that you should use reader for processing
     * instead of trying to access the resource original content using {@link Resource}, because this way you can ignore the other preProcessors from
     * the chain.<br/>
     * It is not require to explicitly handle exception. When the processing fails, the following can happen:
     * <ul>
     * <li>the exception is wrapped in {@link RuntimeException} and the processing chain is interrupted (by default)</li>
     * </ul>
     * <br/>
     * It is not required to close the reader and writers, because these will be closed for you.
     * 
     * @param resource the original resource as it found in the model.
     * @param reader {@link Reader} used to read processed resource content.
     * @param writer {@link Writer} where used to write processed results.
     */
    abstract public String process(Resource resource, String content) throws Exception;

    /**
     * Destroy this object or any other internal state created during initialization.
     *
     * @throws Exception if the destroy operation failed.
     */
    public void destroy() throws Exception {
        // nothing to do by default
    }

}
