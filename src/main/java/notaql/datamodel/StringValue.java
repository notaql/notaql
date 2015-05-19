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
 * A String value
 * @author Thomas Lottermann
 */
public class StringValue extends BaseAtomValue<String> implements Comparable<StringValue> {
    private static final long serialVersionUID = -7819084014306325806L;

    public StringValue(String value) {
        super(value);
    }
    public StringValue(String value, ComplexValue<?> parent) {
        super(value, parent);
    }

    @Override
    public int compareTo(StringValue o) {
        return getValue().compareTo(o.getValue());
    }

    @Override
    public Value deepCopy() {
        return new StringValue(getValue());
    }
}
