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

package notaql.engines.hbase.parser.path;

import notaql.datamodel.Step;
import notaql.engines.hbase.model.path.AnyFamilyStep;
import notaql.model.EvaluationException;
import notaql.model.path.*;
import notaql.parser.TransformationParser;
import notaql.parser.antlr.NotaQL2ColumnInBaseVisitor;
import notaql.parser.antlr.NotaQL2ColumnInParser;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.LinkedList;
import java.util.List;

/**
 * Extracts an InputPath from given parse tree.
 */
public class HBaseInputPathVisitor extends NotaQL2ColumnInBaseVisitor<InputPath> {
    private final TransformationParser parser;
    private boolean relative;

    public HBaseInputPathVisitor(TransformationParser parser, boolean relative) {
        this.parser = parser;
        this.relative = relative;
    }

    /**
     * Here we parse the .split() method
     * @param ctx
     * @return
     */
    @Override
    public InputPath visitSplittedInputPath(@NotNull NotaQL2ColumnInParser.SplittedInputPathContext ctx) {
        // TODO: Generify split function? at least encapsulate it for all parsers to use
        final List<InputPathStep> pathSteps = new LinkedList<>(visit(ctx.firstInputPathStep()).getPathSteps());

        if(ctx.atom() != null) {
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
     * Here we parse the row id input (IN._r)
     * @param ctx
     * @return
     */
    @Override
    public InputPath visitRowInputPathStep(@NotNull NotaQL2ColumnInParser.RowInputPathStepContext ctx) {
        return new InputPath(relative, new IdStep<>(new Step<>("_id")));
    }

    /**
     * Here we parse cell input (e.g. IN._c?(@ > 3)).
     * This also means delegating stuff to another input path parser
     * @param ctx
     * @return
     */
    @Override
    public InputPath visitCellInputPathStep(@NotNull NotaQL2ColumnInParser.CellInputPathStepContext ctx) {
        final InputPathStep familyStep =
                ctx.colFamilyFilter==null ?
                    new AnyBoundCurrentStep() : new BoundIdStep<>(new Step<>(ctx.colFamilyFilter.getText()));

        List<InputPathStep> pathSteps = new LinkedList<>();

        pathSteps.add(familyStep);

        if(ctx.predicate() == null)
            pathSteps.add(new AnyBoundCurrentStep());
        else
            pathSteps.add(new PredicateStep(parser.getPredicateVisitor().evaluate(ctx.predicate().getText())));

        if(ctx.source.getType()==NotaQL2ColumnInParser.Col)
            pathSteps.add(new NamePathMethodStep());

        return new InputPath(relative, pathSteps);
    }

    /**
     * Here we parse constant steps (e.g. IN.a)
     * @param ctx
     * @return
     */
    @Override
    public InputPath visitColIdInputPathStep(@NotNull NotaQL2ColumnInParser.ColIdInputPathStepContext ctx) {
        final InputPathStep familyStep =
                ctx.colId().colFamily==null ?
                        new AnyFamilyStep() : new IdStep<>(new Step<>(ctx.colId().colFamily.getText()));

        return new InputPath(
                relative,
                familyStep,
                new IdStep<>(new Step<>(ctx.colId().colName.getText()))
        );
    }

    /**
     * Here we parse resolved paths (e.g. IN.$(IN.a)).
     * @param ctx
     * @return
     */
    @Override
    public InputPath visitResolvedInputPathStep(@NotNull NotaQL2ColumnInParser.ResolvedInputPathStepContext ctx) {
        final InputPathStep familyStep =
                ctx.colFamily==null ?
                        new AnyBoundCurrentStep() : new BoundIdStep<>(new Step<>(ctx.colFamily.getText()));

        return new InputPath(
                relative,
                familyStep,
                new ResolvedIdStep(parser.getInputPathVisitor().evaluateAbsolute(ctx.absoluteInputPath().path().getText()))
        );
    }

    /**
     * Here we parse the @ step. This is only allowed in a relative path.
     * @param ctx
     * @return
     */
    @Override
    public InputPath visitRelativeCurrentCellPathStep(@NotNull NotaQL2ColumnInParser.RelativeCurrentCellPathStepContext ctx) {
        if(!relative)
            throw new EvaluationException("'IN' is not allowed as input path");
        return new InputPath(
                true
        );
    }
}
