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

package notaql.engines.redis.parser.path;

import notaql.datamodel.Step;
import notaql.model.path.*;
import notaql.parser.TransformationParser;
import notaql.parser.antlr.NotaQL2KeyValueInBaseVisitor;
import notaql.parser.antlr.NotaQL2KeyValueInParser;
import org.antlr.v4.runtime.misc.NotNull;

/**
 * Created by thomas on 03.12.14.
 */
public class RedisInputPathStepVisitor extends NotaQL2KeyValueInBaseVisitor<InputPathStep> {
    private final TransformationParser parser;

    public RedisInputPathStepVisitor(TransformationParser parser) {
        this.parser = parser;
    }

    @Override
    public InputPathStep visitKeyId(@NotNull NotaQL2KeyValueInParser.KeyIdContext ctx) {
        return new IdStep<>(new Step<>(ctx.getText()));
    }

    @Override
    public InputPathStep visitIndex(@NotNull NotaQL2KeyValueInParser.IndexContext ctx) {
        return new IdStep<>(new Step<>(Integer.parseInt(ctx.Int().getText())));
    }

    @Override
    public InputPathStep visitSplitStep(@NotNull NotaQL2KeyValueInParser.SplitStepContext ctx) {
        if(ctx.atom() != null) {
            String string = ctx.atom().getText().replace("\\'", "'");
            string = string.substring(1, string.length() - 1);
            return new StringSplitMethodStep(string);
        }
        return new StringSplitMethodStep();
    }

    @Override
    public InputPathStep visitResolvedGenericComplexStep(@NotNull NotaQL2KeyValueInParser.ResolvedGenericComplexStepContext ctx) {
        return new ResolvedIdStep(parser.getInputPathVisitor().evaluateAbsolute(ctx.absoluteInputPath().path().getText()));
    }

    @Override
    public InputPathStep visitAnyGenericComplexStep(@NotNull NotaQL2KeyValueInParser.AnyGenericComplexStepContext ctx) {
        return new AnyBoundStep();
    }

    @Override
    public InputPathStep visitCurrentGenericComplexStep(@NotNull NotaQL2KeyValueInParser.CurrentGenericComplexStepContext ctx) {
        return new CurrentBoundStep();
    }

    @Override
    public InputPathStep visitPredicateGenericComplexStep(@NotNull NotaQL2KeyValueInParser.PredicateGenericComplexStepContext ctx) {
        return new PredicateStep(parser.getPredicateVisitor().evaluate(ctx.predicate().getText()));
    }

    @Override
    public InputPathStep visitNameHashMethod(@NotNull NotaQL2KeyValueInParser.NameHashMethodContext ctx) {
        return new NamePathMethodStep();
    }

    @Override
    public InputPathStep visitIndexListMethod(@NotNull NotaQL2KeyValueInParser.IndexListMethodContext ctx) {
        return new IndexPathMethodStep();
    }
}
