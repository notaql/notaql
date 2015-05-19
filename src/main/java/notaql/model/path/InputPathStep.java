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
import java.util.List;

/**
 * Input paths consist of different steps. This is the base interface for the steps (e.g. IN.a[*].*.name() consists of 4 steps)
 */
public interface InputPathStep extends Serializable {
    /**
     * Evaluate this step providing all the matched values and the fixations necessary to reach them.
     *
     * @param step
     * @param contextFixation   The fixation that was in context before the evaluation of the InputPath started
     * @return
     */
    public List<ValueEvaluationResult> evaluate(ValueEvaluationResult step, Fixation contextFixation);
}
