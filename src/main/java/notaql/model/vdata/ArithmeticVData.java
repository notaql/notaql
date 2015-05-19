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

import notaql.datamodel.NumberValue;
import notaql.datamodel.Value;
import notaql.model.EvaluationException;

/**
 * Created by thomas on 18.11.14.
 */
public class ArithmeticVData implements VData {
    private static final long serialVersionUID = -3414331565043198232L;
    private final VData left;
    private final VData right;
    private final Operation operator;

    public ArithmeticVData(VData left, VData right, Operation operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    public VData getLeft() {
        return left;
    }

    public VData getRight() {
        return right;
    }

    public Operation getOperator() {
        return operator;
    }

    public NumberValue calculate(Value leftValue, Value rightValue) {
        if(!(leftValue instanceof NumberValue && rightValue instanceof NumberValue ))
            throw new EvaluationException("Added values must evaluate to a NumberValue. This was not the case.");

        return calculate(operator, (NumberValue)leftValue, (NumberValue)rightValue);
    }

    public static NumberValue calculate(Operation operator, NumberValue leftValue, NumberValue rightValue) {
        final double leftDouble = leftValue.getValue().doubleValue();
        final double rightDouble = rightValue.getValue().doubleValue();

        final double sum;
        switch (operator) {
            case ADD:
                sum = leftDouble + rightDouble;
                break;
            case SUBTRACT:
                sum = leftDouble - rightDouble;
                break;
            case MULTIPLY:
                sum = leftDouble * rightDouble;
                break;
            case DIVIDE:
                sum = leftDouble / rightDouble;
                break;
            default:
                throw new EvaluationException("Unknown operator: " + operator);
        }

        return new NumberValue(sum);
    }

    @Override
    public String toString() {
        return left.toString() + " " + operator.toString() + " " + right.toString();
    }

    public enum Operation {
        ADD, SUBTRACT, MULTIPLY, DIVIDE
    }
}
