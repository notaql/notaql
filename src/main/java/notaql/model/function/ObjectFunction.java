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

package notaql.model.function;

import notaql.engines.Engine;
import notaql.evaluation.ObjectFunctionEvaluator;

import java.util.Arrays;
import java.util.List;

/**
 * Created by thomas on 7/18/15.
 */
public class ObjectFunction implements FunctionProvider {
    private static final long serialVersionUID = -7625988146800633603L;

    @Override
    public String getName() {
        return "OBJECT";
    }

    @Override
    public List<Parameter> getParameters() {
        return Arrays.asList(new Parameter("fields", Parameter.ArgumentType.KEYWORD_ARG));
    }

    @Override
    public boolean isApplicable(Engine inEngine, Engine outEngine) {
        return true;
    }

    @Override
    public FunctionEvaluator getEvaluator() {
        return new ObjectFunctionEvaluator();
    }

    @Override
    public FunctionReducer getReducer() {
        return new ObjectFunctionEvaluator();
    }
}
