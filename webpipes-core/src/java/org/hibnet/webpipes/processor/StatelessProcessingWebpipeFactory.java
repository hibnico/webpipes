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
package org.hibnet.webpipes.processor;

import org.hibnet.webpipes.Webpipe;
import org.hibnet.webpipes.WebpipeOutput;
import org.hibnet.webpipes.WebpipeUtils;

/**
 * If stateless, the factory is also the webpipe itself
 */
public interface StatelessProcessingWebpipeFactory extends ProcessingWebpipeFactory {

    @Override
    default public Webpipe createProcessingWebpipe(String path, Webpipe source) {
        return new ProcessingWebpipe(WebpipeUtils.idOf(this.getClass(), source), path, getName(), source) {
            @Override
            protected WebpipeOutput fetchOutput() throws Exception {
                return process(getChildWebpipe());
            }
        };
    }

    public String getName();

    public WebpipeOutput process(Webpipe webpipe) throws Exception;

}
