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

import java.io.Serializable;
import java.util.List;

/**
 * An output path may consist of multiple steps (TODO: Only this is needed. The OutputPath is only necessary in the parser -> rename this and make Arguments just take this as a name)
 * This is the base interface for such a step.
 */
public interface OutputPathStep extends Serializable {

    /**
     * Provides the names of this step for target path determination
     * @param step
     * @param fixation
     * @return
     */
    public List<StepNameEvaluationResult> evaluateStepName(StepNameEvaluationResult step, Fixation fixation);
}
