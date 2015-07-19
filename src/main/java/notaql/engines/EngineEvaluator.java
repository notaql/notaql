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

package notaql.engines;

import notaql.datamodel.ObjectValue;
import notaql.model.Transformation;
import notaql.parser.path.InputPathParser;
import notaql.parser.path.OutputPathParser;
import org.apache.spark.api.java.JavaRDD;


/**
 * An EngineEvaluator is where the engines specify how transformations should be evaluated for the given store.
 *
 * Here we define the ways to access data and store it as well as influence the evaluation process.
 */
public interface EngineEvaluator {
    /**
     * Provides the inputpath parser
     */
    public InputPathParser getInputPathParser();

    /**
     * Provides the outputpath parser
     */
    public OutputPathParser getOutputPathParser();

    /**
     * Evaluates the transformation using Spark.
     *
     * @param transformation
     * @return
     */
    public JavaRDD<ObjectValue> evaluate(Transformation transformation);

    /**
     * Stores the result in the database using spark.
     *
     * @param result
     */
    public void store(JavaRDD<ObjectValue> result);

}
