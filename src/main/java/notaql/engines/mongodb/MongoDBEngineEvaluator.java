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

package notaql.engines.mongodb;

import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.hadoop.MongoInputFormat;
import com.mongodb.hadoop.MongoOutputFormat;
import notaql.NotaQL;
import notaql.datamodel.AtomValue;
import notaql.datamodel.BooleanValue;
import notaql.datamodel.ObjectValue;
import notaql.datamodel.Value;
import notaql.engines.Engine;
import notaql.engines.EngineEvaluator;
import notaql.engines.mongodb.datamodel.ValueConverter;
import notaql.engines.mongodb.model.vdata.HashFunctionVData;
import notaql.engines.mongodb.model.vdata.ListCountFunctionVData;
import notaql.engines.mongodb.model.vdata.ObjectIdFunctionVData;
import notaql.engines.mongodb.parser.path.MongoDBInputPathParser;
import notaql.engines.mongodb.parser.path.MongoDBOutputPathParser;
import notaql.evaluation.SparkTransformationEvaluator;
import notaql.model.EvaluationException;
import notaql.model.Transformation;
import notaql.model.vdata.ConstructorVData;
import notaql.model.vdata.FunctionVData;
import notaql.parser.TransformationParser;
import notaql.parser.path.InputPathParser;
import notaql.parser.path.OutputPathParser;
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.bson.BSONObject;
import scala.Tuple2;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by thomas on 23.02.15.
 */
public class MongoDBEngineEvaluator implements EngineEvaluator {
    private final TransformationParser parser;
    private final boolean noQuery;
    private Engine engine;

    private String databaseName;
    private String collectionName;

    private MongoClient mongoClient = null;
    private DB database = null;

    private final static Logger logger = Logger.getLogger(MongoDBEngineEvaluator.class.getName());

    public MongoDBEngineEvaluator(Engine engine, TransformationParser parser, Map<String, AtomValue<?>> params) {
        this.engine = engine;
        this.parser = parser;

        if(!params.containsKey("database_name") || !params.containsKey("collection_name"))
            throw new EvaluationException(
                    "MongoDB engine expects the following parameters on initialization: " +
                            engine.getArguments().stream().collect(Collectors.joining(", "))
            );

        this.databaseName = params.get("database_name").getValue().toString();
        this.collectionName = params.get("collection_name").getValue().toString();
        this.noQuery = (Boolean)params.getOrDefault("no_query", new BooleanValue(false)).getValue();
    }


    @Override
    public InputPathParser getInputPathParser() {
        return new MongoDBInputPathParser(parser);
    }

    @Override
    public OutputPathParser getOutputPathParser() {
        return new MongoDBOutputPathParser(parser);
    }

    /**
     * Uses the Hadoop API to get the input objects. Then uses the geneesc framewes to do tes evaluesion.es
     *
     * @param transformation
     * @return
     */
    @Override
    public JavaRDD<ObjectValue> evaluate(Transformation transformation) {
        JavaSparkContext sc = NotaQL.SparkFactory.getSparkContext();

        String mongoDBHost = "mongodb://" + NotaQL.prop.getProperty("mongodb_host", "localhost") + ":27017/";

        Configuration config = new Configuration();
        config.set("mongo.input.uri", mongoDBHost + databaseName + "." + collectionName);

        // add partial filter to query in mongodb
        if(!noQuery && transformation.getInPredicate() != null) {
            final BSONObject query = FilterTranslator.toMongoDBQuery(transformation.getInPredicate());
            logger.info("Sending query to MongoDB: " + query.toString());
            config.set("mongo.input.query", query.toString());
        }

        final SparkTransformationEvaluator evaluator = new SparkTransformationEvaluator(transformation);

        JavaPairRDD<Object, BSONObject> mongoRDD = sc.newAPIHadoopRDD(config, MongoInputFormat.class, Object.class, BSONObject.class);

        // convert all objects in rdd to inner format
        final JavaRDD<Value> converted = mongoRDD.map(t -> ValueConverter.convertToNotaQL(t._2));
        // filter the ones not fulfilling the input filter (queries of MongoDB are less expressive than NotaQL)
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

        String mongoDBHost = "mongodb://" + NotaQL.prop.getProperty("mongodb_host", "localhost") + ":27017/";

        Configuration config = new Configuration();
        config.set("mongo.output.uri", mongoDBHost + databaseName + "." + collectionName);

        JavaPairRDD<Object,BSONObject> output = result.mapToPair(
                o -> new Tuple2<>(null, (DBObject)ValueConverter.convertFromNotaQL(o))
        );

        if(NotaQL.prop.getProperty("log_output") != null && NotaQL.prop.getProperty("log_output").equals("true"))
            output.foreach(t -> logger.info("Storing object: " + t._2.toString()));
        else
            logger.info("Storing objects.");

        output.saveAsNewAPIHadoopFile("file:///notapplicable",
                Object.class, Object.class, MongoOutputFormat.class, config);
        logger.info("Stored result.");
    }

    private void connect() throws ConnectException {
        try {
            mongoClient = new MongoClient(NotaQL.prop.getProperty("mongodb_host", "localhost"));
            database = mongoClient.getDB(databaseName);
        } catch (UnknownHostException e) {
            throw new ConnectException(e.toString());
        }
    }

    private void disconnect() {
        mongoClient.close();
    }

    @Override
    public ConstructorVData getConstructor(String name) {
        return null;
    }

    @Override
    public FunctionVData getFunction(String name) {
        if(name.equals("LIST_COUNT"))
            return new ListCountFunctionVData();
        if(name.equals("HASH"))
            return new HashFunctionVData();
        if(name.equals("OBJECT_ID"))
            return new ObjectIdFunctionVData();
        return null;
    }
}
