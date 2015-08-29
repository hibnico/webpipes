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
package org.hibnet.webpipes.css;

import java.util.ArrayList;
import java.util.List;

public class CssSpliter {

    public List<String> split(String content) throws Exception {
        List<String> result = new ArrayList<>();

        StringBuilder current = null;
        int count = 0;
        int multiplier = 1;
        boolean inRule = false;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') {
                inRule = true;
            }
            if (c == ',' && !inRule) {
                multiplier++;
            }
            if (c == '}') {
                count += multiplier;
                multiplier = 1;
                inRule = false;
            }
            if (count >= 4000 || current == null) {
                // split
                if (current != null) {
                    result.add(current.toString());
                }
                current = new StringBuilder();
                count = 0;
            }
            current.append(c);
        }

        return result;
    }
}
