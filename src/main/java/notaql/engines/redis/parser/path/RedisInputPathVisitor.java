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
import notaql.model.path.AnyBoundCurrentStep;
import notaql.model.path.IdStep;
import notaql.model.path.InputPath;
import notaql.model.path.InputPathStep;
import notaql.parser.TransformationParser;
import notaql.parser.antlr.NotaQL2KeyValueInBaseVisitor;
import notaql.parser.antlr.NotaQL2KeyValueInParser;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.LinkedList;

/**
 * Created by thomas on 13.01.15.
 */
public class RedisInputPathVisitor extends NotaQL2KeyValueInBaseVisitor<InputPath> {
    private final TransformationParser parser;
    private final RedisInputPathStepVisitor stepVisitor;
    private boolean relative;

    public RedisInputPathVisitor(TransformationParser parser, boolean relative) {
        this.parser = parser;
        this.relative = relative;

        this.stepVisitor = new RedisInputPathStepVisitor(parser);
    }

    @Override
    public InputPath visitKeyInputPath(@NotNull NotaQL2KeyValueInParser.KeyInputPathContext ctx) {
        return new InputPath(relative, new IdStep<>(new Step<>("_id")));
    }

    @Override
    public InputPath visitValueInputPath(@NotNull NotaQL2KeyValueInParser.ValueInputPathContext ctx) {
        final LinkedList<InputPathStep> steps = new LinkedList<>();
        steps.add(new IdStep<>(new Step<>("_v")));
        if(ctx.complexTypeStep() != null)
            steps.addAll(visit(ctx.complexTypeStep()).getPathSteps());
        if(ctx.splitStep() != null) {
            steps.add(stepVisitor.visit(ctx.splitStep()));
            steps.add(new AnyBoundCurrentStep());
        }

        return new InputPath(relative, steps);
    }

    @Override
    public InputPath visitListPathStep(@NotNull NotaQL2KeyValueInParser.ListPathStepContext ctx) {
        if(ctx.listMethod() == null)
            return new InputPath(relative, stepVisitor.visit(ctx.listStep()));

        return new InputPath(relative, stepVisitor.visit(ctx.listStep()), stepVisitor.visit(ctx.listMethod()));
    }

    @Override
    public InputPath visitHashPathStep(@NotNull NotaQL2KeyValueInParser.HashPathStepContext ctx) {
        if(ctx.hashMethod() == null)
            return new InputPath(relative, stepVisitor.visit(ctx.hashStep()));

        return new InputPath(relative, stepVisitor.visit(ctx.hashStep()), stepVisitor.visit(ctx.hashMethod()));
    }
}
