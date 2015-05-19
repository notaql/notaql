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

import java.util.List;
import java.util.Map;

/**
 * This provides the interface for complex values (e.g. ListValue or ObjectValue).
 *
 * Complex values are key-value based structures.
 *
 * @author Thomas Lottermann
 */
public interface ComplexValue<T> extends Value {
    /**
     * Provides the value for the given key
     *
     * @param key
     * @return
     */
    public Value get(Step<T> key);

    /**
     * Converts this value to a map
     *
     * @return
     */
    public Map<Step<T>, Value> toMap();

    /**
     * Indicates how many key-value pairs are in this complex object
     *
     * @return
     */
    public int size();

    /**
     * Splits the value of the given key by creating a SplitValue from it and the provided split list and
     * replacing the current value with this representation.
     *
     * TODO: generify?
     *
     * @param key
     * @param splits
     * @return
     */
    public SplitValue<Integer> split(Step<T> key, List<Value> splits);

    /**
     * Provides the key under which the provided child is stored
     *
     * @param child
     * @return
     */
    public Step<T> getStep(Value child);

}
