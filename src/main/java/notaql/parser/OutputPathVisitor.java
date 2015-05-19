/*
 * Copyright 2015 by Thomas Lottermann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package notaql.parser;

import notaql.model.path.OutputPath;
import notaql.parser.antlr.NotaQL2BaseVisitor;
import notaql.parser.antlr.NotaQL2Parser;
import org.antlr.v4.runtime.misc.NotNull;

/**
 * This class forwards the calls to the appropriate output engines specific input parser
 */
public class OutputPathVisitor extends NotaQL2BaseVisitor<OutputPath> {
    private final TransformationParser transformationParser;

    public OutputPathVisitor(TransformationParser transformationParser) {
        this.transformationParser = transformationParser;
    }

    @Override
    public OutputPath visitAbsoluteOutputPath(@NotNull NotaQL2Parser.AbsoluteOutputPathContext ctx) {
        return transformationParser.getOutEngineEvaluator().getOutputPathParser().parse(ctx.path().getText(), false);
    }

    @Override
    public OutputPath visitRelativeOutputPath(@NotNull NotaQL2Parser.RelativeOutputPathContext ctx) {
        return transformationParser.getOutEngineEvaluator().getOutputPathParser().parse(ctx.path().getText(), true);
    }
}
