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

import notaql.datamodel.ListValue;
import notaql.datamodel.Step;
import notaql.datamodel.Value;
import notaql.datamodel.fixation.Fixation;
import notaql.evaluation.values.PartialListValue;
import notaql.model.vdata.VData;
import notaql.model.vdata.aggregation.ListVData;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Aggregates elements into a list by equality.
 */
public class ListVDataEvaluator implements Evaluator, Reducer {
    private static final long serialVersionUID = 5667219569115230965L;

    @Override
    public List<ValueEvaluationResult> evaluate(VData vData, Fixation fixation) {
        assert vData instanceof ListVData;
        final ListVData listVData = (ListVData) vData;

        final PartialListValue values = new PartialListValue();

        values.addAll(
                EvaluatorService.getInstance().evaluate(listVData.getExpression(), fixation)
                        .stream()
                        .map(ValueEvaluationResult::getValue)
                        .collect(Collectors.toList())
        );

        return Arrays.asList(new ValueEvaluationResult(values, fixation));
    }

    @Override
    public boolean canReduce(VData vData) {
        return true;
    }

    /**
     * TODO: reuse v1?
     *
     * NOTE: It is necessary that v1 does not contain any equal values (already reduced)
     * @param vData
     * @param v1
     * @param v2
     * @return
     */
    @Override
    public PartialListValue reduce(VData vData, Value v1, Value v2) {
        assert vData instanceof ListVData && v1 instanceof PartialListValue && v2 instanceof PartialListValue;

        final ListVData listVData = (ListVData) vData;
        final PartialListValue l1 = (PartialListValue) v1;
        final PartialListValue l2 = (PartialListValue) v2;

        final PartialListValue result = new PartialListValue();

        final Map<String, Value> mergeMap = l1.toMap().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getValue().groupKey(), Map.Entry::getValue));

        l2.forEach(item -> {
            final String code = item.groupKey();
            if (!mergeMap.containsKey(code)) {
                mergeMap.put(item.groupKey(), item);
                return;
            } else if (!EvaluatorService.getInstance().canReduce(listVData.getExpression())) {
                return;
            }

            final Value equalItem = mergeMap.get(code);

            final Value reduce = EvaluatorService.getInstance().reduce(listVData.getExpression(), equalItem, item);

            mergeMap.put(code, reduce);
        });

        result.addAll(mergeMap.values());

        return result;
    }

    @Override
    public Value createIdentity(VData vData) {
        return new PartialListValue();
    }

    @Override
    public Value finalize(VData vData, Value value) {
        assert value instanceof PartialListValue;
        final PartialListValue partialListValue = (PartialListValue) value;
        final ListVData listVData = (ListVData) vData;

        final ListValue result = new ListValue();

        if(!EvaluatorService.getInstance().canReduce(listVData.getExpression())) {
            result.addAll(partialListValue);
            return result;
        }

        partialListValue.forEach(v -> result.add(EvaluatorService.getInstance().finalize(listVData.getExpression(), v)));
        return result;
    }

    @Override
    public List<Class<? extends VData>> getProcessedClasses() {
        return Arrays.asList(ListVData.class);
    }
}
