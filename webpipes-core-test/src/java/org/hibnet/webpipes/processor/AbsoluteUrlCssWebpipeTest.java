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

import static junit.framework.Assert.assertEquals;

import org.hibnet.webpipes.resource.StringResource;
import org.junit.Test;

public class AbsoluteUrlCssWebpipeTest {


    @Test
    public void testEmpty() throws Exception {
        StringResource data = new StringResource("test", "");
        AbsoluteUrlCssWebpipe pipe = new AbsoluteUrlCssWebpipe(data, "/absolute/");
        assertEquals("", pipe.fetchContent().getMain());
    }

    @Test
    public void testNoUrl() throws Exception {
        StringResource data = new StringResource("test", ".item { color: white; }");
        AbsoluteUrlCssWebpipe pipe = new AbsoluteUrlCssWebpipe(data, "/absolute/");
        assertEquals(".item { color: white; }", pipe.fetchContent().getMain());
    }

    @Test
    public void testUrl() throws Exception {
        StringResource data = new StringResource("test", ".item { background-image: url(image/test.png); }");
        AbsoluteUrlCssWebpipe pipe = new AbsoluteUrlCssWebpipe(data, "/absolute/");
        assertEquals(".item { background-image: url(/absolute/image/test.png); }", pipe.fetchContent().getMain());
    }

    @Test
    public void testSpaces() throws Exception {
        StringResource data = new StringResource("test", ".item { background-image: url( image/test.png  ); }");
        AbsoluteUrlCssWebpipe pipe = new AbsoluteUrlCssWebpipe(data, "/absolute/");
        assertEquals(".item { background-image: url( /absolute/image/test.png  ); }", pipe.fetchContent().getMain());
        data = new StringResource("test", ".item { background-image: url  (image/test.png); }");
        pipe = new AbsoluteUrlCssWebpipe(data, "/absolute/");
        assertEquals(".item { background-image: url  (/absolute/image/test.png); }", pipe.fetchContent().getMain());
    }

    @Test
    public void testQuotedUrl() throws Exception {
        StringResource data = new StringResource("test", ".item { background-image: url('image/test.png'); }");
        AbsoluteUrlCssWebpipe pipe = new AbsoluteUrlCssWebpipe(data, "/absolute/");
        assertEquals(".item { background-image: url('/absolute/image/test.png'); }", pipe.fetchContent().getMain());

        data = new StringResource("test", ".item { background-image: url(\"image/test.png\"); }");
        pipe = new AbsoluteUrlCssWebpipe(data, "/absolute/");
        assertEquals(".item { background-image: url(\"/absolute/image/test.png\"); }", pipe.fetchContent().getMain());
    }
}
