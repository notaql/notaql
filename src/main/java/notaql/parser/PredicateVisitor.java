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
import notaql.model.path.InputPath;
import notaql.model.predicate.*;
import notaql.model.vdata.VData;
import notaql.parser.antlr.*;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.NotNull;

/**
 * Created by thomas on 08.12.14.
 */
public class PredicateVisitor extends NotaQL2BaseVisitor<Predicate> {
    private final TransformationParser transformationParser;

    public PredicateVisitor(TransformationParser transformationParser) {
        this.transformationParser = transformationParser;
    }

    public Predicate evaluate(String predicate) {
        final NotaQL2Lexer lexer = new NotaQL2Lexer(new ANTLRInputStream(predicate));
        final NotaQL2Parser parser = new NotaQL2Parser(new CommonTokenStream(lexer));

        parser.addErrorListener(new NotaQLErrorListener(parser));

        final NotaQL2Parser.PredicateContext predicateContext = parser.standalonePredicate().predicate();

        return visit(predicateContext);
    }

    @Override
    public Predicate visitAbsolutePathExistencePredicate(@NotNull NotaQL2Parser.AbsolutePathExistencePredicateContext ctx) {
        return new PathExistencePredicate(transformationParser.getInputPathVisitor().visit(ctx.absoluteInputPath()));
    }

    @Override
    public Predicate visitRelativePathExistencePredicate(@NotNull NotaQL2Parser.RelativePathExistencePredicateContext ctx) {
        return new PathExistencePredicate(transformationParser.getInputPathVisitor().visit(ctx.relativeInputPath()));
    }

    @Override
    public Predicate visitBracedPredicate(@NotNull NotaQL2Parser.BracedPredicateContext ctx) {
        return visit(ctx.predicate());
    }

    @Override
    public Predicate visitNegatedPredicate(@NotNull NotaQL2Parser.NegatedPredicateContext ctx) {
        return new NegatedPredicate(visit(ctx.predicate()));
    }

    @Override
    public Predicate visitAndPredicate(@NotNull NotaQL2Parser.AndPredicateContext ctx) {
        return new LogicalOperationPredicate(
                visit(ctx.predicate(0)),
                visit(ctx.predicate(1)),
                LogicalOperationPredicate.Operator.AND
        );
    }

    @Override
    public Predicate visitOrPredicate(@NotNull NotaQL2Parser.OrPredicateContext ctx) {
        return new LogicalOperationPredicate(
                visit(ctx.predicate(0)),
                visit(ctx.predicate(1)),
                LogicalOperationPredicate.Operator.OR
        );
    }

    @Override
    public Predicate visitRelationalPredicate(@NotNull NotaQL2Parser.RelationalPredicateContext ctx) {
        final VData left = transformationParser.getVDataVisitor().visit(ctx.vData(0));
        final VData right = transformationParser.getVDataVisitor().visit(ctx.vData(1));

        switch (ctx.op.getType()) {
            case NotaQL2Parser.LT:
                return new RelationalPredicate(left, right, RelationalPredicate.Operator.LT);
            case NotaQL2Parser.LTEQ:
                return new RelationalPredicate(left, right, RelationalPredicate.Operator.LTEQ);
            case NotaQL2Parser.GT:
                return new RelationalPredicate(left, right, RelationalPredicate.Operator.GT);
            case NotaQL2Parser.GTEQ:
                return new RelationalPredicate(left, right, RelationalPredicate.Operator.GTEQ);
            case NotaQL2Parser.EQ:
                return new RelationalPredicate(left, right, RelationalPredicate.Operator.EQ);
            case NotaQL2Parser.NEQ:
                return new RelationalPredicate(left, right, RelationalPredicate.Operator.NEQ);
        }

        throw new EvaluationException("Unknown relational operator: " + ctx.op);
    }

}
