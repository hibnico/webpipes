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
package org.hibnet.webpipes.processor.yui;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.hibnet.webpipes.processor.ResourceProcessor;
import org.hibnet.webpipes.resource.Resource;

/**
 * YUICssCompressorProcessor. Use YUI css compression utility for processing a css resource.
 */
public class YUICssCompressorProcessor extends ResourceProcessor {

    /**
     * An option of CssCompressor.
     */
    private static final int linebreakpos = -1;

    @Override
    public String process(Resource resource, String content) throws IOException {
        Writer writer = new StringWriter();
        try {
            YuiCssCompressor compressor = new YuiCssCompressor(new StringReader(content));
            compressor.compress(writer, linebreakpos);
            content = writer.toString();
        } finally {
            writer.close();
        }
        return content;
    }
}
