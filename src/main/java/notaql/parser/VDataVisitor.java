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

import notaql.model.EvaluationException;
import notaql.model.function.Argument;
import notaql.model.path.InputPath;
import notaql.model.vdata.*;
import notaql.parser.antlr.NotaQL2BaseVisitor;
import notaql.parser.antlr.NotaQL2Parser;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by thomas on 03.12.14.
 */
public class VDataVisitor extends NotaQL2BaseVisitor<VData> {
    private final TransformationParser transformationParser;

    public VDataVisitor(TransformationParser transformationParser) {
        this.transformationParser = transformationParser;
    }

    @Override
    public VData visitAbsoluteInputPathVData(@NotNull NotaQL2Parser.AbsoluteInputPathVDataContext ctx) {
        final InputPath inputPath = transformationParser.getInputPathVisitor().visit(ctx.absoluteInputPath());

        return new InputVData(inputPath);
    }

    @Override
    public VData visitAtomVData(@NotNull NotaQL2Parser.AtomVDataContext ctx) {
        return new AtomVData(transformationParser.getAtomValueVisitor().visit(ctx.atom()));
    }

    @Override
    public VData visitBracedVData(@NotNull NotaQL2Parser.BracedVDataContext ctx) {
        return visit(ctx.vData());
    }

    @Override
    public VData visitMultiplicatedVData(@NotNull NotaQL2Parser.MultiplicatedVDataContext ctx) {
        final VData left = visit(ctx.vData(0));
        final VData right = visit(ctx.vData(1));

        switch (ctx.op.getType()) {
            case NotaQL2Parser.STAR:
                return new ArithmeticVData(left, right, ArithmeticVData.Operation.MULTIPLY);
            case NotaQL2Parser.DIV:
                return new ArithmeticVData(left, right, ArithmeticVData.Operation.DIVIDE);
        }

        throw new EvaluationException("Unknown arithmetic operator: " + NotaQL2Parser.tokenNames[ctx.op.getType()]);
    }

    @Override
    public VData visitAddedVData(@NotNull NotaQL2Parser.AddedVDataContext ctx) {
        final VData left = visit(ctx.vData(0));
        final VData right = visit(ctx.vData(1));

        switch (ctx.op.getType()) {
            case NotaQL2Parser.PLUS:
                return new ArithmeticVData(left, right, ArithmeticVData.Operation.ADD);
            case NotaQL2Parser.MINUS:
                return new ArithmeticVData(left, right, ArithmeticVData.Operation.SUBTRACT);
        }

        throw new EvaluationException("Unknown arithmetic operator: " + NotaQL2Parser.tokenNames[ctx.op.getType()]);
    }

    @Override
    public VData visitGenericFunction(@NotNull NotaQL2Parser.GenericFunctionContext ctx) {
        final List<Argument> arguments = ctx.argument().stream()
                .map(a -> transformationParser.getArgumentVisitor().visit(a))
                .collect(Collectors.toList());

        return new GenericFunctionVData(ctx.Name().getText(), arguments);
    }


    @Override
    public VData visitRelativeInputPathVData(@NotNull NotaQL2Parser.RelativeInputPathVDataContext ctx) {
        final InputPath inputPath = transformationParser.getInputPathVisitor().visit(ctx.relativeInputPath());

        return new InputVData(inputPath);
    }


}