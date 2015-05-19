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

import notaql.datamodel.ComplexValue;
import notaql.datamodel.fixation.Fixation;
import notaql.datamodel.fixation.FixationStep;
import notaql.evaluation.ValueEvaluationResult;
import notaql.model.EvaluationException;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The * step. This binds the step.
 */
public class AnyBoundStep implements InputPathStep {
    private static final long serialVersionUID = 6262496393312141399L;

    @Override
    public List<ValueEvaluationResult> evaluate(ValueEvaluationResult step, Fixation contextFixation) {
        // in case the next step in the context is bound we already did something like * / ?() -> don't allow it!
        final FixationStep<?> nextStep = contextFixation.getNextStep(step.getFixation());
        if(nextStep != null && nextStep.isBound())
            throw new EvaluationException("* path step is only allowed on items which are not yet bound - by a * or ?() or $().");

        // in case we don't have a complex value, * won't work
        if(!(step.getValue() instanceof ComplexValue))
            return new LinkedList<>();

        final ComplexValue<?> value = (ComplexValue<?>) step.getValue();
        final Fixation fixation = step.getFixation();

        // return all steps which do not start with "_" - e.g. "_id"
        return value.toMap().entrySet()
                .stream()
                .filter(e -> !(e.getKey().getStep() instanceof String && ((String)e.getKey().getStep()).startsWith("_")))
                .map(
                        e -> new ValueEvaluationResult( // rebuild the evaluation result
                                e.getValue(),
                                new Fixation(fixation, new FixationStep<>(e.getKey(), true)) // extend the fixation with the new step
                        )
                )
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "*";
    }
}
