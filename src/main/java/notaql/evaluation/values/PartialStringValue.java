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

package notaql.evaluation.values;

import notaql.datamodel.ComplexValue;
import notaql.datamodel.StringValue;
import notaql.datamodel.Value;
import notaql.datamodel.fixation.Fixation;

/**
 * Partial string.
 */
public class PartialStringValue extends StringValue implements UnresolvedValue {

    private static final long serialVersionUID = -5877686268349131499L;

    public PartialStringValue(String value) {
        super(value);
    }

    public PartialStringValue(String value, ComplexValue<?> parent) {
        super(value, parent);
    }

    @Override
    public Value deepCopy() {
        return new PartialStringValue(getValue());
    }
}
