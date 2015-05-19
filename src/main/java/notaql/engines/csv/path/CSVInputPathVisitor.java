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

package notaql.engines.csv.path;

import notaql.datamodel.Step;
import notaql.model.EvaluationException;
import notaql.model.path.*;
import notaql.parser.TransformationParser;
import notaql.parser.antlr.*;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.LinkedList;
import java.util.List;

/**
 * The visitor that produces inputpaths for the CSV engine
 */
public class CSVInputPathVisitor extends NotaQL2CSVInBaseVisitor<InputPath> {
    private final TransformationParser parser;
    private boolean relative;

    public CSVInputPathVisitor(TransformationParser parser, boolean relative) {
        this.parser = parser;
        this.relative = relative;
    }

    /**
     * Parses the split() method
     *
     * @param ctx
     * @return
     */
    @Override
    public InputPath visitSplittedInputPath(@NotNull NotaQL2CSVInParser.SplittedInputPathContext ctx) {
        final List<InputPathStep> pathSteps = new LinkedList<>(visit(ctx.firstInputPathStep()).getPathSteps());

        if (ctx.atom() != null) {
            String string = ctx.atom().getText().replace("\\'", "'");
            string = string.substring(1, string.length() - 1);
            pathSteps.add(new StringSplitMethodStep(string));
        } else {
            pathSteps.add(new StringSplitMethodStep());
        }

        pathSteps.add(new AnyBoundCurrentStep());

        return new InputPath(relative, pathSteps);
    }

    /**
     * Parses a cell step.
     *
     * @param ctx
     * @return
     */
    @Override
    public InputPath visitCellInputPathStep(@NotNull NotaQL2CSVInParser.CellInputPathStepContext ctx) {
        List<InputPathStep> pathSteps = new LinkedList<>();

        if (ctx.predicate() == null)
            pathSteps.add(new AnyBoundCurrentStep());
        else
            pathSteps.add(new PredicateStep(parser.getPredicateVisitor().evaluate(ctx.predicate().getText())));

        if (ctx.source.getType() == NotaQL2CSVInParser.Col)
            pathSteps.add(new NamePathMethodStep());

        return new InputPath(relative, pathSteps);
    }

    /**
     * Parses a constant column id and produces a matching step (IN.constant)
     *
     * @param ctx
     * @return
     */
    @Override
    public InputPath visitColIdInputPathStep(@NotNull NotaQL2CSVInParser.ColIdInputPathStepContext ctx) {
        return new InputPath(
                relative,
                new IdStep<>(new Step<>(ctx.colId().colName.getText()))
        );
    }

    /**
     * Parses a resolved input path (IN.$(IN.a))
     *
     * @param ctx
     * @return
     */
    @Override
    public InputPath visitResolvedInputPathStep(@NotNull NotaQL2CSVInParser.ResolvedInputPathStepContext ctx) {
        return new InputPath(
                relative,
                new ResolvedIdStep(parser.getInputPathVisitor().evaluateAbsolute(ctx.absoluteInputPath().path().getText()))
        );
    }

    /**
     * In case of a relative path (i.e. @) we simply create a new root of a path.
     * In any other case its not permitted.
     *
     * @param ctx
     * @return
     */
    @Override
    public InputPath visitRelativeCurrentCellPathStep(@NotNull NotaQL2CSVInParser.RelativeCurrentCellPathStepContext ctx) {
        if (!relative)
            throw new EvaluationException("'IN' is not allowed as input path");
        return new InputPath(
                true
        );
    }
}
