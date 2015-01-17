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
package org.hibnet.webpipes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hibnet.webpipes.resource.Resource;

public class WebpipeCache {

    private Charset UTF8 = Charset.forName("UTF-8");

    private File cacheDir;

    public WebpipeCache(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    public List<String> getContents(Webpipe pipe) throws IOException {
        File sha1File = new File(cacheDir, pipe.getPaths().get(0) + ".sha1");
        if (!sha1File.exists()) {
            return null;
        }

        byte[] expectedSha1 = computeSHA1(pipe);

        if (sha1File.length() != expectedSha1.length) {
            // wrong size : don't even bother to read it
            return null;
        }
        byte[] actualSha1 = readFile(sha1File);

        if (!Arrays.equals(expectedSha1, actualSha1)) {
            return null;
        }

        // same sha1, get the content out
        List<String> contents = new ArrayList<>();
        for (String path : pipe.getPaths()) {
            byte[] data = readFile(new File(cacheDir, path));
            contents.add(new String(data, UTF8));
        }
        return contents;
    }

    private byte[] readFile(File file) throws IOException, FileNotFoundException {
        int size = (int) file.length();
        byte[] data = new byte[size];
        int offset = 0;
        int readed = 0;
        try (InputStream in = new FileInputStream(file)) {
            while (offset < size && (readed = in.read(data, offset, size - offset)) != -1) {
                offset += readed;
            }
        }
        return data;
    }

    public void storeContents(Webpipe webpipe, List<String> contents) throws IOException {
        byte[] sha1 = computeSHA1(webpipe);

        try (OutputStream out = new FileOutputStream(new File(cacheDir, webpipe.getPaths().get(0) + ".sha1"))) {
            out.write(sha1);
        }

        for (int i = 0; i < webpipe.getPaths().size(); i++) {
            String path = webpipe.getPaths().get(i);
            path = path.replaceAll("/", File.separator).replaceAll("\\", File.separator);
            try (OutputStream out = new FileOutputStream(new File(cacheDir, path))) {
                out.write(contents.get(i).getBytes(UTF8));
            }
        }
    }

    private byte[] computeSHA1(Webpipe webpipe) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 is not supported", e);
        }
        for (String path : webpipe.getPaths()) {
            digest.update(path.getBytes(UTF8));
            digest.update((byte) '\n');
        }
        for (Resource r : webpipe.getResources()) {
            digest.update(r.getContent().getBytes(UTF8));
            digest.update((byte) '\n');
        }
        byte[] sha1 = digest.digest();
        return sha1;
    }

}
