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
import notaql.model.EvaluationException;
import notaql.model.function.FunctionProvider;
import notaql.model.function.SimpleFunction;
import notaql.model.vdata.GenericFunctionVData;
import notaql.model.vdata.VData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * This evaluates generic functions. Right now this just supports simple functions
 * (because they are currently the only pluggable functions).
 *
 * TODO: Make sure to translate all other functions to reduce this weird inconsistent state
 */
public class GenericFunctionVDataEvaluator implements Evaluator {

    private Map<String, Method> simpleFunctions = new HashMap<>();

    /**
     * Grabs all functions that are available
     */
    public GenericFunctionVDataEvaluator() {
        final ServiceLoader<FunctionProvider> functionProviderLoader = ServiceLoader.load(FunctionProvider.class);

        // extract all simpleFunctions from the classes
        for (FunctionProvider functionProvider : functionProviderLoader) {
            for (Method method : functionProvider.getClass().getMethods()) {
                final SimpleFunction annotation = method.getAnnotation(SimpleFunction.class);
                if(annotation == null)
                    continue;

                // make sure that the return type is okay
                if(!Value.class.isAssignableFrom(method.getReturnType())) {
                    throw new AssertionError("Method " + annotation.name() + " has invalid return type");
                }

                simpleFunctions.put(annotation.name(), method);
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

        final VData[] args = functionVData.getArgs();
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
