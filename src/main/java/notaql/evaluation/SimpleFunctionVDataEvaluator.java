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
import notaql.model.function.Argument;
import notaql.model.function.Arguments;
import notaql.model.function.FunctionEvaluator;
import notaql.model.vdata.GenericFunctionVData;
import notaql.model.vdata.VData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * This evaluates simple functions.
 */
public class SimpleFunctionVDataEvaluator implements FunctionEvaluator {
    private final String name;
    private final Method method;

    public SimpleFunctionVDataEvaluator(String name, Method method) {
        this.name = name;
        this.method = method;
    }

    /**
     * Evaluates simple functions in the way, that all arguments are evaluated. In case one argument is ambiguous,
     * we simply evaluate the function a couple of times.
     * If more than one is ambiguous we throw an exception. A cross product would be possible, but as of now we did
     * not encounter a real usecase for that.
     *
     * TODO: This is missing support for named arguments and varargs!
     * FIXME: This assumes correct argument order and ignores keywords
     *
     * @param args
     * @param fixation
     * @return
     */
    @Override
    public List<ValueEvaluationResult> evaluate(Arguments args, Fixation fixation) {
        if(args.getVArgs().size() > 0)
            throw new EvaluationException("Varargs are not yet supported for simple functions.");

        final List<List<ValueEvaluationResult>> results = new LinkedList<>();

        if(args.getKWArgs().size() != method.getParameterCount())
            throw new EvaluationException(name + " was provided with the wrong amount of arguments.");

        Fixation lastFixation = fixation;

        // evaluate each argument
        for (Argument arg : args.getKWArgs()) {
            final List<ValueEvaluationResult> evaluate = EvaluatorService.getInstance().evaluate(arg.getVData(), lastFixation);
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
                    throw new EvaluationException(name + ": Two arguments were ambigous. This is not (yet) supported.");

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
                    throw new EvaluationException(name + " encountered wrong types");
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
            throw new EvaluationException("Method " + name + " could not be invoked.", e);
        }

        return returns;
    }

    @Override
    public boolean canReduce(Arguments args) {
        return false;
    }
}
