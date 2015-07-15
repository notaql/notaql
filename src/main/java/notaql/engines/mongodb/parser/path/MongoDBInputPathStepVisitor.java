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

package notaql.engines.mongodb.parser.path;

import notaql.datamodel.Step;
import notaql.model.path.*;
import notaql.parser.TransformationParser;
import notaql.parser.antlr.NotaQL2DocumentInBaseVisitor;
import notaql.parser.antlr.NotaQL2DocumentInParser;
import org.antlr.v4.runtime.misc.NotNull;

/**
 * Created by thomas on 03.12.14.
 */
public class MongoDBInputPathStepVisitor extends NotaQL2DocumentInBaseVisitor<InputPathStep> {
    private final TransformationParser parser;

    public MongoDBInputPathStepVisitor(TransformationParser parser) {
        this.parser = parser;
    }

    @Override
    public InputPathStep visitFieldIdStep(@NotNull NotaQL2DocumentInParser.FieldIdStepContext ctx) {
        return new IdStep<>(new Step<>(ctx.fieldId().fieldName.getText()));
    }

    @Override
    public InputPathStep visitIdStep(@NotNull NotaQL2DocumentInParser.IdStepContext ctx) {
        return new IdStep<>(new Step<>("_id"));
    }

    @Override
    public InputPathStep visitIndexListStep(@NotNull NotaQL2DocumentInParser.IndexListStepContext ctx) {
        return new IdStep<>(new Step<>(Integer.parseInt(ctx.index().indexNumber.getText())));
    }

    @Override
    public InputPathStep visitResolvedGenericStep(@NotNull NotaQL2DocumentInParser.ResolvedGenericStepContext ctx) {
        return new ResolvedIdStep(parser.getInputPathVisitor().evaluateAbsolute(ctx.absoluteInputPath().path().getText()));
    }

    @Override
    public InputPathStep visitPredicateGenericStep(@NotNull NotaQL2DocumentInParser.PredicateGenericStepContext ctx) {
        return new PredicateStep(parser.getPredicateVisitor().evaluate(ctx.predicate().getText()));
    }

    @Override
    public InputPathStep visitAnyGenericStep(@NotNull NotaQL2DocumentInParser.AnyGenericStepContext ctx) {
        return new AnyBoundStep();
    }

    @Override
    public InputPathStep visitCurrentGenericStep(@NotNull NotaQL2DocumentInParser.CurrentGenericStepContext ctx) {
        return new CurrentBoundStep();
    }

    @Override
    public InputPathStep visitNamePathMethod(@NotNull NotaQL2DocumentInParser.NamePathMethodContext ctx) {
        return new NamePathMethodStep();
    }

    @Override
    public InputPathStep visitIndexPathMethod(@NotNull NotaQL2DocumentInParser.IndexPathMethodContext ctx) {
        return new IndexPathMethodStep();
    }

    @Override
    public InputPathStep visitStringSplitMethod(@NotNull NotaQL2DocumentInParser.StringSplitMethodContext ctx) {
        if(ctx.atom() != null) {
            String string = ctx.atom().getText().replace("\\'", "'");
            string = string.substring(1, string.length() - 1);
            return new StringSplitMethodStep(string);
        }

        // FIXME: shouldn't an anystep be added afterwards?
        return new StringSplitMethodStep();
    }

    @Override
    public InputPathStep visitGenericAttributeStep(@NotNull NotaQL2DocumentInParser.GenericAttributeStepContext ctx) {
        return visit(ctx.genericStep());
    }

    @Override
    public InputPathStep visitGenericListStep(@NotNull NotaQL2DocumentInParser.GenericListStepContext ctx) {
        return visit(ctx.genericStep());
    }

    @Override
    public InputPathStep visitListPathStep(@NotNull NotaQL2DocumentInParser.ListPathStepContext ctx) {
        return visit(ctx.listStep());
    }

    @Override
    public InputPathStep visitAttributePathStep(@NotNull NotaQL2DocumentInParser.AttributePathStepContext ctx) {
        return visit(ctx.attributeStep());
    }

    @Override
    public InputPathStep visitMethodPathStep(@NotNull NotaQL2DocumentInParser.MethodPathStepContext ctx) {
        return visit(ctx.pathMethod());
    }

    @Override
    public InputPathStep visitSplitPathMethod(@NotNull NotaQL2DocumentInParser.SplitPathMethodContext ctx) {
        return visit(ctx.splitMethod());
    }

    @Override
    public InputPathStep visitSplitNameStep(@NotNull NotaQL2DocumentInParser.SplitNameStepContext ctx) {
        return new IdStep<>(new Step<>("split"));
    }
}
