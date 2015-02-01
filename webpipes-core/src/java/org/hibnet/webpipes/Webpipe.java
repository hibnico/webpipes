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
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Webpipe {

    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private static final String NO_CONTENT = "";

    private static final String INVALIDATED_CONTENT = "";

    protected Charset UTF8 = Charset.forName("UTF-8");

    private File cacheDir;

    private volatile String content = NO_CONTENT;

    public abstract String getId();

    public abstract boolean refresh() throws IOException;

    public void setCacheDir(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    protected void invalidateCache() {
        synchronized (content) {
            content = INVALIDATED_CONTENT;
        }
    }

    public final String getContent() throws Exception {
        if (content == NO_CONTENT || content == INVALIDATED_CONTENT) {
            synchronized (content) {
                if (content == NO_CONTENT) {
                    content = readContent();
                    if (content == NO_CONTENT) {
                        content = fetchContent();
                        storeContent(content);
                    }
                } else if (content == INVALIDATED_CONTENT) {
                    content = fetchContent();
                    storeContent(content);
                }
            }
        }
        return content;
    }

    abstract protected String fetchContent() throws Exception;

    private String readContent() throws Exception {
        if (cacheDir == null) {
            // no durable cache configured
            return NO_CONTENT;
        }

        File sha1File = new File(cacheDir, getId() + ".sha1");
        if (!sha1File.exists()) {
            return NO_CONTENT;
        }

        byte[] expectedSha1 = computeSHA1();

        if (sha1File.length() != expectedSha1.length) {
            // wrong size : don't even bother to read it
            return NO_CONTENT;
        }
        byte[] actualSha1 = readFile(sha1File);

        if (!Arrays.equals(expectedSha1, actualSha1)) {
            return NO_CONTENT;
        }

        // same sha1, get the content out
        byte[] data = readFile(new File(cacheDir, getId() + ".txt"));
        return new String(data, UTF8);
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

    private void storeContent(String content) throws Exception {
        if (cacheDir == null) {
            // no durable cache configured
            return;
        }

        byte[] sha1 = computeSHA1();

        try (OutputStream out = new FileOutputStream(new File(cacheDir, getId() + ".sha1"))) {
            out.write(sha1);
        }

        try (OutputStream out = new FileOutputStream(new File(cacheDir, getId() + ".txt"))) {
            out.write(content.getBytes(UTF8));
        }
    }

    private byte[] computeSHA1() throws Exception {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 is not supported", e);
        }
        updateDigest(digest);
        byte[] sha1 = digest.digest();
        return sha1;
    }

    protected abstract void updateDigest(MessageDigest digest) throws Exception;

}
