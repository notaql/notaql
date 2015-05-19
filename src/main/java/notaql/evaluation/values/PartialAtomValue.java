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

import notaql.datamodel.AtomValue;
import notaql.datamodel.ComplexValue;
import notaql.datamodel.Value;
import notaql.datamodel.fixation.Fixation;

/**
 * Just like any AtomValue but marked as unresolved. Currently used for extrema.
 */
public class PartialAtomValue<T> implements AtomValue<T>, UnresolvedValue {
    private static final long serialVersionUID = -5953631532632217857L;
    private final AtomValue<T> value;
    private ComplexValue<?> parent;

    public PartialAtomValue(AtomValue<T> value) {
        this.value = value;
    }

    public PartialAtomValue(AtomValue<T> value, ComplexValue<?> parent) {
        this.value = value;
        this.parent = parent;
    }

    public AtomValue<T> getSource() {
        return value;
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
        return new PartialAtomValue<>(value);
    }

    @Override
    public T getValue() {
        return value.getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PartialAtomValue that = (PartialAtomValue) o;

        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public String groupKey() {
        return GroupHelper.digest(value.getClass().getCanonicalName() + ":" + value.toString());
    }
}
