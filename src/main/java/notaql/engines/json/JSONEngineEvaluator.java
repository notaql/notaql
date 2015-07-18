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

package notaql.engines.json;

import notaql.NotaQL;
import notaql.datamodel.AtomValue;
import notaql.datamodel.ObjectValue;
import notaql.datamodel.Value;
import notaql.engines.Engine;
import notaql.engines.EngineEvaluator;
import notaql.engines.json.datamodel.ValueConverter;
import notaql.engines.json.path.JSONInputPathParser;
import notaql.engines.json.path.JSONOutputPathParser;
import notaql.evaluation.SparkTransformationEvaluator;
import notaql.model.EvaluationException;
import notaql.model.Transformation;
import notaql.model.vdata.ConstructorVData;
import notaql.model.vdata.FunctionVData;
import notaql.parser.TransformationParser;
import notaql.parser.path.InputPathParser;
import notaql.parser.path.OutputPathParser;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.json.JSONObject;

import java.net.ConnectException;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by thomas on 23.02.15.
 */
public class JSONEngineEvaluator implements EngineEvaluator {
    private final TransformationParser parser;
    private Engine engine;

    private String path;

    private final static Logger logger = Logger.getLogger(JSONEngineEvaluator.class.getName());

    public JSONEngineEvaluator(Engine engine, TransformationParser parser, Map<String, AtomValue<?>> params) {
        this.engine = engine;
        this.parser = parser;

        if (!params.keySet().equals(new HashSet<>(engine.getArguments())))
            throw new EvaluationException(
                    "JSON engine expects the following parameter on initialization: " +
                            engine.getArguments().stream().collect(Collectors.joining(", "))
            );

        this.path = params.get("path").getValue().toString();
    }


    @Override
    public InputPathParser getInputPathParser() {
        return new JSONInputPathParser(parser);
    }

    @Override
    public OutputPathParser getOutputPathParser() {
        return new JSONOutputPathParser(parser);
    }

    /**
     * Uses the Hadoop API to get the input objects. Then uses the generic framework to do the evaluation.
     *
     * @param transformation
     * @return
     */
    @Override
    public JavaRDD<ObjectValue> evaluate(Transformation transformation) {
        JavaSparkContext sc = NotaQL.SparkFactory.getSparkContext();

        final SparkTransformationEvaluator evaluator = new SparkTransformationEvaluator(transformation);

        final JavaRDD<Object> jsonRDD = sc.textFile(path)
                .filter(s -> !s.equals("[") && !s.equals("]")) // if it consists of an array containing objects: just ignore the array TODO: this is kind of Q&D
                .map(JSONObject::new);

        // convert all objects in rdd to inner format
        final JavaRDD<Value> converted = jsonRDD.map(ValueConverter::convertToNotaQL);
        // filter the ones not fulfilling the input filter
        final JavaRDD<Value> filtered = converted.filter(v -> transformation.satisfiesInPredicate((ObjectValue) v));

        // process all input
        return evaluator.process(filtered);
    }

    /**
     * Uses the Hadoop API to store results
     * @param result
     */
    @Override
    public void store(JavaRDD<ObjectValue> result) {
        logger.info("Storing result.");

        JavaSparkContext sc = NotaQL.SparkFactory.getSparkContext();

        JavaRDD<Object> output = result.map(ValueConverter::convertFromNotaQL);

        if(NotaQL.prop.getProperty("log_output") != null && NotaQL.prop.getProperty("log_output").equals("true"))
            output.foreach(s -> logger.info("Storing object: " + s.toString()));
        else
            logger.info("Storing objects.");

        output.map(Object::toString).saveAsTextFile(path);

        logger.info("Stored result.");
    }

    private void connect() throws ConnectException {
    }

    private void disconnect() {

    }

    /*
    @Override
    public ConstructorVData getConstructor(String name) {
        return null;
    }

    @Override
    public FunctionVData getFunction(String name) {
        return null;
    }
    */
}
