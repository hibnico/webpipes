/*
 *  Copyright 2015 WebPipes contributors
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
function webpipes_runner(content, uglify, reserved_names) {
    var options = {
        codegen_options: {
                ascii_only: false,
                beautify: !uglify,
                indent_level: 4,
                indent_start: 0,
                quote_keys: false,
                space_colon: false,
                inline_script: false
        },
        reserved_names: reserved_names,    
        ast: false,
        consolidate: false,
        mangle: true,
        mangle_toplevel: false,
        no_mangle_functions: false,
        squeeze: true,
        make_seqs: true,
        dead_code: true,
        verbose: false,
        show_copyright: true,
        out_same_file: false,
        max_line_length: 32 * 1024,
        unsafe: false,
        defines: { },
        lift_vars: false,    
        make: false,
    };
    var jsp = require("./parse-js"),
    pro = require("./process"),
    slice = jsp.slice,
    member = jsp.member,
    curry = jsp.curry,
    MAP = pro.MAP,
    PRECEDENCE = jsp.PRECEDENCE,
    OPERATORS = jsp.OPERATORS;

    function ast_squeeze_more(ast) {
        var w = pro.ast_walker(), walk = w.walk, scope;
        function with_scope(s, cont) {
                var save = scope, ret;
                scope = s;
                ret = cont();
                scope = save;
                return ret;
        };
        function _lambda(name, args, body) {
                return [ this[0], name, args, with_scope(body.scope, curry(MAP, body, walk)) ];
        };
        return w.with_walkers({
                "toplevel": function(body) {
                        return [ this[0], with_scope(this.scope, curry(MAP, body, walk)) ];
                },
                "function": _lambda,
                "defun": _lambda,
                "new": function(ctor, args) {
                        if (ctor[0] == "name" && ctor[1] == "Array" && !scope.has("Array")) {
                                if (args.length != 1) {
                                        return [ "array", args ];
                                } else {
                                        return walk([ "call", [ "name", "Array" ], args ]);
                                }
                        }
                },
                "call": function(expr, args) {
                        if (expr[0] == "dot" && expr[2] == "toString" && args.length == 0) {
                                // foo.toString()  ==>  foo+""
                                return [ "binary", "+", expr[1], [ "string", "" ]];
                        }
                        if (expr[0] == "name" && expr[1] == "Array" && args.length != 1 && !scope.has("Array")) {
                                return [ "array", args ];
                        }
                }
        }, function() {
                return walk(pro.ast_add_scope(ast));
        });
    };

    exports.ast_squeeze_more = ast_squeeze_more;

    //parse code and get the initial AST
    var ast = jsp.parse(content);
    //get a new AST with mangled names
    ast = exports.ast_mangle(ast, {
        toplevel: options.mangle_toplevel,
        except: options.reserved_names
    });
    //get an AST with compression optimizations
    ast = exports.ast_squeeze(ast, options);
    if (options.unsafe) {
        ast = exports.ast_squeeze_more(ast);
    }
    var res = exports.gen_code(ast,  options.codegen_options);
    return {
        content : res,
        sourceMap : null
    }
}
