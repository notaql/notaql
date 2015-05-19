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

import notaql.model.path.InputPath;
import notaql.parser.antlr.NotaQL2BaseVisitor;
import notaql.parser.antlr.NotaQL2Parser;
import org.antlr.v4.runtime.misc.NotNull;

/**
 * This class forwards the calls to the appropriate input engines specific input parser
 */
public class InputPathVisitor extends NotaQL2BaseVisitor<InputPath> {
    private final TransformationParser transformationParser;

    public InputPathVisitor(TransformationParser transformationParser) {
        this.transformationParser = transformationParser;
    }

    public InputPath evaluateAbsolute(String path) {
        return transformationParser.getInEngineEvaluator().getInputPathParser().parse(path, false);
    }

    public InputPath evaluateRelative(String path) {
        return transformationParser.getInEngineEvaluator().getInputPathParser().parse(path, true);
    }

    @Override
    public InputPath visitAbsoluteInputPath(@NotNull NotaQL2Parser.AbsoluteInputPathContext ctx) {
        return evaluateAbsolute(ctx.path().getText());
    }

    @Override
    public InputPath visitRelativeInputPath(@NotNull NotaQL2Parser.RelativeInputPathContext ctx) {
        return evaluateRelative(ctx.path().getText());
    }
}
