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
 * A simple implementation of AtomValue, which simply stores the value it shall represent
 *
 * @author Thomas Lottermann
 */
public abstract class BaseAtomValue<T> implements AtomValue<T> {
    private static final long serialVersionUID = 4683295308425362724L;
    private T value;
    private ComplexValue<?> parent;

    /**
     * Simply construct the value
     *
     * @param value
     */
    public BaseAtomValue(T value) {
        this(value, null);
    }

    /**
     * Construct the value with a given parent
     * @param value
     * @param parent
     */
    public BaseAtomValue(T value, ComplexValue<?> parent) {
        this.value = value;
        this.parent = parent;
    }

    /**
     * The group key consists of the canonical name hashed with the string representation of the value
     * @return
     */
    @Override
    public String groupKey() {
        if(value != null)
            return GroupHelper.digest(getClass().getCanonicalName() + ":" + value.toString());
        return GroupHelper.digest(getClass().getCanonicalName() + ":");
    }

    @Override
    public T getValue() {
        return this.value;
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
    public String toString() {
        return "'" + value.toString() + "'";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseAtomValue that = (BaseAtomValue) o;

        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
