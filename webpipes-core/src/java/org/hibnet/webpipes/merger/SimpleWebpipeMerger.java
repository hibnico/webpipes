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
package org.hibnet.webpipes.merger;

import java.util.List;

import org.hibnet.webpipes.Webpipe;
import org.hibnet.webpipes.WebpipeOutput;

public class SimpleWebpipeMerger implements WebpipeMerger {

    private boolean addNewLineOnMerge = true;

    public boolean isAddNewLineOnMerge() {
        return addNewLineOnMerge;
    }

    public void setAddNewLineOnMerge(boolean addNewLineOnMerge) {
        this.addNewLineOnMerge = addNewLineOnMerge;
    }

    @Override
    public WebpipeOutput merge(List<Webpipe> webpipes) throws Exception {
        StringBuilder buffer = new StringBuilder();
        for (Webpipe webpipe : webpipes) {
            buffer.append(webpipe.getContent().getMain());
            if (addNewLineOnMerge) {
                buffer.append("\n");
            }
        }
        return new WebpipeOutput(buffer.toString(), null);
    }
}
