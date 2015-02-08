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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Webpipe {

    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private static final String NOT_INITIALIZED_CONTENT = "";

    public static final String NO_CONTENT = "";

    private static final String INVALIDATED_CONTENT = "";

    private static final List<Webpipe> NOT_INITIALIZED_CHILDREN = new ArrayList<>();

    public static final List<Webpipe> NO_CHILDREN = new ArrayList<>();

    private static final List<Webpipe> INVALIDATED_CHILDREN = new ArrayList<>();

    private File cacheDir;

    private volatile String content = NO_CONTENT;

    private List<Webpipe> children;

    public abstract String getId();

    protected List<Webpipe> getChildren() throws IOException {
        if (children == INVALIDATED_CHILDREN || children == NOT_INITIALIZED_CHILDREN) {
            synchronized (children) {
                if (children == INVALIDATED_CHILDREN || children == NOT_INITIALIZED_CHILDREN) {
                    children = buildChildrenList();
                }
            }
        }
        return children;
    }

    protected List<Webpipe> buildChildrenList() throws IOException {
        return NO_CHILDREN;
    }

    public abstract boolean refresh() throws IOException;

    protected boolean refreshChildren() throws IOException {
        boolean refresh = refreshChildrenList();
        for (Webpipe webpipe : getChildren()) {
            refresh = refresh || webpipe.refresh();
        }
        if (refresh) {
            invalidateContentCache();
        }
        return refresh;
    }

    protected boolean refreshChildrenList() throws IOException {
        List<Webpipe> cachedChildren = getChildren();
        List<Webpipe> newChildren = buildChildrenList();
        boolean refresh = cachedChildren.size() != newChildren.size();
        if (!refresh) {
            for (int i = 0; i < cachedChildren.size(); i++) {
                refresh = !newChildren.get(i).getId().equals(cachedChildren.get(i).getId());
                if (refresh) {
                    break;
                }
            }
        }
        if (refresh) {
            synchronized (children) {
                children = newChildren;
            }
        }
        return refresh;
    }

    public void setCacheDir(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    protected void invalidateContentCache() {
        synchronized (content) {
            content = INVALIDATED_CONTENT;
        }
    }

    protected void invalidateChildrenListCache() {
        synchronized (children) {
            children = INVALIDATED_CHILDREN;
        }
    }

    public final String getContent() throws Exception {
        if (content == NOT_INITIALIZED_CONTENT || content == INVALIDATED_CONTENT) {
            synchronized (content) {
                if (content == NOT_INITIALIZED_CONTENT) {
                    content = readContent();
                    if (content == NOT_INITIALIZED_CONTENT) {
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

    protected String fetchChildrenContent() throws Exception {
        StringBuilder buffer = new StringBuilder();
        for (Webpipe webpipe : getChildren()) {
            buffer.append(webpipe.getContent());
            buffer.append("\n");
        }
        return buffer.toString();
    }

    private String readContent() throws Exception {
        if (cacheDir == null) {
            // no durable cache configured
            return NOT_INITIALIZED_CONTENT;
        }

        File sha1File = new File(cacheDir, getId() + ".sha1");
        if (!sha1File.exists()) {
            return NOT_INITIALIZED_CONTENT;
        }

        byte[] expectedSha1 = computeSHA1();

        if (sha1File.length() != expectedSha1.length) {
            // wrong size : don't even bother to read it
            return NOT_INITIALIZED_CONTENT;
        }
        byte[] actualSha1 = readFile(sha1File);

        if (!Arrays.equals(expectedSha1, actualSha1)) {
            return NOT_INITIALIZED_CONTENT;
        }

        // same sha1, get the content out
        byte[] data = readFile(new File(cacheDir, getId() + ".txt"));
        return new String(data, WebpipeUtils.UTF8);
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
            out.write(content.getBytes(WebpipeUtils.UTF8));
        }
    }

    private byte[] computeSHA1() throws Exception {
        MessageDigest digest = WebpipeUtils.buildSHA1Digest();
        updateDigest(digest);
        byte[] sha1 = digest.digest();
        return sha1;
    }

    protected abstract void updateDigest(MessageDigest digest) throws Exception;

    protected void updateChildrenDigest(MessageDigest digest) throws Exception {
        for (Webpipe webpipe : getChildren()) {
            webpipe.updateDigest(digest);
            digest.update((byte) '\n');
        }
    }

}
