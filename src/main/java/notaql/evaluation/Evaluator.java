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

import notaql.datamodel.fixation.Fixation;
import notaql.model.vdata.VData;

import java.io.Serializable;
import java.util.List;

/**
 * An Evaluator is the initial execution unit for vData expressions.
 * It can be though of as a sort of (flat-)map function, that is executed for each fixation.
 */
public interface Evaluator extends VDataService, Serializable {
    /**
     * This can be though of as a sort of (flat-)map function, that is executed for each fixation.
     *
     * This is the first step of transforming data. The group and reduction steps are performed in the Reducer.
     * @param vData
     * @param fixation
     * @return
     */
    public List<ValueEvaluationResult> evaluate(VData vData, Fixation fixation);

    /**
     * Provides info if this evaluator also features a Reducer
     *
     * TODO: this seems sort of weird? Shouldn't we fix this? A simple instanceOf is enough.
     * @param vData
     * @return
     */
    public boolean canReduce(VData vData);

}
