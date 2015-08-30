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
import java.security.MessageDigest;
import java.util.Arrays;

import org.hibnet.jsourcemap.SourceMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Webpipe {

    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private static final WebpipeOutput NOT_INITIALIZED_OUTPUT = new WebpipeOutput("", null);

    public static final WebpipeOutput NO_OUTPUT = new WebpipeOutput("", null);

    private static final WebpipeOutput INVALIDATED_OUTPUT = new WebpipeOutput("", null);

    private File cacheDir;

    private volatile WebpipeOutput output = NOT_INITIALIZED_OUTPUT;

    private String path;

    public Webpipe(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return getPath();
    }

    public abstract boolean refresh() throws IOException;

    public void setCacheDir(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    protected void invalidateOutputCache() {
        synchronized (output) {
            output = INVALIDATED_OUTPUT;
        }
    }

    public final WebpipeOutput getOutput() throws Exception {
        if (output == NOT_INITIALIZED_OUTPUT || output == INVALIDATED_OUTPUT) {
            synchronized (output) {
                if (output == NOT_INITIALIZED_OUTPUT) {
                    output = readOutput();
                    if (output == NOT_INITIALIZED_OUTPUT) {
                        output = fetchOutput();
                        setPath(output);
                        storeOutput(output);
                    }
                } else if (output == INVALIDATED_OUTPUT) {
                    output = fetchOutput();
                    setPath(output);
                    storeOutput(output);
                }
            }
        }
        return output;
    }

    private void setPath(WebpipeOutput output) {
        SourceMap sourceMap = output.getSourceMap();
        if (sourceMap != null) {
            sourceMap.file = path;
        }
    }

    abstract protected WebpipeOutput fetchOutput() throws Exception;

    private WebpipeOutput readOutput() throws Exception {
        if (cacheDir == null) {
            // no durable cache configured
            return NOT_INITIALIZED_OUTPUT;
        }

        File sha1File = new File(cacheDir, getPath() + ".sha1");
        if (!sha1File.exists()) {
            return NOT_INITIALIZED_OUTPUT;
        }

        byte[] expectedSha1 = computeSHA1();

        if (sha1File.length() != expectedSha1.length) {
            // wrong size : don't even bother to read it
            return NOT_INITIALIZED_OUTPUT;
        }
        byte[] actualSha1 = readFile(sha1File);

        if (!Arrays.equals(expectedSha1, actualSha1)) {
            return NOT_INITIALIZED_OUTPUT;
        }

        // same sha1, get the content out
        byte[] data = readFile(new File(cacheDir, getPath() + ".txt"));
        String content = new String(data, WebpipeUtils.UTF8);

        SourceMap sourceMap = null;
        File sourceMapFile = new File(cacheDir, getPath() + ".map");
        if (sourceMapFile.exists()) {
            sourceMap = WebpipeUtils.parseSourceMap(readFile(sourceMapFile));
        }

        return new WebpipeOutput(content, sourceMap);
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

    private void storeOutput(WebpipeOutput output) throws Exception {
        if (cacheDir == null) {
            // no durable cache configured
            return;
        }

        byte[] sha1 = computeSHA1();

        try (OutputStream out = new FileOutputStream(new File(cacheDir, getPath() + ".sha1"))) {
            out.write(sha1);
        }

        try (OutputStream out = new FileOutputStream(new File(cacheDir, getPath() + ".txt"))) {
            out.write(output.getContent().getBytes(WebpipeUtils.UTF8));
        }

        SourceMap sourceMap = output.getSourceMap();
        if (sourceMap != null) {
            try (OutputStream out = new FileOutputStream(new File(cacheDir, getPath() + ".map"))) {
                WebpipeUtils.serializeSourceMap(sourceMap, out);
            }
        }
    }

    private byte[] computeSHA1() throws Exception {
        MessageDigest digest = WebpipeUtils.buildSHA1Digest();
        updateDigest(digest);
        byte[] sha1 = digest.digest();
        return sha1;
    }

    public abstract void updateDigest(MessageDigest digest) throws Exception;

}
