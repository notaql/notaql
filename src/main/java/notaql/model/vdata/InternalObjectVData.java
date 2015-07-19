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

import notaql.model.function.Argument;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Internal OBJECT() function allowing nicer construction. This is just used for internal purposes.
 */
public class InternalObjectVData implements VData {
    private static final long serialVersionUID = -2544526318060714727L;
    private final List<Argument> specifications;

    public InternalObjectVData() {
        specifications = new LinkedList<>();
    }

    public InternalObjectVData(List<Argument> specifications) {
        this.specifications = specifications;
    }

    public InternalObjectVData(Argument... specifications) {
        this(Arrays.asList(specifications));
    }

    public List<Argument> getSpecifications() {
        return specifications;
    }

    @Override
    public String toString() {
        return "INTERNAL_OBJECT(" + specifications + ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InternalObjectVData that = (InternalObjectVData) o;

        if (specifications != null ? !specifications.equals(that.specifications) : that.specifications != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return specifications != null ? specifications.hashCode() : 0;
    }
}
