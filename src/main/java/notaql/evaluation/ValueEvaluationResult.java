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

package notaql.evaluation;

import notaql.datamodel.Value;
import notaql.datamodel.fixation.Fixation;

import java.io.Serializable;

/**
* Used as a way to return fixations with values which get created during the evaluation process.
*/
public class ValueEvaluationResult implements Serializable {
    private static final long serialVersionUID = -2437560373789906863L;
    private Value value;
    private Fixation fixation;

    public ValueEvaluationResult(Value value, Fixation fixation) {
        this.value = value;
        this.fixation = fixation;
    }

    public Value getValue() {
        return value;
    }

    public Fixation getFixation() {
        return fixation;
    }
}
