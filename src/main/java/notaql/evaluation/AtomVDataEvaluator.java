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

import notaql.datamodel.fixation.Fixation;
import notaql.model.vdata.AtomVData;
import notaql.model.vdata.VData;

import java.util.Arrays;
import java.util.List;

/**
 * Evaluates atomic expressions (e.g. 'Hello')
 */
public class AtomVDataEvaluator implements Evaluator {
    private static final long serialVersionUID = -4179936655213482124L;

    @Override
    public List<ValueEvaluationResult> evaluate(VData vData, Fixation fixation) {
        assert vData instanceof AtomVData;
        final AtomVData atomVData = (AtomVData) vData;
        return Arrays.asList(new ValueEvaluationResult(atomVData.getValue(), fixation));
    }

    @Override
    public boolean canReduce(VData vData) {
        return false;
    }

    @Override
    public List<Class<? extends VData>> getProcessedClasses() {
        return Arrays.asList(AtomVData.class);
    }
}
