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

package notaql.datamodel;

/**
 * A number value. The number below might be of any type which falls under Number
 * @author Thomas Lottermann
 */
public class NumberValue extends BaseAtomValue<Number> implements Comparable<NumberValue> {
    private static final long serialVersionUID = -3499680203989857055L;

    public NumberValue(Number value) {
        super(value);
    }
    public NumberValue(Number value, ComplexValue<?> parent) {
        super(value, parent);
    }

    @Override
    public int compareTo(NumberValue o) {
        return new Double(getValue().doubleValue()).compareTo(o.getValue().doubleValue());
    }

    @Override
    public Value deepCopy() {
        return new NumberValue(getValue());
    }
}
