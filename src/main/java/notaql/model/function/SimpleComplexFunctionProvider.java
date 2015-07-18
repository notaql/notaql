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

import notaql.datamodel.Value;
import notaql.engines.Engine;
import notaql.evaluation.Evaluator;
import notaql.evaluation.Reducer;
import notaql.evaluation.SimpleFunctionVDataEvaluator;
import notaql.model.NotaQLException;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides the complex function interface for simple functions for uniform handling in the GenericFunctionVDataEvaluator
 */
public class SimpleComplexFunctionProvider implements  ComplexFunctionProvider {
    private final String name;
    private final Method method;
    private List<Parameter> parameters = new LinkedList<>();

    public SimpleComplexFunctionProvider(String name, Method method) {
        this.name = name;
        this.method = method;

        for (java.lang.reflect.Parameter parameter : method.getParameters()) {
            final Class<? extends Value> clazz;

            try {
                clazz = parameter.getType().asSubclass(Value.class);
            } catch(ClassCastException e) {
                throw new NotaQLException("Method " + name + " has invalid argument type");
            }

            if(parameter.isVarArgs()) {
                parameters.add(new Parameter(parameter.getName(), Parameter.ArgumentType.VAR_ARG));
            } else {
                parameters.add(new Parameter(parameter.getName()));
            }

        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Parameter> getParameters() {
        return parameters;
    }

    @Override
    public boolean isApplicable(Engine inEngine, Engine outEngine) {
        return true;
    }

    @Override
    public Evaluator getEvaluator() {
        return new SimpleFunctionVDataEvaluator(name, method);
    }

    @Override
    public Reducer getReducer() {
        return null;
    }
}
