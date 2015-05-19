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
import notaql.model.path.*;
import notaql.parser.TransformationParser;
import notaql.parser.antlr.NotaQL2CSVOutBaseVisitor;
import notaql.parser.antlr.NotaQL2CSVOutParser;
import org.antlr.v4.runtime.misc.NotNull;

/**
 * The visitor that provides outputpaths for the CSV engine
 */
public class CSVOutputPathVisitor extends NotaQL2CSVOutBaseVisitor<OutputPath> {
    private final TransformationParser parser;
    private boolean relative;

    public CSVOutputPathVisitor(TransformationParser parser, boolean relative) {
        this.parser = parser;
        this.relative = relative;
    }

    /**
     * Provides the constant step of the given column id (e.g. OUT.a)
     *
     * @param ctx
     * @return
     */
    @Override
    public OutputPath visitColIdOutputPath(@NotNull NotaQL2CSVOutParser.ColIdOutputPathContext ctx) {
        if (ctx.colId().colName.getText().equals("_"))
            return new OutputPath(new IgnoredIdStep());

        return new OutputPath(
                new IdStep<>(new Step<>(ctx.colId().colName.getText()))
        );
    }

    /**
     * Provides resolved output paths (e.g. OUT.$(IN.a))
     *
     * @param ctx
     * @return
     */
    @Override
    public OutputPath visitResolvedOutputPath(@NotNull NotaQL2CSVOutParser.ResolvedOutputPathContext ctx) {
        return new OutputPath(
                new ResolvedIdStep(parser.getInputPathVisitor().evaluateAbsolute(ctx.absoluteInputPath().path().getText()))
        );
    }
}
