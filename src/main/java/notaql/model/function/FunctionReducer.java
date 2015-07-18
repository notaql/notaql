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

import java.io.Serializable;

/**
 * This reducer defines how (partial) values which were created by the evaluation step are combined (reduced).
 */
public interface FunctionReducer extends Serializable {
    /**
     * This combines two (partial) values from the evaluation step (i.e. map)
     *
     * This can be thought of as the function that is passed in the reduce step.
     * @param args
     * @param v1
     * @param v2
     * @return
     */
    public Value reduce(Arguments args, Value v1, Value v2);

    /**
     * This is the neutral element, with which the reduction chain is started.
     * @param args
     * @return
     */
    public Value createIdentity(Arguments args);

    /**
     * This is executed after the reduction step is done.
     * This can be used in order to clean up the values (e.g. transform a PartialObjectValue to an ObjectValue)
     * @param args
     * @param value
     * @return
     */
    public Value finalize(Arguments args, Value value);
}
