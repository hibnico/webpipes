package org.hibnet.webpipes.processor;

import java.io.IOException;
import java.security.MessageDigest;

import org.hibnet.webpipes.Webpipe;
import org.hibnet.webpipes.WebpipeUtils;

public abstract class ProcessingWebpipe extends Webpipe {

    private Webpipe childWebpipe;

    public ProcessingWebpipe(String id, String path, String name, Webpipe childWebpipe) {
        super(id, WebpipeUtils.pathOf(path, "/webpipes", name, childWebpipe.getPath()));
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
