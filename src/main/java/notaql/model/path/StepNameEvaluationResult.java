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

import notaql.datamodel.Step;
import notaql.datamodel.fixation.Fixation;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides a way to denote which shall be the output path while providing fixations created in the creation of that
 * (e.g. by using $() in an output path).
 */
public class StepNameEvaluationResult {
    private List<Step<?>> steps;
    private Fixation fixation;

    public StepNameEvaluationResult(Fixation fixation) {
        this.steps = new LinkedList<>();
        this.fixation = fixation;
    }

    public StepNameEvaluationResult(Fixation fixation, List<Step<?>> steps) {
        this.steps = steps;
        this.fixation = fixation;
    }

    public StepNameEvaluationResult(Fixation fixation, Step<?>... steps) {
        this(fixation, Arrays.asList(steps));
    }

    public StepNameEvaluationResult(Fixation fixation, StepNameEvaluationResult lead, Step<?> step) {
        this.fixation = fixation;
        this.steps = new LinkedList<>(lead.steps);
        this.steps.add(step);
    }

    public List<Step<?>> getSteps() {
        return steps;
    }

    public Fixation getFixation() {
        return fixation;
    }

    @Override
    public String toString() {
        return "StepNameEvaluationResult{" +
                "steps=" + steps +
                ", fixation=" + fixation +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StepNameEvaluationResult that = (StepNameEvaluationResult) o;

        if (fixation != null ? !fixation.equals(that.fixation) : that.fixation != null) return false;
        if (steps != null ? !steps.equals(that.steps) : that.steps != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = steps != null ? steps.hashCode() : 0;
        result = 31 * result + (fixation != null ? fixation.hashCode() : 0);
        return result;
    }
}
