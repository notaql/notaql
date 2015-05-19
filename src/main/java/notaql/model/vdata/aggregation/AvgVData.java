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

import notaql.datamodel.NumberValue;
import notaql.datamodel.Value;
import notaql.model.EvaluationException;
import notaql.model.vdata.ArithmeticVData;
import notaql.model.vdata.VData;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Thomas Lottermann
 */
public class AvgVData extends SimpleAggregatingVData<NumberValue> {
    private static final long serialVersionUID = 8822802215804033740L;

    public AvgVData(VData expression) {
        super(expression);
    }

    @Override
    public NumberValue aggregate(List<Value> values) {
        if (values.stream().filter(v -> !(v instanceof NumberValue)).findAny().isPresent())
            throw new EvaluationException("AVG aggregation function encountered values which are not numbers.");

        final Optional<NumberValue> sum = values
                .stream()
                .map(v -> (NumberValue) v)
                .reduce((a, b) -> ArithmeticVData.calculate(ArithmeticVData.Operation.ADD, a, b));

        if (sum.isPresent())
            return ArithmeticVData.calculate(
                    ArithmeticVData.Operation.DIVIDE,
                    sum.get(),
                    new NumberValue(values.size())
            );

        return new NumberValue(0);
    }

    @Override
    public String toString() {
        return "AVG(" + super.toString() + ")";
    }
}
