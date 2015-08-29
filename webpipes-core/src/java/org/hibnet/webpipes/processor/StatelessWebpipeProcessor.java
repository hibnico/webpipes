package org.hibnet.webpipes.processor;

import org.hibnet.webpipes.Webpipe;
import org.hibnet.webpipes.WebpipeOutput;

public abstract class StatelessWebpipeProcessor extends ProcessingWebpipeFactory {

    @Override
    public Webpipe createProcessingWebpipe(Webpipe source) {
        return new ProcessingWebpipe(source) {
            @Override
            protected WebpipeOutput fetchOutput() throws Exception {
                return process(webpipe);
            }
        };
    }

    public abstract WebpipeOutput process(Webpipe webpipe) throws Exception;

}
