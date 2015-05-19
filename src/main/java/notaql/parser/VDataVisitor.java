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

import notaql.model.AttributeSpecification;
import notaql.model.EvaluationException;
import notaql.model.path.InputPath;
import notaql.model.path.OutputPath;
import notaql.model.vdata.*;
import notaql.model.vdata.aggregation.*;
import notaql.parser.antlr.NotaQL2BaseVisitor;
import notaql.parser.antlr.NotaQL2Parser;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.LinkedList;
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
    public VData visitAggregateVData(@NotNull NotaQL2Parser.AggregateVDataContext ctx) {
        final NotaQL2Parser.AggregationFunctionContext aggrCtx = ctx.aggregationFunction();
        // TODO: could be more generic
        switch (aggrCtx.function.getType()) {
            case NotaQL2Parser.SUM:
                return new SumVData(visit(aggrCtx.vData()));
            case NotaQL2Parser.AVG:
                return new AvgVData(visit(aggrCtx.vData()));
            case NotaQL2Parser.MIN:
                return new ExtremumVData(visit(aggrCtx.vData()), ExtremumVData.Function.MIN);
            case NotaQL2Parser.MAX:
                return new ExtremumVData(visit(aggrCtx.vData()), ExtremumVData.Function.MAX);
            case NotaQL2Parser.COUNT:
                return new CountVData();
            case NotaQL2Parser.IMPLODE:
                return new ImplodeVData(visit(aggrCtx.vData()));
            case NotaQL2Parser.LIST:
                return new ListVData(visit(aggrCtx.vData()));
        }

        throw new EvaluationException("Unknown aggregation function operator: " + NotaQL2Parser.tokenNames[aggrCtx.function.getType()]);
    }

    @Override
    public VData visitConstructorVData(@NotNull NotaQL2Parser.ConstructorVDataContext ctx) {
        return visit(ctx.constructorFunction());
    }

    @Override
    public VData visitObjectConstructorFunction(@NotNull NotaQL2Parser.ObjectConstructorFunctionContext ctx) {
        final List<AttributeSpecification> specifications = new LinkedList<>();

        for (NotaQL2Parser.AttributeSpecificationContext specificationCtx : ctx.attributeSpecification()) {
            final OutputPath outputPath = transformationParser.getOutputPathVisitor().visit(specificationCtx.genericOutputPath());
            final VData vData = visit(specificationCtx.vData());

            specifications.add(new AttributeSpecification(outputPath, vData));
        }

        return new ObjectVData(specifications);
    }

    @Override
    public VData visitGenericConstructorFunction(@NotNull NotaQL2Parser.GenericConstructorFunctionContext ctx) {
        final ConstructorVData constructor = transformationParser.getOutEngineEvaluator().getConstructor(ctx.Name().getText());
        if(constructor == null)
            throw new EvaluationException("Unknown constructor: " + ctx.Name().getText());

        final List<AttributeSpecification> specifications = new LinkedList<>();

        for (NotaQL2Parser.AttributeSpecificationContext specificationCtx : ctx.attributeSpecification()) {
            final OutputPath outputPath = transformationParser.getOutputPathVisitor().visit(specificationCtx.genericOutputPath());
            final VData vData = visit(specificationCtx.vData());

            specifications.add(new AttributeSpecification(outputPath, vData));
        }

        constructor.init(specifications.toArray(new AttributeSpecification[specifications.size()]));

        return constructor;
    }

    @Override
    public VData visitGenericFunction(@NotNull NotaQL2Parser.GenericFunctionContext ctx) {
        // TODO: make the functions in the in and out engines pluggable as well; Also generify AVG, etc..
        FunctionVData function = transformationParser.getInEngineEvaluator().getFunction(ctx.Name().getText());
        if(function == null)
            function = transformationParser.getOutEngineEvaluator().getFunction(ctx.Name().getText());

        final List<VData> vDatas = ctx.vData().stream().map(this::visit).collect(Collectors.toList());

        final VData[] vDataArray = vDatas.toArray(new VData[vDatas.size()]);
        if(function != null) {
            function.init(vDataArray);
            return function;
        }

        return new GenericFunctionVData(ctx.Name().getText(), vDataArray);
    }

    @Override
    public VData visitRelativeInputPathVData(@NotNull NotaQL2Parser.RelativeInputPathVDataContext ctx) {
        final InputPath inputPath = transformationParser.getInputPathVisitor().visit(ctx.relativeInputPath());

        return new InputVData(inputPath);
    }


}