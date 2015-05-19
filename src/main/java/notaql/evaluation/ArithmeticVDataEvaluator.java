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

package notaql.evaluation;

import notaql.datamodel.NullValue;
import notaql.datamodel.NumberValue;
import notaql.datamodel.Step;
import notaql.datamodel.Value;
import notaql.datamodel.fixation.Fixation;
import notaql.evaluation.values.PartialArithmeticValue;
import notaql.evaluation.values.PartialNullValue;
import notaql.model.EvaluationException;
import notaql.model.vdata.ArithmeticVData;
import notaql.model.vdata.VData;
import notaql.evaluation.values.ResolvingUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Evaluates arithmetic expressions.
 */
public class ArithmeticVDataEvaluator implements Evaluator, Reducer {

    private static final long serialVersionUID = 7619497803530166986L;

    /**
     * In this execution the left and right value are evaluated for the current evaluation level.
     * In case the evaluation can be done completely (we retrieve fully resolved values), we can evaluate the operation.
     * In case some values are still unresolved: we need to return a temporary ArithmeticValue which can be evaluated
     * in the next step.
     *
     * @param vData
     * @param fixation
     * @return A list of evaluation results being either a PartialArithmeticValue or a completely resolved NumberValue
     */
    @Override
    public List<ValueEvaluationResult> evaluate(VData vData, Fixation fixation) {
        assert vData instanceof ArithmeticVData;
        final ArithmeticVData arithmeticVData = (ArithmeticVData) vData;

        final List<ValueEvaluationResult> result = new LinkedList<>();

        final List<ValueEvaluationResult> leftEval = EvaluatorService.getInstance().evaluate(arithmeticVData.getLeft(), fixation);

        for (ValueEvaluationResult leftResult : leftEval) {
            final List<ValueEvaluationResult> rightEval = EvaluatorService.getInstance().evaluate(arithmeticVData.getRight(), fixation);

            final Value leftValue = leftResult.getValue();

            boolean leftUnresolved = ResolvingUtils.isUnresolved(leftValue);

            for (ValueEvaluationResult rightResult : rightEval) {
                final Value rightValue = rightResult.getValue();

                if(leftUnresolved || ResolvingUtils.isUnresolved(rightValue)) {
                    result.add(
                            new ValueEvaluationResult(
                                    new PartialArithmeticValue(leftValue, rightValue)
                                    , rightResult.getFixation()
                            )
                    );
                    continue;
                }

                final NumberValue calculation = arithmeticVData.calculate(leftValue, rightValue);

                result.add(new ValueEvaluationResult(calculation, rightResult.getFixation()));
            }
        }

        return result;
    }

    @Override
    public boolean canReduce(VData vData) {
        return true;
    }

    @Override
    public List<Class<? extends VData>> getProcessedClasses() {
        return Arrays.asList(ArithmeticVData.class);
    }

    @Override
    public Value reduce(VData vData, Value v1, Value v2) {
        assert vData instanceof ArithmeticVData;
        final ArithmeticVData arithmeticVData = (ArithmeticVData) vData;

        if(v1 instanceof NullValue && v2 instanceof NullValue)
            return createIdentity(vData);

        if(v1 instanceof NullValue)
            return v2;

        if(v2 instanceof NullValue)
            return v1;

        if(v1 instanceof NumberValue)
            return v1;

        if(v2 instanceof NumberValue)
            return v2;

        if(!(v1 instanceof PartialArithmeticValue && v2 instanceof PartialArithmeticValue))
            throw new EvaluationException("Reduce on arithmetic VData, but no ArithmeticValue given.");

        final PartialArithmeticValue pv1 = (PartialArithmeticValue) v1;
        final PartialArithmeticValue pv2 = (PartialArithmeticValue) v2;

        Value pv1l = pv1.get(new Step<>(PartialArithmeticValue.Position.LEFT));
        Value pv1r = pv1.get(new Step<>(PartialArithmeticValue.Position.RIGHT));
        Value pv2l = pv2.get(new Step<>(PartialArithmeticValue.Position.LEFT));
        Value pv2r = pv2.get(new Step<>(PartialArithmeticValue.Position.RIGHT));

        if(ResolvingUtils.isUnresolved(pv1l)) {
            assert ResolvingUtils.isUnresolved(pv2l);
            assert !ResolvingUtils.isUnresolved(pv1r);
            assert !ResolvingUtils.isUnresolved(pv2r);
            assert pv1r.equals(pv2r);

            return new PartialArithmeticValue(EvaluatorService.getInstance().reduce(arithmeticVData.getLeft(), pv1l, pv2l), pv1r);
        }

        assert ResolvingUtils.isUnresolved(pv1r);
        assert ResolvingUtils.isUnresolved(pv2r);
        assert !ResolvingUtils.isUnresolved(pv2l);
        assert pv1l.equals(pv2l);

        return new PartialArithmeticValue(pv1l, EvaluatorService.getInstance().reduce(arithmeticVData.getRight(), pv1r, pv2r));
    }

    @Override
    public Value createIdentity(VData vData) {
        return new PartialNullValue();
    }

    @Override
    public Value finalize(VData vData, Value value) {
        assert vData instanceof ArithmeticVData;
        final ArithmeticVData arithmeticVData = (ArithmeticVData) vData;

        if(value instanceof NumberValue)
            return value;

        assert value instanceof PartialArithmeticValue;

        final PartialArithmeticValue pv = (PartialArithmeticValue) value;

        return arithmeticVData.calculate(
                pv.get(new Step<>(PartialArithmeticValue.Position.LEFT)),
                pv.get(new Step<>(PartialArithmeticValue.Position.RIGHT))
        );
    }
}
