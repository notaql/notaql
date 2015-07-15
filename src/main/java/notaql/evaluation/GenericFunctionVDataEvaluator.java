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

import com.google.common.collect.Sets;
import notaql.NotaQL;
import notaql.datamodel.Value;
import notaql.datamodel.fixation.Fixation;
import notaql.model.EvaluationException;
import notaql.model.NotaQLException;
import notaql.model.function.*;
import notaql.model.vdata.GenericFunctionVData;
import notaql.model.vdata.VData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * This evaluates generic functions. Right now this just supports simple functions
 * (because they are currently the only pluggable functions).
 */
public class GenericFunctionVDataEvaluator implements Evaluator {
    private Map<String, Method> simpleFunctions = new HashMap<>();
    private Map<String, ComplexFunctionProvider> complexFunctions = new HashMap<>();

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

                final Method prev = simpleFunctions.put(annotation.name(), method);

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
            final ComplexFunctionProvider prev = complexFunctions.put(complexFunctionProvider.getName(), complexFunctionProvider);

            // check if there was a name clash
            if(prev != null)
                throw new NotaQLException(
                        String.format("The function '%1$s' was discovered twice during the loading process.", prev.getName())
                );
        }
    }

    private void validateFunctions() {
        // check if there was a name clash
        final Sets.SetView<String> duplicateFunctions = Sets.intersection(simpleFunctions.keySet(), complexFunctions.keySet());
        if(!duplicateFunctions.isEmpty())
            throw new NotaQLException(
                    String.format("The functions '%1$s' were discovered twice during the loading process.", duplicateFunctions.toString())
            );

        validateSimpleFunctions();
        validateComplexFunctions();
    }

    private void validateSimpleFunctions() {
        for (Map.Entry<String, Method> simpleFunction : simpleFunctions.entrySet()) {
            final String name = simpleFunction.getKey();
            final Method method = simpleFunction.getValue();

            if(!Value.class.isAssignableFrom(method.getReturnType())) {
                throw new NotaQLException("Method " + name + " has invalid return type");
            }

            // TODO: some checking still missing
        }
    }

    /**
     * Checks that the following properties:
     * - Values with default values come after the ones without
     * - Varargs are come last
     */
    private void validateComplexFunctions() {
        for (Map.Entry<String, ComplexFunctionProvider> complexFunction : complexFunctions.entrySet()) {
            final String name = complexFunction.getKey();
            final ComplexFunctionProvider provider = complexFunction.getValue();

            boolean foundDefault = false;
            boolean foundVArgs = false;

            for (Parameter parameter : provider.getParameters()) {
                if(foundVArgs) {
                    throw new NotaQLException(
                            String.format("The function '%1$s' has vargs that do not come last.", name)
                    );
                }

                if(parameter.getDefaultValue() != null) {
                    foundDefault = true;
                } else if(foundDefault) {
                    throw new NotaQLException(
                            String.format("The function '%1$s' has non-default argument following default argument.", name)
                    );
                }
                if(parameter.isVarArg()) {
                    foundVArgs = true;
                }
            }
        }
    }

    /**
     * Evaluates simple functions in the way, that all arguments are evaluated. In case one argument is ambiguous,
     * we simply evaluate the function a couple of times.
     * If more than one is ambiguous we throw an exception. A cross product would be possible, but as of now we did
     * not encounter a real usecase for that.
     *
     * @param vData
     * @param fixation
     * @return
     */
    @Override
    public List<ValueEvaluationResult> evaluate(VData vData, Fixation fixation) {
        assert vData instanceof GenericFunctionVData;
        final GenericFunctionVData functionVData = (GenericFunctionVData) vData;
        final Method method = simpleFunctions.get(functionVData.getName());
        if(method == null)
            throw new EvaluationException(functionVData.getName() + " is an unknown function.");

        final List<Argument> args = functionVData.getArgs();
        final List<List<ValueEvaluationResult>> results = new LinkedList<>();

        if(args.length < method.getParameterCount())
            throw new EvaluationException(functionVData.getName() + " was provided with too few arguments.");

        Fixation lastFixation = fixation;

        // evaluate each argument
        for (VData arg : args) {
            final List<ValueEvaluationResult> evaluate = EvaluatorService.getInstance().evaluate(arg, lastFixation);
            if(evaluate.size() > 0)
                lastFixation = evaluate.get(evaluate.size() - 1).getFixation();
            results.add(evaluate);
        }

        // make sure that at most one argument is ambigous.
        int ambigous = -1;
        int i = 0;
        for (List<ValueEvaluationResult> result : results) {
            if(result.size() > 1) {
                if (ambigous > -1)
                    throw new EvaluationException(functionVData.getName() + ": Two arguments were ambigous. This is not (yet) supported.");

                ambigous = i;
            }

            i++;
        }

        final Iterator<List<ValueEvaluationResult>> iterator = results.iterator();

        // check if types match
        // TODO: add support for ... parameters
        for (Class<?> aClass : method.getParameterTypes()) {
            // guaranteed before
            assert iterator.hasNext();

            final List<ValueEvaluationResult> result = iterator.next();

            // check if types match
            for (ValueEvaluationResult evaluationResult : result) {
                if(!aClass.isAssignableFrom(evaluationResult.getValue().getClass()))
                    throw new EvaluationException(functionVData.getName() + " encountered wrong types");
            }
        }

        Object[] params = new Object[results.size()];

        // copy params
        int j = 0;
        for (List<ValueEvaluationResult> result : results) {
            if(j != ambigous) {
                params[j] = result.get(0).getValue();
            }
            j++;
        }

        final List<ValueEvaluationResult> returns = new LinkedList<>();

        // invoke the function and store the results in results
        try {
            if(ambigous > -1) {
                for (ValueEvaluationResult evaluationResult : results.get(ambigous)) {
                    params[ambigous] = evaluationResult.getValue();
                    final Object invoke = method.invoke(null, params);

                    assert invoke instanceof Value;
                    returns.add(new ValueEvaluationResult((Value)invoke, lastFixation));
                }
            } else {
                final Object invoke = method.invoke(null, params);

                assert invoke instanceof Value;
                returns.add(new ValueEvaluationResult((Value)invoke, lastFixation));
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new EvaluationException("Method " + functionVData.getName() + " could not be invoked.", e);
        }

        return returns;
    }

    @Override
    public boolean canReduce(VData vData) {
        return false;
    }

    @Override
    public List<Class<? extends VData>> getProcessedClasses() {
        return Arrays.asList(GenericFunctionVData.class);
    }
}
