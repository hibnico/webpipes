/*
 *  Copyright 2014-2015 WebPipes contributors
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
package org.hibnet.webpipes.processor.googleclosure;

import java.util.Arrays;
import java.util.logging.Level;

import org.hibnet.webpipes.Webpipe;
import org.hibnet.webpipes.WebpipeOutput;
import org.hibnet.webpipes.processor.ProcessingWebpipe;
import org.hibnet.webpipes.processor.ProcessingWebpipeFactory;

import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.ClosureCodingConvention;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.DiagnosticGroups;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;

/**
 * Uses Google closure compiler for js minimization.
 *
 * See <a href="http://blog.bolinfest.com/2009/11/calling-closure-compiler-from-java.html">
 * http://blog.bolinfest.com/2009/11/calling-closure-compiler-from-java.html</a>
 */
public class GoogleClosureCompressorProcessor {

    private final class GoogleClosureCompressorWebpipe extends ProcessingWebpipe {

        private CompilationLevel compilationLevel;

        private GoogleClosureCompressorWebpipe(Webpipe webpipe, CompilationLevel compilationLevel) {
            super(webpipe);
            this.compilationLevel = compilationLevel;
        }

        @Override
        protected WebpipeOutput fetchOutput() throws Exception {
            return new WebpipeOutput(compile(getChildWebpipe(), compilationLevel));
        }
    }

    public Webpipe createProcessingWebpipe(Webpipe source, CompilationLevel compilationLevel) {
        return new GoogleClosureCompressorWebpipe(source, compilationLevel);
    }

    public ProcessingWebpipeFactory createFactory(final CompilationLevel compilationLevel) {
        return new ProcessingWebpipeFactory() {
            @Override
            public Webpipe createProcessingWebpipe(Webpipe source) {
                return new GoogleClosureCompressorWebpipe(source, compilationLevel);
            }
        };
    }

    private String compile(Webpipe webpipe, CompilationLevel compilationLevel) throws Exception {
        if (compilationLevel == null) {
            compilationLevel = CompilationLevel.SIMPLE_OPTIMIZATIONS;
        }
        String content = webpipe.getOutput().getContent();
        CompilerOptions compilerOptions = newCompilerOptions();
        Compiler compiler = newCompiler(compilerOptions, compilationLevel);
        SourceFile[] input = new SourceFile[] { SourceFile.fromCode(webpipe.getName(), content) };
        SourceFile[] externs = getExterns(webpipe);
        if (externs == null) {
            // fallback to empty array when null is provided.
            externs = new SourceFile[] {};
        }
        Result result = null;
        result = compiler.compile(Arrays.asList(externs), Arrays.asList(input), compilerOptions);
        if (result.success) {
            content = compiler.toSource();
        } else {
            throw new RuntimeException("Compilation has errors: " + Arrays.asList(result.errors));
        }
        return content;
    }

    private Compiler newCompiler(CompilerOptions compilerOptions, CompilationLevel compilationLevel) {
        Compiler.setLoggingLevel(Level.SEVERE);
        Compiler compiler = new Compiler();
        compilationLevel.setOptionsForCompilationLevel(compilerOptions);
        // make it play nice with GAE
        compiler.disableThreads();
        compiler.initOptions(compilerOptions);
        return compiler;
    }

    protected SourceFile[] getExterns(Webpipe webpipe) {
        return new SourceFile[] {};
    }

    /**
     * @return default {@link CompilerOptions} object to be used by compressor.
     */
    protected CompilerOptions newCompilerOptions() {
        CompilerOptions options = new CompilerOptions();
        /**
         * According to John Lenz from the Closure Compiler project, if you are using the Compiler API directly, you should specify a
         * CodingConvention. {@link http://code.google.com/p/wro4j/issues/detail?id=155}
         */
        options.setCodingConvention(new ClosureCodingConvention());
        // set it to warning, otherwise compiler will fail
        options.setWarningLevel(DiagnosticGroups.CHECK_VARIABLES, CheckLevel.WARNING);
        return options;
    }
}
