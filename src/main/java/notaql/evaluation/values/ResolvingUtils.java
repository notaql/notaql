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
import notaql.datamodel.Value;

/**
 * This utility class should help with resolving tasks
 *
 * @author Thomas Lottermann
 */
public class ResolvingUtils {
    public static boolean isUnresolved(Value value) {
        if(value instanceof UnresolvedValue)
            return true;

        if(!(value instanceof ComplexValue))
            return false;

        final ComplexValue<?> complexValue = (ComplexValue<?>) value;

        return complexValue.toMap().values() // for each nested value
                .stream()
                .map(ResolvingUtils::isUnresolved) // recurse
                .filter(b -> b)
                .findAny()
                .isPresent(); // and look if any value is true
    }

}
