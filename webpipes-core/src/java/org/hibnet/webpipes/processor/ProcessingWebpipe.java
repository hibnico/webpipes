package org.hibnet.webpipes.processor;

import java.io.IOException;
import java.security.MessageDigest;

import org.hibnet.webpipes.Webpipe;

public abstract class ProcessingWebpipe extends Webpipe {

    private Webpipe childWebpipe;

    private String id;

    public ProcessingWebpipe(Webpipe childWebpipe) {
        this.childWebpipe = childWebpipe;
        this.id = this.getClass().getSimpleName() + "-" + childWebpipe.getId();
    }

    @Override
    public String getId() {
        return id;
    }

    protected Webpipe getChildWebpipe() {
        return childWebpipe;
    }

    @Override
    public String getName() {
        return childWebpipe.getName();
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
