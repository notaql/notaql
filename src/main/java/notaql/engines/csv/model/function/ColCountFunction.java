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

package notaql.engines.csv.model.function;

import notaql.datamodel.NumberValue;
import notaql.datamodel.ObjectValue;
import notaql.datamodel.fixation.Fixation;
import notaql.engines.Engine;
import notaql.engines.csv.CSVEngine;
import notaql.evaluation.ValueEvaluationResult;
import notaql.model.function.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides the number of columns in a row
 */
public class ColCountFunction implements ComplexFunctionProvider {

    @Override
    public String getName() {
        return "COL_COUNT";
    }

    @Override
    public List<Parameter> getParameters() {
        return new LinkedList<>();
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
        assert fixation.getRootValue() instanceof ObjectValue;
        // get root value
        final ObjectValue value = (ObjectValue) fixation.getRootValue();
        // count its attributes
        return Arrays.asList(new ValueEvaluationResult(new NumberValue(value.size()), fixation));
    }

    @Override
    public boolean canReduce(Arguments args) {
        return false;
    }
}
