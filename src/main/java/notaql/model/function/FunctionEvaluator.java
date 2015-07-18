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

import notaql.datamodel.fixation.Fixation;
import notaql.evaluation.ValueEvaluationResult;
import notaql.model.vdata.GenericFunctionVData;
import notaql.model.vdata.VData;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Evaluates a function
 */
public interface FunctionEvaluator extends Serializable {
    /**
     * This can be though of as a sort of (flat-)map function, that is executed for each fixation.
     *
     * This is the first step of transforming data. The group and reduction steps are performed in the Reducer.
     *
     * @param args
     * @param fixation
     * @return
     */
    public List<ValueEvaluationResult> evaluate(Arguments args, Fixation fixation);

    /**
     * Provides info if this evaluator also features a Reducer
     *
     * @param args
     * @return
     */
    public boolean canReduce(Arguments args);
}
