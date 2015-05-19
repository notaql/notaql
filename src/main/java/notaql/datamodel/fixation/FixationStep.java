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

package notaql.datamodel.fixation;

import notaql.datamodel.Step;

import java.io.Serializable;

/**
 * A step in a fixation. These might be bound (e.g. by * or ?()) or not.
 */
public class FixationStep<T> implements Serializable {
    private static final long serialVersionUID = 3417556675393596272L;
    private final Step<T> step;
    private final boolean bound;

    /**
     * @param step
     * @param bound True if this step is bound to a * / @ / ?() step
     */
    public FixationStep(Step<T> step, boolean bound) {
        this.step = step;
        this.bound = bound;
    }

    /**
     * Provides the inner representation of this step (currently String for objects or Integer for Lists)
     * @return
     */
    public Step<T> getStep() {
        return step;
    }

    /**
     * Denotes if this fixation step is bound or not
     * @return
     */
    public boolean isBound() {
        return bound;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FixationStep that = (FixationStep) o;

        return bound == that.bound && step.equals(that.step);

    }

    @Override
    public int hashCode() {
        int result = step.hashCode();
        result = 31 * result + (bound ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return step.toString() + (bound ? "(BOUND)": "");
    }
}
