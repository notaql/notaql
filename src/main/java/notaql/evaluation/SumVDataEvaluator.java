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

import notaql.datamodel.NumberValue;
import notaql.datamodel.Value;
import notaql.datamodel.fixation.Fixation;
import notaql.evaluation.values.PartialNumberValue;
import notaql.model.vdata.VData;
import notaql.model.vdata.aggregation.SumVData;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sums up numeric values.
 */
public class SumVDataEvaluator implements Evaluator, Reducer {
    private static final long serialVersionUID = 7140445940159667709L;

    @Override
    public List<ValueEvaluationResult> evaluate(VData vData, Fixation fixation) {
        assert vData instanceof SumVData;
        final SumVData sumVData = (SumVData) vData;

        final VData path = sumVData.getExpression();

        final List<ValueEvaluationResult> results = EvaluatorService.getInstance().evaluate(path, fixation);

        final List<Value> values = results.stream().map(ValueEvaluationResult::getValue).collect(Collectors.toList());

        return Arrays.asList(new ValueEvaluationResult(new PartialNumberValue(sumVData.aggregate(values).getValue()), fixation));
    }

    @Override
    public boolean canReduce(VData vData) {
        return true;
    }

    @Override
    public PartialNumberValue reduce(VData vData, Value v1, Value v2) {
        assert v1 instanceof PartialNumberValue && v2 instanceof PartialNumberValue;
        return new PartialNumberValue(((PartialNumberValue)v1).getValue().doubleValue() + ((PartialNumberValue)v2).getValue().doubleValue());
    }

    @Override
    public Value createIdentity(VData vData) {
        return new PartialNumberValue(0);
    }

    @Override
    public Value finalize(VData vData, Value value) {
        assert value instanceof PartialNumberValue;
        return new NumberValue(((PartialNumberValue)value).getValue());
    }

    @Override
    public List<Class<? extends VData>> getProcessedClasses() {
        return Arrays.asList(SumVData.class);
    }
}
