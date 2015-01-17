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
package org.hibnet.webpipes.processor.sass;

import org.hibnet.webpipes.processor.ResourceProcessor;
import org.hibnet.webpipes.resource.Resource;
import org.jruby.Ruby;
import org.jruby.ast.Node;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.ByteList;

public class RubySassCssProcessor extends ResourceProcessor {

    private static final String[] REQUIRES = { "rubygems", "sass/plugin", "sass/engine" };

    @Override
    public String process(Resource resource, String content) throws Exception {
        StringBuilder script = new StringBuilder();
        for (String require : REQUIRES) {
            script.append("  require '" + require + "'\n");
        }
        script.append("result = Sass::Engine.new(\"");

        for (int i = 0; i < content.length(); i++) {
            int code = content.codePointAt(i);
            if (code < 0x80) {
                // We leave only ASCII unchanged.
                char c = content.charAt(i);
                if (c == '\\') {
                    script.append("\\\\");
                } else if (code == '"') {
                    script.append("\\\"");
                } else if (code == '#') {
                    script.append("\\#");
                } else {
                    script.append(c);
                }
            } else {
                // Non-ASCII String may cause invalid multibyte char (US-ASCII) error with Ruby 1.9
                // because Ruby 1.9 expects you to use ASCII characters in your source code.
                // Instead we use Unicode code point representation which is usable with
                // Ruby 1.9 and later. Inspired from
                // http://www.stefanwille.com/2010/08/ruby-on-rails-fix-for-invalid-multibyte-char-us-ascii/
                script.append(String.format("\\u%04x", code));
            }
        }

        script.append("\", {:syntax => :scss}).render");

        Ruby runtime = Ruby.newInstance();
        Node node = runtime.parse(ByteList.create(script), resource.getName(), runtime.getCurrentContext().getCurrentScope(), 0, false);
        IRubyObject result = runtime.runNormally(node);
        return result.toString();
    }

}
