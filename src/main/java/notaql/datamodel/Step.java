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

import java.io.Serializable;

/**
* Denotes a step (key) in a complex value (e.g. a attribute name in an object or an index in a list).
*/
public class Step<T> implements Serializable {
    private static final long serialVersionUID = 3390195113308410543L;
    private T step;

    public Step(T step) {
        this.step = step;
    }

    public T getStep() {
        return step;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Step step1 = (Step) o;

        if (!step.equals(step1.step)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return step.hashCode();
    }

    @Override
    public String toString() {
        return step.toString();
    }
}
