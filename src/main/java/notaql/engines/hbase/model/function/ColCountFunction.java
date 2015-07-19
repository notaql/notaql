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

package notaql.engines.hbase.model.function;

import notaql.datamodel.*;
import notaql.datamodel.fixation.Fixation;
import notaql.engines.Engine;
import notaql.engines.csv.CSVEngine;
import notaql.evaluation.EvaluatorService;
import notaql.evaluation.ValueEvaluationResult;
import notaql.evaluation.values.ResolvingUtils;
import notaql.model.EvaluationException;
import notaql.model.function.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Provides the number of columns in a row - or for a certain column family in a row
 */
public class ColCountFunction implements ComplexFunctionProvider {

    @Override
    public String getName() {
        return "COL_COUNT";
    }

    @Override
    public List<Parameter> getParameters() {
        return Arrays.asList(new Parameter("family", new NullValue()))
    }

    @Override
    public boolean isApplicable(Engine inEngine, Engine outEngine) {
        return inEngine instanceof CSVEngine;
    }

    @Override
    public FunctionEvaluator getEvaluator() {
        return new ColCountEvaluator();
    }

    @Override
    public FunctionReducer getReducer() {
        return null;
    }
}

class ColCountEvaluator implements FunctionEvaluator {
    private static final long serialVersionUID = -5037602804069935528L;

    @Override
    public List<ValueEvaluationResult> evaluate(Arguments args, Fixation fixation) {
        final ValueEvaluationResult familyResult;

        // get family if provided
        if(getFromConstantKWArgs() != null) {
            final List<ValueEvaluationResult> eval = EvaluatorService.getInstance().evaluate(colFamilyVData, fixation);
            if(eval.size() != 1)
                throw new EvaluationException("The column family that was provided to COL_COUNT was not distinct");
            familyResult = eval.get(0);
            if(ResolvingUtils.isUnresolved(familyResult.getValue()))
                throw new EvaluationException("Aggregation functions are not allowed in COL_COUNT");
            if(!(familyResult.getValue() instanceof StringValue))
                throw new EvaluationException("COL_COUNT() expects a string value as a family argument, got: " + familyResult.getValue().toString());
        } else {
            familyResult = null;
        }

        assert fixation.getRootValue() instanceof ObjectValue;

        final ObjectValue value = (ObjectValue) fixation.getRootValue();

        // if no family is provided: count all columns
        if(familyResult == null) {
            final Optional<Integer> reduce = value.toMap().entrySet()
                    .stream()
                    .filter(e -> e.getValue() instanceof ObjectValue)
                    .map(e -> ((ObjectValue) e.getValue()).size())
                    .reduce((a, b) -> a + b);
            assert reduce.isPresent();
            return Arrays.asList(new ValueEvaluationResult(new NumberValue(reduce.get()), fixation));
        }

        // if a family is provided count only the columns in there
        final StringValue familyResultValue = (StringValue) familyResult.getValue();

        final Value family = value.get(new Step<>(familyResultValue.getValue()));

        assert family instanceof ObjectValue;

        return Arrays.asList(new ValueEvaluationResult(new NumberValue(((ObjectValue)family).size()), familyResult.getFixation()));
    }

    @Override
    public boolean canReduce(Arguments args) {
        return false;
    }
}
