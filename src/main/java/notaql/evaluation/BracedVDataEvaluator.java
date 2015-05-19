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
import notaql.model.vdata.BracedVData;
import notaql.model.vdata.VData;

import java.util.Arrays;
import java.util.List;

/**
 * Evaluates braced expressions. This simply forwards the evaluation.
 */
public class BracedVDataEvaluator implements Evaluator, Reducer {
    @Override
    public List<ValueEvaluationResult> evaluate(VData vData, Fixation fixation) {
        assert vData instanceof BracedVData;

        return EvaluatorService.getInstance().evaluate(vData, fixation);
    }

    @Override
    public boolean canReduce(VData vData) {
        return EvaluatorService.getInstance().canReduce(vData);
    }

    @Override
    public Value reduce(VData vData, Value v1, Value v2) {
        return EvaluatorService.getInstance().reduce(vData, v1, v2);
    }

    @Override
    public Value createIdentity(VData vData) {
        return EvaluatorService.getInstance().createIdentity(vData);
    }

    @Override
    public Value finalize(VData vData, Value value) {
        return EvaluatorService.getInstance().finalize(vData, value);
    }

    @Override
    public List<Class<? extends VData>> getProcessedClasses() {
        return Arrays.asList(BracedVData.class);
    }
}
