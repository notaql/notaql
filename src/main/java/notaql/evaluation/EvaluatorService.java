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
import notaql.model.vdata.VData;

import java.util.*;

/**
 * This allows easy access to evaluating parts of a transformation.
 */
public class EvaluatorService implements Evaluator, Reducer {
    private static final long serialVersionUID = -7037962637650845525L;
    private static EvaluatorService service;
    private Map<Class<? extends VData>, Evaluator> evaluators = new HashMap<>();
    private Map<Class<? extends VData>, Reducer> reducers = new HashMap<>();

    private EvaluatorService() {
        final ServiceLoader<Evaluator> evaluatorLoader = ServiceLoader.load(Evaluator.class);
        final ServiceLoader<Reducer> reducerLoader = ServiceLoader.load(Reducer.class);

        for (Evaluator evaluator : evaluatorLoader) {
            for (Class<? extends VData> aClass : evaluator.getProcessedClasses()) {
                evaluators.put(aClass, evaluator);
            }
        }
        for (Reducer reducer : reducerLoader) {
            for (Class<? extends VData> aClass : reducer.getProcessedClasses()) {
                reducers.put(aClass, reducer);
            }
        }
    }

    public static synchronized EvaluatorService getInstance() {
        if (service == null) {
            service = new EvaluatorService();
        }
        return service;
    }

    private Evaluator getEvaluator(VData vData) {
        return evaluators.get(vData.getClass());
    }

    private Reducer getReducer(VData vData) {
        return reducers.get(vData.getClass());
    }

    /**
     * Call the evaluator that is meant to evaluate the given vData.
     * @param vData
     * @param fixation
     * @return
     */
    @Override
    public List<ValueEvaluationResult> evaluate(VData vData, Fixation fixation) {
        final Evaluator evaluator = getEvaluator(vData);

        if(evaluator == null)
            throw new EvaluationException("VData type has no evaluator at the moment: " + vData.toString());

        return evaluator.evaluate(vData, fixation);
    }

    /**
     * Call the evaluator that is meant to evaluate the given vData and see if it reduces.
     *
     * @param vData
     * @return
     */
    @Override
    public boolean canReduce(VData vData) {
        final Evaluator evaluator = getEvaluator(vData);

        if(evaluator == null)
            throw new EvaluationException("VData type has no evaluator at the moment: " + vData.toString());

        return evaluator.canReduce(vData);
    }

    /**
     * Call the reducer that is meant to reduce the given vData.
     *
     * @param vData
     * @param v1
     * @param v2
     * @return
     */
    @Override
    public Value reduce(VData vData, Value v1, Value v2) {
        final Reducer reducer = getReducer(vData);

        if(reducer == null)
            throw new EvaluationException("VData type has no reducer at the moment: " + vData.toString());

        return reducer.reduce(vData, v1, v2);
    }

    /**
     * Call the reducer that is meant to reduce the given vData and retrieve the identity element (neutral element).
     *
     * @param vData The instance of vData, that this reducer is built for
     * @return
     */
    @Override
    public Value createIdentity(VData vData) {
        final Reducer reducer = getReducer(vData);

        if(reducer == null)
            throw new EvaluationException("VData type has no reducer at the moment: " + vData.toString());

        return reducer.createIdentity(vData);
    }

    /**
     * Call the reducer that is meant to reduce the given vData and finalize the given data.
     *
     * @param vData The instance of vData, that this reducer is built for
     * @return
     */
    @Override
    public Value finalize(VData vData, Value value) {
        final Reducer reducer = getReducer(vData);

        if(reducer == null)
            throw new EvaluationException("VData type has no reducer at the moment: " + vData.toString());

        return reducer.finalize(vData, value);
    }

    @Override
    public List<Class<? extends VData>> getProcessedClasses() {
        throw new UnsupportedOperationException();
    }
}
