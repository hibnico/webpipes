package org.hibnet.webpipes.processor.less;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.hibnet.webpipes.Webpipe;
import org.hibnet.webpipes.processor.ProcessingWebpipe;
import org.hibnet.webpipes.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.LessCompiler.Problem;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.LessSource.StringSource;
import com.github.sommeri.less4j.core.DefaultLessCompiler;

public class Less4jWebpipe extends ProcessingWebpipe {

    private static final Logger LOG = LoggerFactory.getLogger(Less4jProcessor.class);

    private static final LessCompiler compiler = new DefaultLessCompiler();

    private List<Resource> importedResources = new ArrayList<>();

    /**
     * Required to use the less4j import mechanism.
     */
    private class RelativeAwareLessSource extends StringSource {

        private Resource resource;

        public RelativeAwareLessSource(Resource resource, String content) {
            super(content);
            this.resource = resource;
        }

        @Override
        public LessSource relativeSource(String relativePath) throws StringSourceException {
            try {
                Resource relativeResource = resource.resolve(relativePath);
                importedResources.add(relativeResource);
                return new RelativeAwareLessSource(relativeResource, relativeResource.getContent());
            } catch (Exception e) {
                LOG.error("Failed to compute relative resource: {}", resource, e);
                throw new StringSourceException();
            }
        }
    }

    public Less4jWebpipe(Webpipe webpipe) {
        super(webpipe);
    }

    @Override
    protected String fetchContent() throws Exception {
        importedResources.clear();

        String content = webpipe.getContent();
        StringSource lessSource;
        if (webpipe instanceof Resource) {
            lessSource = new RelativeAwareLessSource((Resource) webpipe, content);
        } else {
            lessSource = new StringSource(content);
        }
        CompilationResult result = compiler.compile(lessSource);
        logWarnings(result);
        content = result.getCss();
        return content;
    }

    @Override
    public boolean refresh() throws IOException {
        boolean needUpdate = refreshChildren();
        needUpdate = needUpdate || webpipe.refresh();
        if (needUpdate) {
            invalidateContentCache();
        }
        return needUpdate;
    }

    @Override
    protected List<Webpipe> buildChildrenList() throws IOException {
        return new ArrayList<Webpipe>(importedResources);
    }

    private void logWarnings(CompilationResult result) {
        if (!result.getWarnings().isEmpty()) {
            LOG.warn("Less warnings are:");
            for (Problem problem : result.getWarnings()) {
                LOG.warn(problemAsString(problem));
            }
        }
    }

    private String problemAsString(Problem problem) {
        return String.format("%s:%s %s.", problem.getLine(), problem.getCharacter(), problem.getMessage());
    }
}
