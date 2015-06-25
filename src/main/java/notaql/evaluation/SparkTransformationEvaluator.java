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

import notaql.NotaQL;
import notaql.datamodel.ComplexValue;
import notaql.datamodel.ObjectValue;
import notaql.datamodel.Step;
import notaql.datamodel.Value;
import notaql.datamodel.fixation.Fixation;
import notaql.model.Transformation;
import notaql.model.path.IgnoredIdStep;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This is the generic transformation framework for NotaQL.
 *
 * It builds on Spark.
 */
public class SparkTransformationEvaluator implements Serializable {
    private static final long serialVersionUID = 1118170317689014276L;
    private Transformation transformation;

    private final static Logger logger = Logger.getLogger(SparkTransformationEvaluator.class.getName());

    public SparkTransformationEvaluator(Transformation transformation) {
        this.transformation = transformation;
    }

    /**
     * The starting point of the evaluation of a NotaQL transformation.
     *
     * @param values
     * @return
     */
    public JavaRDD<ObjectValue> process(JavaRDD<Value> values) {
        // map
        final JavaRDD<ValueEvaluationResult> evaluated = values
                .flatMap(v -> EvaluatorService.getInstance()
                        .evaluate(transformation.getExpression(), new Fixation((ObjectValue) v)));

        // Aggregate - recursively go down the tree described by the transformation
        final JavaPairRDD<String, Value> reduced = evaluated.mapToPair(r -> new Tuple2<>(r.getValue().groupKey(), r.getValue()))
                .aggregateByKey(
                        EvaluatorService.getInstance().createIdentity(transformation.getExpression()),
                        (a, b) -> EvaluatorService.getInstance().reduce(transformation.getExpression(), a, b),
                        (a, b) -> EvaluatorService.getInstance().reduce(transformation.getExpression(), a, b)
                );

        // finalize all values
        return reduced.map(t -> (ObjectValue) EvaluatorService.getInstance().finalize(transformation.getExpression(), t._2));
    }

    /**
     * Drops ignored values (i.e. OUT._)
     * @param c
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    private <T extends ComplexValue> T clearIgnored(T c) {
        for (Iterator<Map.Entry<Step, Value>> iterator = c.toMap().entrySet().iterator(); iterator.hasNext(); ) {
            final Map.Entry<Step, Value> next = iterator.next();
            if (next.getKey() instanceof IgnoredIdStep.IgnoredStep)
                iterator.remove();

            if (next.getValue() instanceof ComplexValue<?>)
                clearIgnored((ComplexValue<?>) next.getValue());
        }
        return c;
    }

    public static class SparkFactory {
        private static JavaSparkContext sc = null;

        private SparkFactory() {

        }

        public static JavaSparkContext getSparkContext() {
            if (sc != null)
                return sc;

            sc = new JavaSparkContext(NotaQL.prop.getProperty("spark_master", "local"), "NotaQL");

            return sc;
        }
    }

}
