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
import notaql.datamodel.Step;
import notaql.datamodel.fixation.Fixation;
import notaql.datamodel.fixation.FixationStep;
import notaql.evaluation.ValueEvaluationResult;

import java.util.*;

/**
 * Allows to define a constant step. This step does not bind.
 */
public class IdStep<T> implements InputPathStep, OutputPathStep {
    private static final long serialVersionUID = 4129182008946537584L;
    private Step<T> id;

    public IdStep(Step<T> id) {
        this.id = id;
    }

    @Override
    public List<ValueEvaluationResult> evaluate(ValueEvaluationResult step, Fixation contextFixation) {
        return evaluate(step, contextFixation, false);
    }

    @Override
    public List<StepNameEvaluationResult> evaluateStepName(StepNameEvaluationResult step, Fixation fixation) {
        return Arrays.asList(new StepNameEvaluationResult(fixation, step, id));
    }

    @Override
    public String toString() {
        return id.toString();
    }

    List<ValueEvaluationResult> evaluate(ValueEvaluationResult step, Fixation contextFixation, boolean bound) {
        // in case we don't have a complex value, this won't work
        if(!(step.getValue() instanceof ComplexValue))
            return new LinkedList<>();

        final ComplexValue<?> value = (ComplexValue<?>) step.getValue();

        final Optional<ValueEvaluationResult> result = value.toMap().entrySet()
                .stream()
                .filter(e -> e.getKey().equals(id))
                .map(e -> new ValueEvaluationResult(e.getValue(), new Fixation(step.getFixation(), new FixationStep<>(id, bound))))
                .findAny();

        // in case this argument does not exist: just keep silent. We do not have a schema! :)
        if(!result.isPresent())
            return new LinkedList<>();

        return Arrays.asList(result.get());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IdStep idStep = (IdStep) o;

        if (!id.equals(idStep.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
