package org.hibnet.webpipes.processor;

import java.io.IOException;
import java.security.MessageDigest;

import org.hibnet.webpipes.Webpipe;

public abstract class ProcessingWebpipe extends Webpipe {

    protected Webpipe webpipe;

    private String id;

    public ProcessingWebpipe(Webpipe webpipe) {
        this.webpipe = webpipe;
        this.id = this.getClass().getSimpleName() + "-" + webpipe.getId();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return webpipe.getName();
    }

    @Override
    public void updateDigest(MessageDigest digest) throws Exception {
        webpipe.updateDigest(digest);
    }

    @Override
    public boolean refresh() throws IOException {
        boolean needUpdate = webpipe.refresh();
        if (needUpdate) {
            invalidateOutputCache();
        }
        return needUpdate;
    }

}
