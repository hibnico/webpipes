package org.hibnet.webpipes.processor;

import java.io.IOException;
import java.security.MessageDigest;

import org.hibnet.webpipes.Webpipe;

public abstract class ProcessingWebpipe extends Webpipe {

    private Webpipe childWebpipe;

    public ProcessingWebpipe(String name, Webpipe childWebpipe) {
        super(name + "/" + childWebpipe.getPath());
        this.childWebpipe = childWebpipe;
    }

    protected Webpipe getChildWebpipe() {
        return childWebpipe;
    }

    @Override
    public void updateDigest(MessageDigest digest) throws Exception {
        childWebpipe.updateDigest(digest);
    }

    @Override
    public boolean refresh() throws IOException {
        boolean needUpdate = childWebpipe.refresh();
        if (needUpdate) {
            invalidateOutputCache();
        }
        return needUpdate;
    }

}
