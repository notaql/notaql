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

import notaql.datamodel.ComplexValue;
import notaql.datamodel.SplitValue;
import notaql.datamodel.Step;
import notaql.datamodel.Value;
import notaql.datamodel.fixation.Fixation;
import notaql.model.EvaluationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This gets created if parts of a arithmetic expression could not be evaluated.
 */
public class PartialArithmeticValue implements ComplexValue<PartialArithmeticValue.Position>, UnresolvedValue {
    private static final long serialVersionUID = 1650670767581594848L;
    private Value left;
    private Value right;
    private ComplexValue parent;

    public PartialArithmeticValue(Value left, Value right) {
        this(left, right, null);
    }

    public PartialArithmeticValue(Value left, Value right, ComplexValue parent) {
        this.left = left;
        this.right = right;
        this.parent = parent;
    }

    @Override
    public Value get(Step<Position> key) {
        if(key.getStep() == Position.LEFT)
            return left;
        if(key.getStep() == Position.RIGHT)
            return right;

        return null;
    }

    @Override
    public Map<Step<Position>, Value> toMap() {
        final HashMap<Step<Position>, Value> result = new HashMap<>();
        result.put(new Step<>(Position.LEFT), left);
        result.put(new Step<>(Position.RIGHT), right);
        return result;
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public SplitValue<Integer> split(Step<Position> key, List<Value> splits) {
        throw new EvaluationException("An arithmetic value should never be split.");
    }

    @Override
    public Step<Position> getStep(Value child) {
        if(left == child)
            return new Step<>(Position.LEFT);
        return new Step<>(Position.RIGHT);
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
        return new PartialArithmeticValue(left.deepCopy(), right.deepCopy());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PartialArithmeticValue that = (PartialArithmeticValue) o;

        if (left != null ? !left.equals(that.left) : that.left != null) return false;
        if (right != null ? !right.equals(that.right) : that.right != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = left != null ? left.hashCode() : 0;
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }

    @Override
    public String groupKey() {
        return GroupHelper.digest(getClass().getCanonicalName() + ":" + left.groupKey() + ":" + right.groupKey());
    }

    public enum Position {
        LEFT, RIGHT
    }
}
