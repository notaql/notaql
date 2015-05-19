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

import notaql.datamodel.fixation.Fixation;
import notaql.evaluation.ValueEvaluationResult;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides the possibility to denote where data can be found in the input.
 *
 * This also allows evaluation given documents.
 */
public class InputPath implements Serializable {
    private static final long serialVersionUID = -320558093469339566L;
    private boolean relative = false;
    private List<InputPathStep> pathSteps;

    public InputPath(List<InputPathStep> pathSteps) {
        this.pathSteps = pathSteps;
    }

    public InputPath(InputPathStep... pathSteps) {
        this.pathSteps = Arrays.asList(pathSteps);
    }

    public InputPath(boolean relative, List<InputPathStep> pathSteps) {
        this.relative = relative;
        this.pathSteps = pathSteps;
    }

    public InputPath(boolean relative, InputPathStep... pathSteps) {
        this.relative = relative;
        this.pathSteps = Arrays.asList(pathSteps);
    }

    public List<InputPathStep> getPathSteps() {
        return pathSteps;
    }

    public boolean isRelative() {
        return relative;
    }

    /**
     * This method is necessary for absolute paths
     *
     * @param fixation
     * @return
     */
    public List<ValueEvaluationResult> evaluate(Fixation fixation) {
        return evaluate(
                new ValueEvaluationResult(fixation.getRootValue(), new Fixation(fixation.getRootValue())),
                fixation
        );
    }

    /**
     * This method is necessary for evaluating relative input paths
     *
     * 1. check if there is another step
     * 1. true: if we have non-complex values kick them out
     * 2. set the values as the current values and continue with the next step.
     * 3. After all steps are done build fixations for the values
     *
     * @param step
     * @param fixation
     * @return
     */
    public List<ValueEvaluationResult> evaluate(ValueEvaluationResult step, Fixation fixation) {
        List<ValueEvaluationResult> currentSteps = Arrays.asList(
                new ValueEvaluationResult(step.getValue(), step.getFixation())
        );

        // if we simply have "@" return the current value in question
        if(pathSteps.size() == 0 && relative)
            return Arrays.asList(step);

        final Iterator<InputPathStep> pathIterator = pathSteps.iterator();

        while (pathIterator.hasNext()) {
            final InputPathStep pathStep = pathIterator.next();

            final Stream<ValueEvaluationResult> evaluationStepStream = currentSteps
                    .stream()
                    .map(s -> pathStep.evaluate(s, fixation))
                    .flatMap(s -> s.stream());

            if (pathIterator.hasNext()) {
                currentSteps = evaluationStepStream
                        .collect(Collectors.toList());
                continue;
            }

            return evaluationStepStream.collect(Collectors.toList());
        }

        return new LinkedList<>();
    }

    @Override
    public String toString() {
        return (relative?"@.":"IN.") + pathSteps.stream().map(Object::toString).collect(Collectors.joining("."));
    }
}
