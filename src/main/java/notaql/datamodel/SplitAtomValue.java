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

/**
 * The purpose of this class is to make splitting possible while still saving the information
 * about the original state.
 * e.g. this is used for the .split() function
 */
public class SplitAtomValue<T> extends ListValue implements SplitValue<Integer>, AtomValue<T> {
    private static final long serialVersionUID = -1477988083251948200L;
    private AtomValue<T> value;

    public SplitAtomValue(AtomValue<T> value, List<Value> splits) {
        super(value.getParent());
        this.value = value;
        this.addAll(splits);
    }

    @Override
    public AtomValue<T> getBaseValue() {
        return this.value;
    }

    @Override
    public T getValue() {
        return value.getValue();
    }

    @Override
    public SplitAtomValue<T> deepCopy() {
        final Value valCopy = value.deepCopy();
        assert valCopy instanceof AtomValue<?>;
        return new SplitAtomValue<>((AtomValue<T>) valCopy, super.deepCopy());
    }

    @Override
    public String toString() {
        return value.toString() + " -> " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SplitAtomValue that = (SplitAtomValue) o;

        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
