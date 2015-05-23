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

package notaql.engines.json.path;

import notaql.datamodel.Step;
import notaql.model.path.IdStep;
import notaql.model.path.IgnoredIdStep;
import notaql.model.path.OutputPathStep;
import notaql.model.path.ResolvedIdStep;
import notaql.parser.TransformationParser;
import notaql.parser.antlr.NotaQL2DocumentOutBaseVisitor;
import notaql.parser.antlr.NotaQL2DocumentOutParser;
import org.antlr.v4.runtime.misc.NotNull;

/**
 * Created by Thomas Lottermann on 03.12.14.
 */
public class JSONOutputPathStepVisitor extends NotaQL2DocumentOutBaseVisitor<OutputPathStep> {
    private final TransformationParser parser;

    public JSONOutputPathStepVisitor(TransformationParser parser) {
        this.parser = parser;
    }

    @Override
    public OutputPathStep visitAttributeIdOutputAttributeStep(@NotNull NotaQL2DocumentOutParser.AttributeIdOutputAttributeStepContext ctx) {
        if(ctx.attributeId().attributeName.getText().equals("_"))
            return new IgnoredIdStep();
        return new IdStep<>(new Step<>(ctx.attributeId().attributeName.getText()));
    }

    @Override
    public OutputPathStep visitIdOutputAttributeStep(@NotNull NotaQL2DocumentOutParser.IdOutputAttributeStepContext ctx) {
        return new IdStep<>(new Step<>("_id"));
    }

    @Override
    public OutputPathStep visitResolvedOutputAttributeStep(@NotNull NotaQL2DocumentOutParser.ResolvedOutputAttributeStepContext ctx) {
        return new ResolvedIdStep(parser.getInputPathVisitor().evaluateAbsolute(ctx.resolvedAttributeId().absoluteInputPath().path().getText()));
    }
}
