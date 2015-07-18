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

import notaql.datamodel.Step;
import notaql.model.function.Argument;
import notaql.model.path.IdStep;
import notaql.model.path.OutputPath;
import notaql.model.path.ResolvedIdStep;
import notaql.parser.antlr.NotaQL2BaseVisitor;
import notaql.parser.antlr.NotaQL2Parser;
import org.antlr.v4.runtime.misc.NotNull;

/**
 * Extracts arguments from the context
 */
public class ArgumentVisitor extends NotaQL2BaseVisitor<Argument> {
    private final TransformationParser transformationParser;

    public ArgumentVisitor(TransformationParser transformationParser) {
        this.transformationParser = transformationParser;
    }

    @Override
    public Argument visitVDataArgument(@NotNull NotaQL2Parser.VDataArgumentContext ctx) {
        return new Argument(
                transformationParser.getVDataVisitor().visit(ctx.vData())
        );
    }

    @Override
    public Argument visitNamedArgument(@NotNull NotaQL2Parser.NamedArgumentContext ctx) {
        final ArgumentNameVisitor argumentNameVisitor = new ArgumentNameVisitor();
        final OutputPath path = argumentNameVisitor.visit(ctx.attributeName());

        return new Argument(
                path,
                transformationParser.getVDataVisitor().visit(ctx.vData())
        );
    }

    private class ArgumentNameVisitor extends NotaQL2BaseVisitor<OutputPath> {
        @Override
        public OutputPath visitConstantAttributeName(@NotNull NotaQL2Parser.ConstantAttributeNameContext ctx) {
            return new OutputPath(new IdStep<>(new Step<>(ctx.Name().getText())));
        }

        @Override
        public OutputPath visitResolvedAttributeName(@NotNull NotaQL2Parser.ResolvedAttributeNameContext ctx) {
            return new OutputPath(
                    new ResolvedIdStep(
                            transformationParser.getInEngineEvaluator()
                                    .getInputPathParser().parse(ctx.absoluteInputPath().getText(), false)
                    )
            );
        }
    }
}