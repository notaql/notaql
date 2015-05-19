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

package notaql.model.path;

import notaql.datamodel.*;
import notaql.datamodel.fixation.Fixation;
import notaql.evaluation.ValueEvaluationResult;
import notaql.model.EvaluationException;

import java.util.Arrays;
import java.util.List;

/**
 * This .name() step is used to get the name of a field in an object.
 */
public class NamePathMethodStep implements PathMethodStep {
    private static final long serialVersionUID = 6830407970932987342L;

    @Override
    public List<ValueEvaluationResult> evaluate(ValueEvaluationResult step, Fixation contextFixation) {
        final Value value = step.getValue();
        final ComplexValue<?> parent = value.getParent();

        if(!(parent instanceof ObjectValue))
            throw new EvaluationException(".name() was called on a value which is not in an object.");

        final Step<String> stringStep = ((ObjectValue) parent).getStep(value);

        return Arrays.asList(
                new ValueEvaluationResult(
                        new StringValue(stringStep.getStep()),
                        step.getFixation()
                )
        );
    }

    @Override
    public String toString() {
        return "name()";
    }
}
