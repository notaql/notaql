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

import notaql.datamodel.Value;
import notaql.datamodel.fixation.Fixation;
import notaql.evaluation.values.PartialObjectValue;
import notaql.model.function.Arguments;
import notaql.model.vdata.InternalObjectVData;
import notaql.model.vdata.VData;
import notaql.model.vdata.aggregation.AggregatingInternalObjectVData;

import java.util.Arrays;
import java.util.List;

/**
 * Simply wraps around the function evaluator for the OBJECT function
 */
public class InternalObjectVDataEvaluator implements Evaluator, Reducer {
    private static final long serialVersionUID = -5276223339595319073L;
    private final ObjectFunctionEvaluator functionEvaluator;

    public InternalObjectVDataEvaluator() {
        this.functionEvaluator = new ObjectFunctionEvaluator();
    }

    @Override
    public List<ValueEvaluationResult> evaluate(VData vData, Fixation fixation) {
        assert vData instanceof InternalObjectVData;
        final InternalObjectVData objectVData = (InternalObjectVData) vData;

        final List<ValueEvaluationResult> results = functionEvaluator.evaluate(
                new Arguments(objectVData.getSpecifications()),
                fixation
        );

        return results;
    }

    @Override
    public boolean canReduce(VData vData) {
        return true;
    }

    @Override
    public PartialObjectValue reduce(VData vData, Value v1, Value v2) {
        assert vData instanceof InternalObjectVData;
        final InternalObjectVData objectVData = (InternalObjectVData) vData;

        final PartialObjectValue reduced = functionEvaluator.reduce(
                new Arguments(objectVData.getSpecifications()),
                v1,
                v2
        );

        return reduced;
    }

    @Override
    public Value createIdentity(VData vData) {
        assert vData instanceof InternalObjectVData;
        final InternalObjectVData objectVData = (InternalObjectVData) vData;

        return functionEvaluator.createIdentity(new Arguments(objectVData.getSpecifications()));
    }

    @Override
    public Value finalize(VData vData, Value value) {
        assert vData instanceof InternalObjectVData;
        final InternalObjectVData objectVData = (InternalObjectVData) vData;

        return functionEvaluator.finalize(new Arguments(objectVData.getSpecifications()), value);
    }

    @Override
    public List<Class<? extends VData>> getProcessedClasses() {
        return Arrays.asList(InternalObjectVData.class, AggregatingInternalObjectVData.class);
    }
}
