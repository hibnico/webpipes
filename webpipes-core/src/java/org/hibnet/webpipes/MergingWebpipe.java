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
package org.hibnet.webpipes;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import org.hibnet.webpipes.merger.SimpleWebpipeMerger;
import org.hibnet.webpipes.merger.WebpipeMerger;

public abstract class MergingWebpipe extends Webpipe {

    private static final List<Webpipe> NOT_INITIALIZED_CHILDREN = new ArrayList<>();

    public static final List<Webpipe> NO_CHILDREN = new ArrayList<>();

    private static final List<Webpipe> INVALIDATED_CHILDREN = new ArrayList<>();

    private List<Webpipe> children = NOT_INITIALIZED_CHILDREN;

    private WebpipeMerger merger = new SimpleWebpipeMerger();

    public MergingWebpipe(String path) {
        super(path);
    }

    public void setMerger(WebpipeMerger merger) {
        this.merger = merger;
    }

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

    abstract protected List<Webpipe> buildChildrenList() throws IOException;

    @Override
    public boolean refresh() throws IOException {
        boolean refresh = refreshChildrenList();
        for (Webpipe webpipe : getChildren()) {
            refresh = refresh || webpipe.refresh();
        }
        if (refresh) {
            invalidateOutputCache();
        }
        return refresh;
    }

    protected boolean refreshChildrenList() throws IOException {
        List<Webpipe> cachedChildren = getChildren();
        List<Webpipe> newChildren = buildChildrenList();
        boolean refresh = cachedChildren.size() != newChildren.size();
        if (!refresh) {
            for (int i = 0; i < cachedChildren.size(); i++) {
                refresh = !newChildren.get(i).getPath().equals(cachedChildren.get(i).getPath());
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

    protected void invalidateChildrenListCache() {
        synchronized (children) {
            children = INVALIDATED_CHILDREN;
        }
    }

    @Override
    protected WebpipeOutput fetchOutput() throws Exception {
        return merger.merge(getChildren());
    }

    @Override
    public void updateDigest(MessageDigest digest) throws Exception {
        for (Webpipe webpipe : getChildren()) {
            webpipe.updateDigest(digest);
            digest.update((byte) '\n');
        }
    }
}
