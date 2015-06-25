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

package notaql.engines.redis;

import notaql.NotaQL;
import notaql.datamodel.*;
import notaql.engines.Engine;
import notaql.engines.EngineEvaluator;
import notaql.engines.mongodb.model.vdata.ListCountFunctionVData;
import notaql.engines.redis.datamodel.ValueConverter;
import notaql.engines.redis.model.vdata.HashMapConstructorVData;
import notaql.engines.redis.parser.path.RedisInputPathParser;
import notaql.engines.redis.parser.path.RedisOutputPathParser;
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
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanResult;

import java.net.ConnectException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by thomas on 23.02.15.
 */
public class RedisEngineEvaluator implements EngineEvaluator {
    private final TransformationParser parser;
    private final int databaseId;

    private Jedis jedis;
    private Engine engine;

    private final static Logger logger = Logger.getLogger(RedisEngineEvaluator.class.getName());

    public RedisEngineEvaluator(Engine engine, TransformationParser parser, Map<String, AtomValue<?>> params) {
        this.engine = engine;
        this.parser = parser;

        if (!params.keySet().equals(new HashSet<>(engine.getArguments())))
            throw new EvaluationException(
                    "CSV engine expects the following parameters on initialization: " +
                            engine.getArguments().stream().collect(Collectors.joining(", "))
            );

        try {
            this.databaseId = Integer.parseInt(params.get("database_id").getValue().toString());
        } catch(NumberFormatException e) {
            throw new EvaluationException("Redis engines expects the database_id to be an integer");
        }
    }

    @Override
    public InputPathParser getInputPathParser() {
        return new RedisInputPathParser(parser);
    }

    @Override
    public OutputPathParser getOutputPathParser() {
        return new RedisOutputPathParser(parser);
    }

    /**
     * TODO: this is really slow and doesn't scale!
     * @param transformation
     * @return
     */
    @Override
    public JavaRDD<ObjectValue> evaluate(Transformation transformation) {
        try {
            connect();
        } catch (ConnectException e) {
            e.printStackTrace();
            throw new EvaluationException(e);
        }

        final JavaSparkContext sc = NotaQL.SparkFactory.getSparkContext();

        final SparkTransformationEvaluator evaluator = new SparkTransformationEvaluator(transformation);

        // simply read in all objects
        final ListBasedCollectionValue inCollection = new ListBasedCollectionValue();
        jedis.select(databaseId);
        final Set<String> keys = jedis.keys("*");

        final String host = NotaQL.prop.getProperty("redis_host", "localhost");
        final ValueConverter converter = new ValueConverter(host, databaseId);

        for (String key: keys) {
            final Value value = converter.readFromRedis(key);

            final ObjectValue objectValue = new ObjectValue();
            objectValue.put(new Step<>("_id"), new StringValue(key));
            objectValue.put(new Step<>("_v"), value);

            inCollection.add(objectValue);
        }

        disconnect();

        final JavaRDD<Value> values = sc.parallelize(inCollection);

        final JavaRDD<Value> filtered = values.filter(v -> transformation.satisfiesInPredicate((ObjectValue) v));

        return evaluator.process(filtered);
    }

    /**
     * @param result
     */
    @Override
    public void store(JavaRDD<ObjectValue> result) {
        try {
            connect();
        } catch (ConnectException e) {
            e.printStackTrace();
            throw new EvaluationException(e);
        }

        jedis.select(databaseId);

        logger.info("Storing objects.");

        final String host = NotaQL.prop.getProperty("redis_host", "localhost");
        final ValueConverter converter = new ValueConverter(host, databaseId);

        result.foreach(
                converter::writeToRedis
        );

        disconnect();
    }

    private void connect() throws ConnectException {
        jedis = new Jedis(NotaQL.prop.getProperty("redis_host", "localhost"));
    }

    private void disconnect() {
        jedis.close();
    }


    @Override
    public ConstructorVData getConstructor(String name) {
        if(name.equals("HM"))
            return new HashMapConstructorVData();
        return null;
    }

    @Override
    public FunctionVData getFunction(String name) {
        if(name.equals("LIST_COUNT"))
            return new ListCountFunctionVData();
        return null;
    }
}
