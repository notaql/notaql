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

package notaql.model.predicate;

import notaql.datamodel.fixation.Fixation;
import notaql.evaluation.ValueEvaluationResult;
import notaql.model.EvaluationException;

/**
 * Created by thomas on 03.12.14.
 */
public class LogicalOperationPredicate implements Predicate {
    private static final long serialVersionUID = 2522093290767621774L;
    private Predicate left;
    private Predicate right;
    private Operator operator;

    public LogicalOperationPredicate(Predicate left, Predicate right, Operator operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    @Override
    public boolean evaluate(ValueEvaluationResult step, Fixation contextFixation) {
        switch (operator) {
            case AND:
                return left.evaluate(step, contextFixation) && right.evaluate(step, contextFixation);
            case OR:
                return left.evaluate(step, contextFixation) || right.evaluate(step, contextFixation);
            default:
                throw new EvaluationException("Unknown logical operator: " + operator);
        }
    }

    public Predicate getLeft() {
        return left;
    }

    public Predicate getRight() {
        return right;
    }

    public Operator getOperator() {
        return operator;
    }

    @Override
    public String toString() {
        return left.toString() + " " + operator.toString() + " " + right.toString();
    }

    public enum Operator {
        AND, OR
    }
}
