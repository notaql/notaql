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
import notaql.model.path.IdStep;
import notaql.model.path.OutputPath;
import notaql.model.path.ResolvedIdStep;
import notaql.parser.TransformationParser;
import notaql.parser.antlr.NotaQL2KeyValueOutBaseVisitor;
import notaql.parser.antlr.NotaQL2KeyValueOutParser;
import org.antlr.v4.runtime.misc.NotNull;

/**
 * Created by Thomas Lottermann on 03.12.14.
 */
public class RedisOutputPathVisitor extends NotaQL2KeyValueOutBaseVisitor<OutputPath> {
    private final TransformationParser parser;

    public RedisOutputPathVisitor(TransformationParser parser) {
        this.parser = parser;
    }

    @Override
    public OutputPath visitKeyOutputPath(@NotNull NotaQL2KeyValueOutParser.KeyOutputPathContext ctx) {
        return new OutputPath(new IdStep<>(new Step<>("_id")));
    }

    @Override
    public OutputPath visitValueOutputPath(@NotNull NotaQL2KeyValueOutParser.ValueOutputPathContext ctx) {
        return new OutputPath(new IdStep<>(new Step<>("_v")));
    }

    @Override
    public OutputPath visitKeyIdOutputPath(@NotNull NotaQL2KeyValueOutParser.KeyIdOutputPathContext ctx) {
        return new OutputPath(new IdStep<>(new Step<>("_v")), new IdStep<>(new Step<>(ctx.keyId().getText())));
    }

    @Override
    public OutputPath visitResolvedOutputPath(@NotNull NotaQL2KeyValueOutParser.ResolvedOutputPathContext ctx) {
        return new OutputPath(new ResolvedIdStep(parser.getInputPathVisitor().evaluateAbsolute(ctx.absoluteInputPath().path().getText())));
    }
}
