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

package notaql.engines.csv.evaluation;

import notaql.datamodel.*;
import notaql.datamodel.fixation.Fixation;
import notaql.engines.csv.model.vdata.ColCountFunctionVData;
import notaql.evaluation.ValueEvaluationResult;
import notaql.evaluation.Evaluator;
import notaql.model.vdata.VData;

import java.util.Arrays;
import java.util.List;

/**
 * Provides a simple function to count the columns.
 */
public class ColCountFunctionVDataEvaluator implements Evaluator {
    private static final long serialVersionUID = 2299508731518757009L;

    /**
     * Simply count all attributes in the row.
     *
     * @param vData
     * @param fixation
     * @return
     */
    @Override
    public List<ValueEvaluationResult> evaluate(VData vData, Fixation fixation) {
        assert vData instanceof ColCountFunctionVData;

        assert fixation.getRootValue() instanceof ObjectValue;
        // get root value
        final ObjectValue value = (ObjectValue) fixation.getRootValue();
        // count its attributes
        return Arrays.asList(new ValueEvaluationResult(new NumberValue(value.size()), fixation));
    }

    /**
     * This function does no aggregation and therefore provides no reduce
     * @param vData
     * @return
     */
    @Override
    public boolean canReduce(VData vData) {
        return false;
    }

    @Override
    public List<Class<? extends VData>> getProcessedClasses() {
        return Arrays.asList(ColCountFunctionVData.class);
    }
}
