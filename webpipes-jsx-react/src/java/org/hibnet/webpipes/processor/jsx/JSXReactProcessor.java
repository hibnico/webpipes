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
package org.hibnet.webpipes.processor.jsx;

import java.util.Map;

import org.hibnet.webpipes.Webpipe;
import org.hibnet.webpipes.WebpipeOutput;
import org.hibnet.webpipes.js.StatelessJsProcessor;

public class JSXReactProcessor extends StatelessJsProcessor {

    public JSXReactProcessor() {
        super("jsx");
    }

    @Override
    protected void initEngine() throws Exception {
        addClientSideEnvironment();
        evalFromWebjar("JSXTransformer.js");
    }

    @Override
    public WebpipeOutput process(Webpipe webpipe) throws Exception {
        WebpipeOutput output = webpipe.getOutput();
        Map<String, Object> res = invokeMethod("JSXTransformer", "transform", output.getContent());
        String content = (String) res.get("code");
        return new WebpipeOutput(content, output.getSourceMap());
    }

}
