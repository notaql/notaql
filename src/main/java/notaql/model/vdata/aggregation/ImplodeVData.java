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
import notaql.datamodel.NumberValue;
import notaql.datamodel.StringValue;
import notaql.datamodel.Value;
import notaql.model.EvaluationException;
import notaql.model.vdata.ArithmeticVData;
import notaql.model.vdata.VData;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Simply appends atom values to one string seperated with the provided seperator
 */
public class ImplodeVData extends SimpleAggregatingVData<StringValue> {
    private static final long serialVersionUID = -4171643231955821231L;
    private final StringValue seperator;

    public ImplodeVData(VData expression) {
        super(expression);
        this.seperator = new StringValue(",");

    }

    public StringValue getSeperator() {
        return seperator;
    }

    @Override
    public StringValue aggregate(List<Value> values) {
        if (values.stream().filter(v -> !(v instanceof AtomValue<?>)).findAny().isPresent())
            throw new EvaluationException("IMPLODE aggregation function encountered values which are not numbers or strings.");

        final String implosion = values
                .stream()
                .map(v -> ((AtomValue<?>)v).getValue().toString())
                .collect(Collectors.joining(seperator.getValue()));

        return new StringValue(implosion);
    }

    @Override
    public String toString() {
        return "IMPLODE(" + super.toString() + ")";
    }
}
