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
import java.util.stream.Collectors;

/**
 * This evaluates generic functions.
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

    private void validateFunctions() {
        functions.values().stream().forEach(ComplexFunctionProvider.Resolver::validate);
    }

    @Override
    public List<ValueEvaluationResult> evaluate(VData vData, Fixation fixation) {
        assert vData instanceof GenericFunctionVData;

        final GenericFunctionVData functionVData = (GenericFunctionVData) vData;

        final ComplexFunctionProvider provider = getProvider(functionVData);

        return provider.getEvaluator().evaluate(
                ComplexFunctionProvider.Resolver.extractArgs(provider, functionVData.getArgs()),
                fixation
        );
    }

    @Override
    public boolean canReduce(VData vData) {
        assert vData instanceof GenericFunctionVData;

        final GenericFunctionVData functionVData = (GenericFunctionVData) vData;

        final ComplexFunctionProvider provider = getProvider(functionVData);

        return provider.getEvaluator()
                .canReduce(ComplexFunctionProvider.Resolver.extractArgs(provider, functionVData.getArgs()));
    }

    @Override
    public List<Class<? extends VData>> getProcessedClasses() {
        return Arrays.asList(GenericFunctionVData.class);
    }

    @Override
    public Value reduce(VData vData, Value v1, Value v2) {
        assert vData instanceof GenericFunctionVData;

        final GenericFunctionVData functionVData = (GenericFunctionVData) vData;

        final ComplexFunctionProvider provider = getProvider(functionVData);

        assert provider.getReducer() != null;

        return provider.getReducer()
                .reduce(
                        ComplexFunctionProvider.Resolver.extractArgs(provider, functionVData.getArgs()),
                        v1,
                        v2
                );
    }

    @Override
    public Value createIdentity(VData vData) {
        assert vData instanceof GenericFunctionVData;

        final GenericFunctionVData functionVData = (GenericFunctionVData) vData;

        final ComplexFunctionProvider provider = getProvider(functionVData);

        assert provider.getReducer() != null;

        return provider.getReducer()
                .createIdentity(ComplexFunctionProvider.Resolver.extractArgs(provider, functionVData.getArgs()));
    }

    @Override
    public Value finalize(VData vData, Value value) {
        assert vData instanceof GenericFunctionVData;

        final GenericFunctionVData functionVData = (GenericFunctionVData) vData;

        final ComplexFunctionProvider provider = getProvider(functionVData);

        assert provider.getReducer() != null;

        return provider.getReducer().finalize(ComplexFunctionProvider.Resolver.extractArgs(provider, functionVData.getArgs()), value);
    }

    private ComplexFunctionProvider getProvider(GenericFunctionVData vData) {
        return functions.get(vData.getName());
    }


}
