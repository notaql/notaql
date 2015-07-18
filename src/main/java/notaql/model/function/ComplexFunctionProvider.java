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
import notaql.evaluation.Evaluator;
import notaql.evaluation.Reducer;

import java.util.List;

/**
 * Provides complex functions which may deeply influence the evaluation process.
 */
public interface ComplexFunctionProvider {
    /**
     * Provides the function name (used in the NotaQL transformations)
     * @return
     */
    public String getName();

    /**
     * Provides the parameters that the function accepts.
     * Every usage of this function is checked for correctness in terms of parameters (i.e. number of parameters and order - this is in Python style)
     * @return
     */
    public List<Parameter> getParameters();

    /**
     * Tells if the function can be used with the given in and out engines
     * @param inEngine
     * @param outEngine
     * @return
     */
    public boolean isApplicable(Engine inEngine, Engine outEngine);

    /**
     * Provides the evaluator for this function
     * @return
     */
    public Evaluator getEvaluator();

    /**
     * Provides the reducer for this function
     * @return
     */
    public Reducer getReducer();
}
