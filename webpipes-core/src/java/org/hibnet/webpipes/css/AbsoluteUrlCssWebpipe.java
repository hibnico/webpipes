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
package org.hibnet.webpipes.css;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibnet.webpipes.Webpipe;
import org.hibnet.webpipes.WebpipeOutput;
import org.hibnet.webpipes.processor.ProcessingWebpipe;

public class AbsoluteUrlCssWebpipe extends ProcessingWebpipe {

    private static final Pattern URL_PATTERN = Pattern.compile("url\\s*\\(\\s*['\"]?");

    private String absolutePath;

    public AbsoluteUrlCssWebpipe(Webpipe webpipe, String absolutePath) {
        super("absoluteUrlCss", webpipe);
        this.absolutePath = absolutePath;
    }

    @Override
    protected WebpipeOutput fetchOutput() throws Exception {
        WebpipeOutput output = getChildWebpipe().getOutput();
        String content = output.getContent();
        StringBuilder buffer = new StringBuilder();
        Matcher matcher = URL_PATTERN.matcher(content);
        int pos = 0;
        while (pos < content.length()) {
            if (!matcher.find(pos)) {
                break;
            }
            buffer.append(content.substring(pos, matcher.end()));
            buffer.append(absolutePath);
            pos = matcher.end();
        }
        buffer.append(content.substring(pos));
        return new WebpipeOutput(buffer.toString(), output.getSourceMap());
    }

}
