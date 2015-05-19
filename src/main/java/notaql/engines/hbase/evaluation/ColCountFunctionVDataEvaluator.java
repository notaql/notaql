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

package notaql.engines.hbase.evaluation;

import notaql.datamodel.*;
import notaql.datamodel.fixation.Fixation;
import notaql.engines.hbase.model.vdata.ColCountFunctionVData;
import notaql.evaluation.ValueEvaluationResult;
import notaql.evaluation.Evaluator;
import notaql.evaluation.EvaluatorService;
import notaql.model.EvaluationException;
import notaql.model.vdata.VData;
import notaql.evaluation.values.ResolvingUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Provides a simple function to count the columns in the given row.
 */
public class ColCountFunctionVDataEvaluator implements Evaluator {
    private static final long serialVersionUID = 2299508731518757009L;

    @Override
    public List<ValueEvaluationResult> evaluate(VData vData, Fixation fixation) {
        assert vData instanceof ColCountFunctionVData;

        final ColCountFunctionVData countFunctionVData = (ColCountFunctionVData) vData;

        final VData colFamilyVData = countFunctionVData.getExpression();

        final ValueEvaluationResult familyResult;

        // get family if provided
        if(colFamilyVData != null) {
            final List<ValueEvaluationResult> eval = EvaluatorService.getInstance().evaluate(colFamilyVData, fixation);
            if(eval.size() != 1)
                throw new EvaluationException("The column family that was provided to COL_COUNT was not distinct");
            familyResult = eval.get(0);
            if(ResolvingUtils.isUnresolved(familyResult.getValue()))
                throw new EvaluationException("Aggregation functions are not allowed in COL_COUNT");
            if(!(familyResult.getValue() instanceof StringValue))
                throw new EvaluationException("COL_COUNT() expects a string value as a family argument, got: " + familyResult.getValue().toString());
        } else {
            familyResult = null;
        }

        assert fixation.getRootValue() instanceof ObjectValue;

        final ObjectValue value = (ObjectValue) fixation.getRootValue();

        // if no family is provided: count all columns
        if(familyResult == null) {
            final Optional<Integer> reduce = value.toMap().entrySet()
                    .stream()
                    .filter(e -> e.getValue() instanceof ObjectValue)
                    .map(e -> ((ObjectValue) e.getValue()).size())
                    .reduce((a, b) -> a + b);
            assert reduce.isPresent();
            return Arrays.asList(new ValueEvaluationResult(new NumberValue(reduce.get()), fixation));
        }

        // if a family is provided count only the columns in there
        final StringValue familyResultValue = (StringValue) familyResult.getValue();

        final Value family = value.get(new Step<>(familyResultValue.getValue()));

        assert family instanceof ObjectValue;

        return Arrays.asList(new ValueEvaluationResult(new NumberValue(((ObjectValue)family).size()), familyResult.getFixation()));
    }

    @Override
    public boolean canReduce(VData vData) {
        return false;
    }

    @Override
    public List<Class<? extends VData>> getProcessedClasses() {
        return Arrays.asList(ColCountFunctionVData.class);
    }
}
