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
 * Utility class which provides tools which should help handling values
 *
 * @author Thomas Lottermann
 */
public class ValueUtils {
    /**
     * Parses the input as Integer, Double or String.
     *
     * TODO: support exponentially notated doubles
     *
     * @param input
     * @return
     */
    public static AtomValue parse(String input) {
        if(input.matches("^\\d+$")) {
            return new NumberValue(Integer.parseInt(input));
        } else if(input.matches("^\\d+\\.\\d*$")) {
            return new NumberValue(Double.parseDouble(input));
        } else if(input.toLowerCase().equals("true") || input.toLowerCase().equals("false")) {
            return new BooleanValue(Boolean.parseBoolean(input));
        }

        return new StringValue(input);
    }

    /**
     * Provides the root value of the provided value
     * @param value
     * @return
     */
    public static Value getRoot(Value value) {
        final Value parent = value.getParent();

        if(parent == null)
            return value;

        return getRoot(parent);
    }
}
