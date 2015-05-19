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

import notaql.datamodel.AtomValue;
import notaql.datamodel.StringValue;
import notaql.datamodel.Value;
import notaql.datamodel.fixation.Fixation;
import notaql.evaluation.values.PartialListValue;
import notaql.model.EvaluationException;
import notaql.model.vdata.VData;
import notaql.model.vdata.aggregation.ImplodeVData;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Evaluates the IMPLODE() function, wich allows concatenating values with a provided separator.
 */
public class ImplodeVDataEvaluator implements Evaluator, Reducer {
    private static final long serialVersionUID = -1145605387730529939L;

    @Override
    public List<ValueEvaluationResult> evaluate(VData vData, Fixation fixation) {
        assert vData instanceof ImplodeVData;
        final ImplodeVData implodeVData = (ImplodeVData) vData;

        final VData path = implodeVData.getExpression();

        final List<ValueEvaluationResult> results = EvaluatorService.getInstance().evaluate(path, fixation);

        if(results.stream().anyMatch(r -> !(r.getValue() instanceof AtomValue)))
            throw new EvaluationException("IMPLODE aggregation function encountered values which are not numbers or strings.");

        final PartialListValue result = new PartialListValue();

        results.stream()
                .map(r -> new StringValue(((AtomValue) r.getValue()).getValue().toString()))
                        .forEach(result::add);

        return Arrays.asList(new ValueEvaluationResult(result, fixation));
    }

    @Override
    public boolean canReduce(VData vData) {
        return true;
    }

    @Override
    public PartialListValue reduce(VData vData, Value v1, Value v2) {
        assert vData instanceof ImplodeVData && v1 instanceof PartialListValue && v2 instanceof PartialListValue;
        final PartialListValue result = new PartialListValue();

        result.addAll((PartialListValue)v1);
        result.addAll((PartialListValue)v2);

        return result;
    }

    @Override
    public Value createIdentity(VData vData) {
        return new PartialListValue();
    }

    @Override
    public Value finalize(VData vData, Value value) {
        assert vData instanceof ImplodeVData;
        assert value instanceof PartialListValue;
        final ImplodeVData implodeVData = (ImplodeVData) vData;

        final PartialListValue partialListValue = (PartialListValue) value;

        final String join = partialListValue.stream()
                .map(v -> ((StringValue) v).getValue())
                .collect(Collectors.joining(implodeVData.getSeperator().getValue()));

        return new StringValue(join);
    }

    @Override
    public List<Class<? extends VData>> getProcessedClasses() {
        return Arrays.asList(ImplodeVData.class);
    }
}
