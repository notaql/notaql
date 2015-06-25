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

import notaql.datamodel.Value;
import notaql.datamodel.fixation.Fixation;
import notaql.evaluation.ValueEvaluationResult;
import notaql.evaluation.EvaluatorService;
import notaql.model.EvaluationException;
import notaql.model.path.InputPath;
import notaql.model.vdata.InputVData;
import notaql.model.vdata.VData;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by thomas on 03.12.14.
 */
public class RelationalPredicate implements Predicate {
    private static final long serialVersionUID = 2348526499584146893L;
    private VData left;
    private VData right;
    private Operator operator;

    public RelationalPredicate(VData left, VData right, Operator operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    /**
     * Evaluates the relative predicate by evaluating left and right. If there is a single result, evaluate next
     * level for no group.
     * <p>
     * TODO: some of these assumptions might be too strict
     *
     * @param step
     * @param contextFixation
     * @return
     */
    @Override
    public boolean evaluate(ValueEvaluationResult step, Fixation contextFixation) {
        // TODO: Q&D hack
        // we must look for a inputVData since the path there needs the current step in order to evaluate relative paths
        final List<ValueEvaluationResult> leftResults;
        if (left instanceof InputVData) {
            final InputPath path = ((InputVData) left).getPath();
            if (path.isRelative()) {
                leftResults = path.evaluate(step, contextFixation);
            } else {
                leftResults = path.evaluate(contextFixation);
            }
        } else {
            leftResults = EvaluatorService.getInstance().evaluate(left, contextFixation);
        }

        if (leftResults.size() == 0)
            return false;

        // we must look for a inputVData since the path there needs the current step in order to evaluate relative paths
        final List<ValueEvaluationResult> rightResults;
        if (right instanceof InputVData) {
            final InputPath path = ((InputVData) right).getPath();
            if (path.isRelative()) {
                rightResults = path.evaluate(step, contextFixation);
            } else {
                rightResults = path.evaluate(contextFixation);
            }
        } else {
            rightResults = EvaluatorService.getInstance().evaluate(right, contextFixation);
        }

        if (rightResults.size() == 0)
            return false;


        if (leftResults.size() > 1 && rightResults.size() > 1)
            throw new EvaluationException("Relative predicate evaluation resulted in multiple values being compared. This is not supported (YET?)");

        final List<Value> leftValues;

        if(EvaluatorService.getInstance().canReduce(left)) {
            leftValues = leftResults
                    .stream()
                    .map(l -> EvaluatorService.getInstance()
                                    .reduce(
                                            left,
                                            EvaluatorService.getInstance().createIdentity(left),
                                            l.getValue()
                                    )
                    )
                    .collect(Collectors.toList());
        } else {
            leftValues = leftResults.stream().map(ValueEvaluationResult::getValue).collect(Collectors.toList());
        }

        final List<Value> rightValues;

        if(EvaluatorService.getInstance().canReduce(right)) {
            rightValues = rightResults
                    .stream()
                    .map(r -> EvaluatorService.getInstance()
                                    .reduce(
                                            right,
                                            EvaluatorService.getInstance().createIdentity(right),
                                            r.getValue()
                                    )
                    )
                    .collect(Collectors.toList());
        } else {
            rightValues = rightResults.stream().map(ValueEvaluationResult::getValue).collect(Collectors.toList());
        }

        return compare(leftValues, rightValues);
    }

    private boolean compare(List<Value> leftValues, List<Value> rightValues) {
        // extract the things that shall be compared
        // TODO: This behaviour is an "any" behaviour. in case there are multiple results: if any of them fit it's considered true.
        final boolean leftScalar = leftValues.size() == 1;

        final Value scalar = leftScalar ? leftValues.get(0) : rightValues.get(0);
        final List<Value> values = leftScalar ? rightValues : leftValues;

        boolean comparable = false;
        final List<Comparable<?>> comparableValues = new LinkedList<>();

        if (scalar instanceof Comparable<?>) {
            values
                    .stream()
                    .filter(v -> v instanceof Comparable<?> && v.getClass().equals(scalar.getClass()))
                    .forEach(v -> comparableValues.add((Comparable<?>) v));

            if (comparableValues.size() > 0)
                comparable = true;
        }

        if (!comparable && (operator == Operator.LT || operator == Operator.LTEQ || operator == Operator.GT || operator == Operator.GTEQ)) {
            return false;
        }

        if ((operator == Operator.LT && leftScalar) || (operator == Operator.GT && !leftScalar)) {
            return comparableValues.stream().anyMatch(v -> ((Comparable) scalar).compareTo(v) < 0);
        }
        if ((operator == Operator.LTEQ && leftScalar) || (operator == Operator.GTEQ && !leftScalar)) {
            return comparableValues.stream().anyMatch(v -> ((Comparable) scalar).compareTo(v) <= 0);
        }
        if ((operator == Operator.GT) || (operator == Operator.LT)) {
            return comparableValues.stream().anyMatch(v -> ((Comparable) scalar).compareTo(v) > 0);
        }
        if ((operator == Operator.GTEQ) || (operator == Operator.LTEQ)) {
            return comparableValues.stream().anyMatch(v -> ((Comparable) scalar).compareTo(v) >= 0);
        }
        if (operator == Operator.EQ) {
            return values.stream().anyMatch(scalar::equals);
        }
        if (operator == Operator.NEQ) {
            return values.stream().anyMatch(v -> !scalar.equals(v));
        }

        throw new EvaluationException("Unknown operator: " + operator);
    }

    public VData getLeft() {
        return left;
    }

    public VData getRight() {
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
        LT, LTEQ, GT, GTEQ, EQ, NEQ
    }
}
