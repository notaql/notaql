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

package notaql.model.vdata;

import notaql.datamodel.AtomValue;

/**
 * Represents the basic constant.
 *
 * @author Thomas Lottermann
 */
public class AtomVData implements VData {
    private static final long serialVersionUID = -488602815781893806L;
    private final AtomValue<?> value;

    public AtomVData(AtomValue<?> value) {
        this.value = value;
    }

    public AtomValue<?> getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AtomVData atomVData = (AtomVData) o;

        if (value != null ? !value.equals(atomVData.value) : atomVData.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
