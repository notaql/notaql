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

package notaql.model.vdata.aggregation;

import notaql.datamodel.AtomValue;
import notaql.datamodel.NullValue;
import notaql.datamodel.Value;
import notaql.model.EvaluationException;
import notaql.model.vdata.VData;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Thomas Lottermann
 */
public class ExtremumVData extends SimpleAggregatingVData<AtomValue> {
    private static final long serialVersionUID = 8874321123164971717L;
    private final Function function;

    public ExtremumVData(VData expression, Function function) {
        super(expression);
        this.function = function;
    }

    public Function getFunction() {
        return function;
    }

    @SuppressWarnings("unchecked")
    @Override
    public AtomValue aggregate(List<Value> values) {
        if (values
                .stream()
                .filter(v -> !(v instanceof AtomValue) || !(((AtomValue) v).getValue() instanceof Comparable))
                .findAny()
                .isPresent()
                )
            throw new EvaluationException(function.name() + " aggregation function encountered values which are not comparable.");

        final Stream<AtomValue> atomValueStream = values.stream().map(v -> (AtomValue) v);

        final Optional<AtomValue> extremum;
        switch (function) {
            case MIN:
                extremum = atomValueStream.min(new AtomValueComparator());
                break;
            case MAX:
                extremum = atomValueStream.max(new AtomValueComparator());
                break;
            default:
                extremum = null;
                assert false: "Unknown extremum operator: " + function.toString();
        }


        if (extremum.isPresent())
            return extremum.get();

        return new NullValue();
    }

    @Override
    public String toString() {
        return function.toString() + "(" + super.toString() + ")";
    }

    public enum Function {
        MIN, MAX
    }

    public static class AtomValueComparator implements Comparator<AtomValue> {
        @Override
        public int compare(AtomValue o1, AtomValue o2) {
            assert o1.getValue() instanceof Comparable && o2.getValue() instanceof Comparable;

            final Comparable o1Value = (Comparable) o1.getValue();
            final Comparable o2Value = (Comparable) o2.getValue();

            try {
                return o1Value.compareTo(o2Value);
            } catch (ClassCastException e) {
                throw new EvaluationException("The values inside a group are not comparable to each " +
                        "other");
            }

        }
    }
}
