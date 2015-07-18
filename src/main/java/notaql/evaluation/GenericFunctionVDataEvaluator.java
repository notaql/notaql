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
import notaql.model.NotaQLException;
import notaql.model.function.*;
import notaql.model.vdata.GenericFunctionVData;
import notaql.model.vdata.VData;

import java.lang.reflect.Method;
import java.util.*;

/**
 * This evaluates generic functions.
 * TODO: TESTCASES!
 */
public class GenericFunctionVDataEvaluator implements Evaluator, Reducer {
    private static final long serialVersionUID = 9075850152674486434L;

    private Map<String, ComplexFunctionProvider> functions = new HashMap<>();

    /**
     * Grabs all functions that are available
     */
    public GenericFunctionVDataEvaluator() {
        loadSimpleFunctions();
        loadComplexFunctions();

        validateFunctions();
    }

    private void loadSimpleFunctions() {
        final ServiceLoader<SimpleFunctionProvider> functionProviderLoader = ServiceLoader.load(SimpleFunctionProvider.class);

        // extract all simple functions from the classes
        for (SimpleFunctionProvider simpleFunctionProvider : functionProviderLoader) {
            for (Method method : simpleFunctionProvider.getClass().getMethods()) {
                final SimpleFunction annotation = method.getAnnotation(SimpleFunction.class);
                if(annotation == null)
                    continue;

                final ComplexFunctionProvider prev = functions.put(
                        annotation.name(),
                        new SimpleComplexFunctionProvider(annotation.name(), method)
                );

                // check if there was a name clash
                if(prev != null)
                    throw new NotaQLException(
                            String.format("The function '%1$s' was discovered twice during the loading process.", annotation.name())
                    );
            }
        }
    }

    private void loadComplexFunctions() {
        final ServiceLoader<ComplexFunctionProvider> functionProviderLoader = ServiceLoader.load(ComplexFunctionProvider.class);

        // extract all complex functions from the classes
        for (ComplexFunctionProvider complexFunctionProvider : functionProviderLoader) {
            final ComplexFunctionProvider prev = functions.put(complexFunctionProvider.getName(), complexFunctionProvider);

            // check if there was a name clash
            if(prev != null)
                throw new NotaQLException(
                        String.format("The function '%1$s' was discovered twice during the loading process.", prev.getName())
                );
        }
    }

    /**
     * Checks that the following properties:
     * - Values with default values come after the ones without
     * - Varargs and Keyword arguments are come last (in this order)
     * This is modelled after the python style of function parameters.
     */
    private void validateFunctions() {
        for (Map.Entry<String, ComplexFunctionProvider> complexFunction : functions.entrySet()) {
            final String name = complexFunction.getKey();
            final ComplexFunctionProvider provider = complexFunction.getValue();

            boolean foundDefault = false;
            boolean foundVArgs = false;
            boolean foundKWArgs = false;

            for (Parameter parameter : provider.getParameters()) {
                final Parameter.ArgumentType type = parameter.getArgType();

                if(foundKWArgs)
                    throw new NotaQLException(
                            String.format("The function '%1$s' has key word arguments that do not come last. It is followed by an argument of type %2$s", name, type)
                    );

                if(type == Parameter.ArgumentType.KEYWORD_ARG) {
                    foundKWArgs = true;
                    continue;
                }

                // type != Parameter.ArgumentType.KEYWORD_ARG;

                if(foundVArgs) {
                    throw new NotaQLException(
                            String.format("The function '%1$s' has varargs that do not come last or immediately before the key word arguments. It is followed by an argument of type %2$s", name, type)
                    );
                }

                if(type == Parameter.ArgumentType.VAR_ARG) {
                    foundVArgs = true;
                    continue;
                }

                // type != Parameter.ArgumentType.KEYWORD_ARG && type != Parameter.ArgumentType.VAR_ARG;

                if(type == Parameter.ArgumentType.DEFAULT) {
                    foundDefault = true;
                    continue;
                }

                // type != Parameter.ArgumentType.KEYWORD_ARG && type != Parameter.ArgumentType.VAR_ARG && type != Parameter.ArgumentType.DEFAULT;
                // => NORMAL

                if(foundDefault) {
                    throw new NotaQLException(
                            String.format("The function '%1$s' has non-default argument following default argument.", name)
                    );
                }


            }
        }
    }

    @Override
    public List<ValueEvaluationResult> evaluate(VData vData, Fixation fixation) {
        return getProvider(vData).getEvaluator().evaluate(vData, fixation);
    }

    @Override
    public boolean canReduce(VData vData) {
        return getProvider(vData).getEvaluator().canReduce(vData);
    }

    @Override
    public List<Class<? extends VData>> getProcessedClasses() {
        return Arrays.asList(GenericFunctionVData.class);
    }

    @Override
    public Value reduce(VData vData, Value v1, Value v2) {
        final ComplexFunctionProvider provider = getProvider(vData);

        assert provider.getReducer() != null;

        return provider.getReducer().reduce(vData, v1, v2);
    }

    @Override
    public Value createIdentity(VData vData) {
        final ComplexFunctionProvider provider = getProvider(vData);

        assert provider.getReducer() != null;

        return provider.getReducer().createIdentity(vData);
    }

    @Override
    public Value finalize(VData vData, Value value) {
        final ComplexFunctionProvider provider = getProvider(vData);

        assert provider.getReducer() != null;

        return provider.getReducer().finalize(vData, value);
    }

    private ComplexFunctionProvider getProvider(VData vData) {
        assert vData instanceof GenericFunctionVData;
        final GenericFunctionVData functionVData = (GenericFunctionVData) vData;

        return functions.get(functionVData.getName());
    }
}
