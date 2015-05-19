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

import java.util.*;

/**
 * The @ step. This refers to a bound step (in the fixation)
 */
public class CurrentBoundStep implements InputPathStep {
    private static final long serialVersionUID = -7940626639435015401L;

    @Override
    public List<ValueEvaluationResult> evaluate(ValueEvaluationResult step, Fixation contextFixation) {
        final Fixation fixation = step.getFixation();
        final FixationStep<?> nextStep = contextFixation.getNextStep(fixation);

        // in case the next step is non existent or at least not bound, the @ operator does not make any sense.
        if(nextStep == null || !nextStep.isBound())
            throw new EvaluationException("@ path step is only allowed on items which actually have a next bound " +
                    "step - you might have forgotten or misplaced an * or ?() or $().");

        // in case we don't have a complex value, @ won't work - This should already be checked by * / ?()
        if(!(step.getValue() instanceof ComplexValue))
            return new LinkedList<>();

        final ComplexValue<?> value = (ComplexValue<?>) step.getValue();

        final Optional<ValueEvaluationResult> result = value.toMap().entrySet()
                .stream()
                .filter(e -> e.getKey().equals(nextStep.getStep()))
                .map(e -> new ValueEvaluationResult(e.getValue(), new Fixation(fixation, nextStep)))
                .findAny();

        assert result.isPresent(): "@ path step was bound to a non-existent step.";

        return Arrays.asList(result.get());
    }

    @Override
    public String toString() {
        return "@";
    }
}
