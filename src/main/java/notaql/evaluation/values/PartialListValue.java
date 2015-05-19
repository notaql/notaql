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
import notaql.datamodel.ListValue;
import notaql.datamodel.fixation.Fixation;

/**
 * Partial list.
 */
public class PartialListValue extends ListValue implements UnresolvedValue {

    private static final long serialVersionUID = -8397563209693445769L;

    public PartialListValue() {
    }

    public PartialListValue(ComplexValue<?> parent) {
        super(parent);
    }

    @Override
    public ListValue deepCopy() {
        final PartialListValue values = new PartialListValue();
        values.addAll(super.deepCopy());
        return values;
    }
}
