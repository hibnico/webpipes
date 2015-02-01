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

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;

import org.hibnet.webpipes.Webpipe;
import org.hibnet.webpipes.processor.ObjectPoolHelper;
import org.hibnet.webpipes.processor.ObjectPoolHelper.ObjectFactory;
import org.hibnet.webpipes.processor.WebpipeProcessor;

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
 * @see http://blog.bolinfest.com/2009/11/calling-closure-compiler-from-java.html
 */
public class GoogleClosureCompressorProcessor extends WebpipeProcessor {

    /**
     * {@link CompilationLevel} to use for compression.
     */
    private CompilationLevel compilationLevel;

    /**
     * Reuse options(which are not thread safe).
     */
    private ObjectPoolHelper<CompilerOptions> optionsPool;

    /**
     * Uses google closure compiler with default compilation level: {@link CompilationLevel#SIMPLE_OPTIMIZATIONS}
     */
    public GoogleClosureCompressorProcessor() {
        this(CompilationLevel.SIMPLE_OPTIMIZATIONS);
    }

    /**
     * @param compilationLevel the compilationLevel to set
     */
    public void setCompilationLevel(CompilationLevel compilationLevel) {
        this.compilationLevel = compilationLevel;
    }

    /**
     * Uses google closure compiler with specified compilation level.
     *
     * @param compilationLevel not null {@link CompilationLevel} enum.
     */
    public GoogleClosureCompressorProcessor(CompilationLevel compilationLevel) {
        /**
         * Using pool to fix the threadSafety issue. See <a href="http://code.google.com/p/closure-compiler/issues/detail?id=781">issue</a>.
         */
        optionsPool = new ObjectPoolHelper<CompilerOptions>(new ObjectFactory<CompilerOptions>() {
            @Override
            public CompilerOptions create() {
                return newCompilerOptions();
            }
        });
        this.compilationLevel = compilationLevel;
    }

    @Override
    public String process(Webpipe webpipe, String content) throws IOException {
        CompilerOptions compilerOptions = optionsPool.getObject();
        Compiler compiler = newCompiler(compilerOptions);
        try {
            SourceFile[] input = new SourceFile[] { SourceFile.fromCode(webpipe.getId(), content) };
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
        } finally {
            optionsPool.returnObject(compilerOptions);
        }
        return content;
    }

    private Compiler newCompiler(CompilerOptions compilerOptions) {
        Compiler.setLoggingLevel(Level.SEVERE);
        Compiler compiler = new Compiler();
        compilationLevel.setOptionsForCompilationLevel(compilerOptions);
        // make it play nice with GAE
        compiler.disableThreads();
        compiler.initOptions(compilerOptions);
        return compiler;
    }

    /**
     * @param resource Currently processed resource. The resource can be null, when the closure compiler is used as a post processor.
     * @return An Array of externs files for the resource to process.
     */
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

    public void destroy() throws Exception {
        optionsPool.destroy();
    }
}
