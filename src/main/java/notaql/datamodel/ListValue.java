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

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This represents a simple BSON-like Array - let's just call it a List
 *
 * @author Thomas Lottermann
 */
public class ListValue implements ComplexValue<Integer>, List<Value> {
    private static final long serialVersionUID = -1249825633460312233L;
    private ComplexValue<?> parent;
    private List<Value> list = new ArrayList<>();

    public ListValue() {
        this(null);
    }

    public ListValue(ComplexValue<?> parent) {
        this.parent = parent;
    }

    @Override
    public Value get(Step<Integer> key) {
        return this.get(key.getStep().intValue());
    }

    @Override
    public Map<Step<Integer>, Value> toMap() {
        final LinkedHashMap<Step<Integer>, Value> result = new LinkedHashMap<>();

        for (int i = 0; i < list.size(); i++) {
            result.put(new Step<Integer>(i), list.get(i));
        }

        return result;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public SplitValue<Integer> split(Step<Integer> key, List<Value> splits) {
        final Value value = list.get(key.getStep());
        if(!(value instanceof AtomValue))
            throw new IllegalArgumentException("The selected value is no atom.");
        final SplitValue<Integer> splitted = new SplitAtomValue<>((AtomValue<?>)value, splits);
        set(key.getStep(), splitted);

        return splitted;
    }

    @Override
    public Step<Integer> getStep(Value child) {
        for (int i = 0; i < list.size(); i++) {
            if(list.get(i) == child)
                return new Step<Integer>(i);
        }
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
    public ListValue deepCopy() {
        final ListValue copy = new ListValue();
        this.stream().forEach(e -> copy.add(e.deepCopy()));
        return copy;
    }

    /**
     * Adds the value to the list and sets the parent as this
     * @param value
     * @return
     */
    @Override
    public boolean add(Value value) {
        value.setParent(this);
        return list.add(value);
    }

    /**
     * Adds the value to the list and sets the parent as this
     * @param index
     * @param element
     */
    @Override
    public void add(int index, Value element) {
        element.setParent(this);
        list.add(index, element);
    }

    /**
     * Adds the values to the list and sets the parent as this
     * @param c
     * @return
     */
    @Override
    public boolean addAll(Collection<? extends Value> c) {
        c.stream().forEach(v -> v.setParent(this));
        return list.addAll(c);
    }

    /**
     * Adds the values to the list and sets the parent as this
     * @param c
     * @return
     */
    @Override
    public boolean addAll(int index, Collection<? extends Value> c) {
        c.stream().forEach(v -> v.setParent(this));
        return list.addAll(index, c);
    }

    /**
     * Adds the value to the list and sets the parent as this
     * @param index
     * @param element
     * @return
     */
    @Override
    public Value set(int index, Value element) {
        element.setParent(this);
        return list.set(index, element);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ListValue listValue = (ListValue) o;

        if (list != null ? !list.equals(listValue.list) : listValue.list != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return list != null ? list.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();

        builder.append("[\n");
        builder.append(
                list.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(",\n"))
        );
        builder.append("\n]");

        return builder.toString();
    }

    /*
    =====================================================================
    The following lines simply delegate everything to the underlying list
    =====================================================================
     */

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public Iterator<Value> iterator() {
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    @Override
    public void replaceAll(UnaryOperator<Value> operator) {
        list.replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super Value> c) {
        list.sort(c);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public Value get(int index) {
        return list.get(index);
    }

    @Override
    public Value remove(int index) {
        return list.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<Value> listIterator() {
        return list.listIterator();
    }

    @Override
    public ListIterator<Value> listIterator(int index) {
        return list.listIterator(index);
    }

    @Override
    public List<Value> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    @Override
    public Spliterator<Value> spliterator() {
        return list.spliterator();
    }

    @Override
    public boolean removeIf(Predicate<? super Value> filter) {
        return list.removeIf(filter);
    }

    @Override
    public Stream<Value> stream() {
        return list.stream();
    }

    @Override
    public Stream<Value> parallelStream() {
        return list.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super Value> action) {
        list.forEach(action);
    }

    @Override
    public String groupKey() {
        final Optional<String> s = list.stream().map(Groupable::groupKey).reduce((a, b) -> GroupHelper.digest(a + b));
        if(!s.isPresent())
            return GroupHelper.digest(getClass().getCanonicalName() + ":" + "0");
        return GroupHelper.digest(getClass().getCanonicalName() + ":" + s.get());
    }
}
