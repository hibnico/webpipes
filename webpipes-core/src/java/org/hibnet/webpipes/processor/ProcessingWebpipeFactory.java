package org.hibnet.webpipes.processor;

import org.hibnet.webpipes.Webpipe;

public abstract class ProcessingWebpipeFactory {

    abstract public Webpipe createProcessingWebpipe(Webpipe source);

}
