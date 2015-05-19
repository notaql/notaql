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
import notaql.model.predicate.Predicate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This is a predicate step (e.g. ?(@.b = 5))
 */
public class PredicateStep implements InputPathStep {
    private static final long serialVersionUID = -8858396804684753673L;
    private Predicate predicate;

    public PredicateStep(Predicate predicate) {
        this.predicate = predicate;
    }

    @Override
    public List<ValueEvaluationResult> evaluate(ValueEvaluationResult step, Fixation contextFixation) {
        // in case the next step in the context is bound we already did something like * / ?() -> don't allow it!
        final FixationStep<?> nextStep = contextFixation.getNextStep(step.getFixation());
        if(nextStep != null && nextStep.isBound())
            throw new EvaluationException("A predicate path step is only allowed on items which are not yet bound - by a * or ?() or $().");

        // in case we don't have a complex value, * won't work
        if(!(step.getValue() instanceof ComplexValue))
            return new LinkedList<>();

        final ComplexValue<?> value = (ComplexValue<?>) step.getValue();
        final Fixation fixation = step.getFixation();

        return value.toMap().entrySet()
                .stream()
                .filter(e -> predicate.evaluate(new ValueEvaluationResult(e.getValue(), step.getFixation()), contextFixation))
                .map(
                        e -> new ValueEvaluationResult(
                                e.getValue(),
                                new Fixation(fixation, new FixationStep<>(e.getKey(), true)) // extend the fixation with the new step
                        )
                )
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "?(" + predicate.toString() + ")";
    }
}
