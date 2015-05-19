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
import notaql.datamodel.Value;
import notaql.datamodel.fixation.Fixation;
import notaql.evaluation.values.PartialAtomValue;
import notaql.evaluation.values.PartialNullValue;
import notaql.model.EvaluationException;
import notaql.model.vdata.VData;
import notaql.model.vdata.aggregation.ExtremumVData;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Executes MIN and MAX functions
 */
public class ExtremumVDataEvaluator implements Evaluator, Reducer {

    private static final long serialVersionUID = -8211952015804397201L;

    @Override
    public List<ValueEvaluationResult> evaluate(VData vData, Fixation fixation) {
        assert vData instanceof ExtremumVData;
        final ExtremumVData extremumVData = (ExtremumVData) vData;

        final VData path = extremumVData.getExpression();

        final List<ValueEvaluationResult> results = EvaluatorService.getInstance().evaluate(path, fixation);

        final List<Value> values = results.stream().map(ValueEvaluationResult::getValue).collect(Collectors.toList());

        if (values
                .stream()
                .anyMatch(v -> !(v instanceof AtomValue) || !(((AtomValue) v).getValue() instanceof Comparable))
                )
            throw new EvaluationException(extremumVData.getFunction() + " aggregation function encountered values which are not comparable.");

        final Stream<AtomValue> atomValueStream = values.stream().map(v -> (AtomValue) v);

        final Optional<AtomValue> extremum;
        switch (extremumVData.getFunction()) {
            case MIN:
                extremum = atomValueStream.min(new ExtremumVData.AtomValueComparator());
                break;
            case MAX:
                extremum = atomValueStream.max(new ExtremumVData.AtomValueComparator());
                break;
            default:
                extremum = null;
                assert false: "Unknown extremum operator: " + extremumVData.getFunction().toString();
        }


        if (extremum.isPresent())
            return Arrays.asList(new ValueEvaluationResult(new PartialAtomValue<>(extremum.get()), fixation));

        return Arrays.asList(new ValueEvaluationResult(new PartialNullValue(), fixation));
    }

    @Override
    public boolean canReduce(VData vData) {
        return true;
    }

    @Override
    public Value reduce(VData vData, Value v1, Value v2) {
        assert vData instanceof ExtremumVData;

        if(v1 instanceof PartialNullValue)
            return v2;

        if(v2 instanceof PartialNullValue)
            return v1;

        assert v1 instanceof PartialAtomValue && v2 instanceof PartialAtomValue;

        final PartialAtomValue pv1 = (PartialAtomValue) v1;
        final PartialAtomValue pv2 = (PartialAtomValue) v2;

        final ExtremumVData extremumVData = (ExtremumVData) vData;

        final int compare = new ExtremumVData.AtomValueComparator().compare(pv1, pv2);

        switch (extremumVData.getFunction()) {
            case MIN:
                if(compare >= 0)
                    return pv2;
                return pv1;
            case MAX:
                if(compare >= 0)
                    return pv1;
                return pv2;
            default:
                throw new AssertionError("Unknown extremum operator: " + extremumVData.getFunction().toString());
        }
    }

    @Override
    public Value createIdentity(VData vData) {
        return new PartialNullValue();
    }

    @Override
    public Value finalize(VData vData, Value value) {
        assert value instanceof PartialAtomValue;
        return ((PartialAtomValue)value).getSource();
    }

    @Override
    public List<Class<? extends VData>> getProcessedClasses() {
        return Arrays.asList(ExtremumVData.class);
    }
}
