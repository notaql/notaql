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
import notaql.evaluation.ValueEvaluationResult;

import java.util.List;

/**
 * Just like the IdStep but with a bound step. This is necessary for the column family selection:
 *
 * OUT._r <- IN.children:_c,
 * OUT.$(IN._r) <- IN._v;
 *
 * the second cell access (IN._v) would fail
 */
public class BoundIdStep<T> extends IdStep<T> {
    private static final long serialVersionUID = 420310095055759821L;

    public BoundIdStep(Step<T> id) {
        super(id);
    }

    @Override
    public List<ValueEvaluationResult> evaluate(ValueEvaluationResult step, Fixation contextFixation) {
        return evaluate(step, contextFixation, true);
    }

    @Override
    public String toString() {
        return super.toString() + "(BOUND)";
    }
}
