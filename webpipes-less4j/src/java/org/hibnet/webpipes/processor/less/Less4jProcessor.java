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
package org.hibnet.webpipes.processor.less;

import org.hibnet.webpipes.Webpipe;
import org.hibnet.webpipes.processor.ProcessingWebpipeFactory;

/**
 * Yet another processor which compiles less to css. This implementation uses open source java library called less4j.
 */
public class Less4jProcessor implements ProcessingWebpipeFactory {

    @Override
    public Webpipe createProcessingWebpipe(Webpipe source) {
        return new Less4jWebpipe(source);
    }

}
