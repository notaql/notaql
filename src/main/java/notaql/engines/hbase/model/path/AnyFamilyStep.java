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

package notaql.engines.hbase.model.path;

import notaql.datamodel.ComplexValue;
import notaql.datamodel.Step;
import notaql.datamodel.fixation.Fixation;
import notaql.datamodel.fixation.FixationStep;
import notaql.evaluation.ValueEvaluationResult;
import notaql.model.path.InputPathStep;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Any family step
 *
 * This sort of step is necessary because of this stupid thing:
 *
 * OUT._r <- IN._c,
 * OUT.$(IN.new_col) <- IN._v;
 *
 * This means that this only has to be used on constant column access (TODO: is this assumption correct?)
 *
 * XXX: USE THIS WITH MUCH CAUSION. THIS SHOULD NOT BE REUSED!!!!
 * FIXME: this might not work because onbound fixations are ignored at some point????
 */
public class AnyFamilyStep implements InputPathStep {
    private static final long serialVersionUID = 6118924185712503718L;

    @Override
    public List<ValueEvaluationResult> evaluate(ValueEvaluationResult step, Fixation contextFixation) {
        final FixationStep<?> nextStep = contextFixation.getNextStep(step.getFixation());

        // in case we don't have a complex value, any step won't work
        if(!(step.getValue() instanceof ComplexValue))
            return new LinkedList<>();

        final ComplexValue<?> value = (ComplexValue<?>) step.getValue();
        final Fixation fixation = step.getFixation();

        // FIXME: Something here is wrong.
        if(nextStep != null && nextStep instanceof AnyFamilyFixationStep<?>) {
            final Optional<ValueEvaluationResult> result = value.toMap().entrySet()
                    .stream()
                    .filter(e -> e.getKey().equals(nextStep.getStep()))
                    .map(e -> new ValueEvaluationResult(e.getValue(), new Fixation(fixation, nextStep)))
                    .findAny();

            assert result.isPresent(): "ANY-FAMILY() path step was bound to a non-existent step.";

            return Arrays.asList(result.get());
        }

        return value.toMap().entrySet()
                .stream()
                .map(
                        e -> new ValueEvaluationResult( // rebuild the evaluation result
                                e.getValue(),
                                new Fixation(fixation, new AnyFamilyFixationStep<>(e.getKey())) // extend the fixation with the new step
                        )
                )
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "ANY-FAMILY()";
    }
}


class AnyFamilyFixationStep<T> extends FixationStep<T> {

    private static final long serialVersionUID = 6157198833049399384L;

    /**
     * @param step
     */
    public AnyFamilyFixationStep(Step<T> step) {
        super(step, false);
    }
}
