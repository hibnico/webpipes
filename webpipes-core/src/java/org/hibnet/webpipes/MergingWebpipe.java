package org.hibnet.webpipes;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.List;

public class MergingWebpipe extends Webpipe {

    private List<Webpipe> webpipes;

    private String id;

    public MergingWebpipe(List<Webpipe> webpipes) {
        this.webpipes = webpipes;

        StringBuilder buffer = new StringBuilder("merging");
        for (Webpipe webpipe : webpipes) {
            buffer.append("-");
            buffer.append(webpipe.getName());
        }
        id = buffer.toString();
    }

    @Override
    protected List<Webpipe> buildChildrenList() throws IOException {
        return webpipes;
    }

    @Override
    public String getName() {
        return "merging";
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    protected String fetchContent() throws Exception {
        return fetchChildrenContent();
    }

    @Override
    public boolean refresh() throws IOException {
        return refreshChildren();
    }

    @Override
    public void updateDigest(MessageDigest digest) throws Exception {
        updateChildrenDigest(digest);
    }
}
