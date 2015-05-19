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
import notaql.evaluation.values.PartialAvgValue;
import notaql.model.EvaluationException;
import notaql.model.vdata.VData;
import notaql.model.vdata.aggregation.AvgVData;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Evaluates the AVG() function.
 */
public class AvgVDataEvaluator implements Evaluator, Reducer {
    private static final long serialVersionUID = -1334411928804069430L;

    /**
     * Evaluate averages. This only operates on Number values.
     *
     * This produces pre-aggregated values which still contain a weight.
     * @param vData
     * @param fixation
     * @return
     */
    @Override
    public List<ValueEvaluationResult> evaluate(VData vData, Fixation fixation) {
        assert vData instanceof AvgVData;
        final AvgVData avgVData = (AvgVData) vData;

        final VData argument = avgVData.getExpression();

        final List<ValueEvaluationResult> results = EvaluatorService.getInstance().evaluate(argument, fixation);

        final List<Value> values = results.stream().map(ValueEvaluationResult::getValue).collect(Collectors.toList());

        if (values.stream().anyMatch(v -> !(v instanceof NumberValue)))
            throw new EvaluationException("AVG aggregation function encountered values which are not numbers.");

        final PartialAvgValue avgValue = new PartialAvgValue(
                values.stream()
                        .map(a -> ((NumberValue) a).getValue())
                        .collect(Collectors.averagingDouble(Number::doubleValue)),
                values.size()
        );

        return Arrays.asList(new ValueEvaluationResult(avgValue, fixation));
    }

    @Override
    public boolean canReduce(VData vData) {
        return true;
    }

    /**
     * Adds the values with the correct weights.
     *
     * @param vData The instance of vData, that this reducer is built for
     * @param v1
     * @param v2
     * @return
     */
    @Override
    public PartialAvgValue reduce(VData vData, Value v1, Value v2) {
        assert v1 instanceof PartialAvgValue && v2 instanceof PartialAvgValue;

        final PartialAvgValue pv1 = (PartialAvgValue) v1;
        final PartialAvgValue pv2 = (PartialAvgValue) v2;

        if(pv1.getCount() == 0)
            return pv2;

        if(pv2.getCount() == 0)
            return pv1;

        int count = (pv1.getCount() + pv2.getCount());

        return new PartialAvgValue(
                (pv1.getValue().doubleValue() / count * pv1.getCount() + pv2.getValue().doubleValue() / count * pv2.getCount()),
                pv1.getCount() + pv2.getCount()
        );
    }

    @Override
    public Value createIdentity(VData vData) {
        return new PartialAvgValue(0, 0);
    }

    @Override
    public Value finalize(VData vData, Value value) {
        assert value instanceof PartialAvgValue;
        return new NumberValue(((PartialAvgValue)value).getValue());
    }

    @Override
    public List<Class<? extends VData>> getProcessedClasses() {
        return Arrays.asList(AvgVData.class);
    }
}
