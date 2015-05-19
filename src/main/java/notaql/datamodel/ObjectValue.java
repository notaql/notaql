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

import notaql.evaluation.values.UnresolvedValue;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This simply represents a BSON-like object
 *
 * @author Thomas Lottermann
 */
public class ObjectValue implements ComplexValue<String> {
    private static final long serialVersionUID = 7479453524811023147L;
    private ComplexValue<?> parent;
    private LinkedHashMap<Step<String>, Value> map = new LinkedHashMap<>();

    public ObjectValue() {
        this(null);
    }

    public ObjectValue(ComplexValue<?> parent) {
        this.parent = parent;
    }

    /**
     * Adds the value to the object and sets the parent as this
     *
     * @param key
     * @param value
     * @return
     */
    public Value put(Step<String> key, Value value) {
        value.setParent(this);
        return map.put(key, value);
    }

    /**
     * Adds the values in the complex value (e.g. ObjectValue) to the object and sets the parent as this
     *
     * @param v
     * @return
     */
    public void putAll(ComplexValue<String> v) {
        final Map<Step<String>, Value> m = v.toMap();
        m.values().stream().forEach(a -> a.setParent(this));
        map.putAll(m);
    }

    /**
     * Adds the values in the map to the object and sets the parent as this
     *
     * @param m
     * @return
     */
    public void putAll(Map<Step<String>, Value> m) {
        m.values().stream().forEach(v -> v.setParent(this));
        map.putAll(m);
    }

    /**
     * Deletes a value from the object
     *
     * Note: this leaves references to this object untouched
     *
     * @param key
     * @return
     */
    public Value removeKey(Step<String> key) {
        return map.remove(key);
    }

    /**
     * Denotes if this object contains the provided key
     * @param key
     * @return
     */
    public boolean containsKey(Step<String> key) {
        return map.containsKey(key);
    }

    /**
     * Provides all keys
     * @return
     */
    public Set<Step<String>> keySet() {
        return map.keySet();
    }

    @Override
    public Value get(Step<String> key) {
        return map.get(key);
    }

    @Override
    public Map<Step<String>, Value> toMap() {
        return map;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public SplitValue<Integer> split(Step<String> key, List<Value> splits) {
        final Value value = map.get(key);
        if(!(value instanceof AtomValue))
            throw new IllegalArgumentException("The selected value is no atom.");
        final SplitValue<Integer> splitted = new SplitAtomValue<>((AtomValue<?>)value, splits);
        put(key, splitted);

        return splitted;
    }

    @Override
    public Step<String> getStep(Value child) {
        final Optional<Map.Entry<Step<String>, Value>> entry = map.entrySet().stream().filter(e -> e.getValue() == child).findAny();
        if(entry.isPresent())
            return entry.get().getKey();
        return null;
    }

    @Override
    public ComplexValue<?> getParent() {
        return parent;
    }

    @Override
    public void setParent(ComplexValue<?> parent) {
        this.parent = parent;
    }

    @Override
    public boolean isRoot() {
        return parent == null;
    }

    @Override
    public Value deepCopy() {
        final ObjectValue copy = new ObjectValue();
        this.toMap().entrySet().stream().forEach(e -> copy.put(e.getKey(), e.getValue().deepCopy()));
        return copy;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();

        builder.append("{\n");
        builder.append(
                map.entrySet().stream()
                        .map(e -> e.getKey() + ": " + (e.getValue() != null ? e.getValue() : "NULL"))
                        .collect(Collectors.joining(",\n"))
        );
        builder.append("\n}");

        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObjectValue that = (ObjectValue) o;

        if (!reduceMap(map).equals(reduceMap(that.map))) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return map != null ? reduceMap(map).hashCode() : 0;
    }

    private Map<Step<String>, Value> reduceMap(Map<Step<String>, Value> map) {
        return map.entrySet().stream()
                .filter(e -> !(e.getValue() instanceof UnresolvedValue))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Since the entry hashes are ordered it should ignore order
     * @return
     */
    @Override
    public String groupKey() {
        final Map<Step<String>, Value> reduceMap = reduceMap(map);

        final String mapHash = reduceMap.entrySet().stream()
                .map(e -> GroupHelper.digest(e.getKey().getStep() + e.getValue().groupKey()))
                .sorted()
                .reduce("", (a, b) -> GroupHelper.digest(a + b));

        return GroupHelper.digest(getClass().getCanonicalName() + ":" + mapHash);
    }
}
