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

import notaql.datamodel.AtomValue;
import notaql.datamodel.ComplexValue;
import notaql.datamodel.Step;
import notaql.datamodel.fixation.Fixation;
import notaql.datamodel.fixation.FixationStep;
import notaql.evaluation.ValueEvaluationResult;
import notaql.model.EvaluationException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This represents the resolving of a path in order to support dereferencing.
 *
 * e.g.
 * OUT._id <- IN._id
 * OUT.deref <- IN.$(IN.ref)
 */
public class ResolvedIdStep implements InputPathStep, OutputPathStep {
    private static final long serialVersionUID = -6935715405397188374L;
    private final InputPath path;

    public ResolvedIdStep(InputPath path) {
        this.path = path;
    }

    @Override
    public List<ValueEvaluationResult> evaluate(ValueEvaluationResult step, Fixation contextFixation) {
        // in case the next step in the context is bound we already did something like * / ?() / $() -> don't allow it!
        final FixationStep<?> nextStep = contextFixation.getNextStep(step.getFixation());
        if(nextStep != null && nextStep.isBound())
            throw new EvaluationException("A resolved path step is only allowed on items which are not yet bound - by a * or ?() or $().");

        // in case we don't have a complex value, $ won't work
        if(!(step.getValue() instanceof ComplexValue))
            return new LinkedList<>();

        final ComplexValue<?> value = (ComplexValue<?>) step.getValue();
        final Fixation fixation = step.getFixation();

        // get the possible ids
        final List<ValueEvaluationResult> resolved = path.evaluate(fixation);
        final List<?> ids = resolved
                .stream()
                .filter(r -> r.getValue() instanceof AtomValue)
                .map(r -> (AtomValue) r.getValue())
                .map(AtomValue::getValue)
                .collect(Collectors.toList());

        // include the results which match any id
        return value.toMap().entrySet()
                .stream()
                .filter(e -> ids.contains(e.getKey().getStep()))
                .map(
                        e -> new ValueEvaluationResult(
                                e.getValue(),
                                new Fixation(fixation, new FixationStep<>(e.getKey(), true)) // extend the fixation with the new step
                        )
                )
                .collect(Collectors.toList());
    }

    @Override
    public List<StepNameEvaluationResult> evaluateStepName(StepNameEvaluationResult step, Fixation fixation) {
        // get the possible names and return them
        final List<ValueEvaluationResult> resolved = path.evaluate(fixation);
        return resolved
                .stream()
                .filter(r -> r.getValue() instanceof AtomValue)
                .map(
                        r -> new StepNameEvaluationResult(
                                r.getFixation(),
                                step,
                                new Step<>(((AtomValue<?>)r.getValue()).getValue())
                        )
                )
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "$(" + (path!=null?path.toString():"null") + ")";
    }
}
