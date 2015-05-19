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

import java.util.Date;

/**
 * A Date value
 */
public class DateValue extends BaseAtomValue<Date> {
    private static final long serialVersionUID = -6171061585225507638L;

    public DateValue(Date value) {
        super(value);
    }

    public DateValue(Date value, ComplexValue<?> parent) {
        super(value, parent);
    }

    @Override
    public Value deepCopy() {
        return new DateValue(getValue());
    }
}
