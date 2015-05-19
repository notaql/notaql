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

package notaql.engines.mongodb.evaluation;

import notaql.datamodel.*;
import notaql.datamodel.fixation.Fixation;
import notaql.engines.mongodb.model.vdata.ListCountFunctionVData;
import notaql.evaluation.ValueEvaluationResult;
import notaql.evaluation.Evaluator;
import notaql.evaluation.EvaluatorService;
import notaql.model.EvaluationException;
import notaql.model.vdata.VData;
import notaql.evaluation.values.ResolvingUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by thomas on 19.02.15.
 */
public class ListCountFunctionVDataEvaluator implements Evaluator {
    private static final long serialVersionUID = -4567264007127006124L;

    @Override
    public List<ValueEvaluationResult> evaluate(VData vData, Fixation fixation) {
        assert vData instanceof ListCountFunctionVData;

        final ListCountFunctionVData countFunctionVData = (ListCountFunctionVData) vData;

        final VData listVData = countFunctionVData.getListVData();

        final ValueEvaluationResult listResult;

        final List<ValueEvaluationResult> eval = EvaluatorService.getInstance().evaluate(listVData, fixation);
        if(eval.size() != 1)
            throw new EvaluationException("The list that was provided to LIST_COUNT was not distinct");
        listResult = eval.get(0);
        if(ResolvingUtils.isUnresolved(listResult.getValue()))
            throw new EvaluationException("Aggregation functions are not allowed in LIST_COUNT");
        if(!(listResult.getValue() instanceof ComplexValue<?>))
            throw new EvaluationException("LIST_COUNT() expects a complex value as argument, got: " + listResult.getValue().toString());

        return Arrays.asList(
                new ValueEvaluationResult(
                        new NumberValue(
                                ((ComplexValue<?>)listResult.getValue()).size()),
                        listResult.getFixation()
                )
        );
    }

    @Override
    public boolean canReduce(VData vData) {
        return false;
    }

    @Override
    public List<Class<? extends VData>> getProcessedClasses() {
        return Arrays.asList(ListCountFunctionVData.class);
    }
}
