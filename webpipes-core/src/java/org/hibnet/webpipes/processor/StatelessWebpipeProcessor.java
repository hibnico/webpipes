package org.hibnet.webpipes.processor;

import org.hibnet.webpipes.Webpipe;

public abstract class StatelessWebpipeProcessor extends ProcessingWebpipeFactory {

    @Override
    public Webpipe createProcessingWebpipe(Webpipe source) {
        return new ProcessingWebpipe(source) {
            @Override
            protected String fetchContent() throws Exception {
                return process(webpipe);
            }
        };
    }

    public abstract String process(Webpipe webpipe) throws Exception;

}
