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

package notaql.engines.hbase;

import notaql.NotaQL;
import notaql.datamodel.AtomValue;
import notaql.datamodel.ObjectValue;
import notaql.datamodel.Step;
import notaql.datamodel.Value;
import notaql.engines.Engine;
import notaql.engines.EngineEvaluator;
import notaql.engines.hbase.datamodel.ValueConverter;
import notaql.engines.hbase.model.vdata.ColCountFunctionVData;
import notaql.engines.hbase.parser.path.HBaseInputPathParser;
import notaql.engines.hbase.parser.path.HBaseOutputPathParser;
import notaql.engines.hbase.parser.path.HBaseOutputPathVisitor;
import notaql.evaluation.SparkTransformationEvaluator;
import notaql.model.EvaluationException;
import notaql.model.Transformation;
import notaql.model.vdata.ConstructorVData;
import notaql.model.vdata.FunctionVData;
import notaql.parser.TransformationParser;
import notaql.parser.path.InputPathParser;
import notaql.parser.path.OutputPathParser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapred.TableOutputFormat;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapred.JobConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This is the spark implementation of HBase evaluation.
 * It uses the Hadoop connector of HBase in order to read and write data.
 *
 * NOTE: the assumption is that all input values are stored as strings.
 *
 * TODO: Allow more input formats. This could be done by introducing cast functions.
 */
public class HBaseEngineEvaluator implements EngineEvaluator {
    private final Engine engine;
    private final String tableId;

    private final static Logger logger = Logger.getLogger(HBaseEngineEvaluator.class.getName());
    private final TransformationParser parser;

    public HBaseEngineEvaluator(Engine engine, TransformationParser parser, Map<String, AtomValue<?>> params) {
        this.engine = engine;
        this.parser = parser;

        if (!params.keySet().equals(new HashSet<>(engine.getArguments())))
            throw new EvaluationException(
                    "HBase engine expects the following parameters on initialization: " +
                            engine.getArguments().stream().collect(Collectors.joining(", "))
            );

        this.tableId = params.get("table_id").getValue().toString();
    }

    @Override
    public InputPathParser getInputPathParser() {
        return new HBaseInputPathParser(parser);
    }

    @Override
    public OutputPathParser getOutputPathParser() {
        return new HBaseOutputPathParser(parser);
    }

    @Override
    public JavaRDD<ObjectValue> evaluate(Transformation transformation) {
        final SparkTransformationEvaluator evaluator = new SparkTransformationEvaluator(transformation);
        JavaSparkContext sc = NotaQL.SparkFactory.getSparkContext();

        final Configuration conf = createConf();
        conf.set(TableInputFormat.INPUT_TABLE, tableId);

        final JavaPairRDD<ImmutableBytesWritable, Result> inputRDD =
                sc.newAPIHadoopRDD(conf, TableInputFormat.class, ImmutableBytesWritable.class, org.apache.hadoop.hbase.client.Result.class);

        // convert all rows in rdd to inner format
        final JavaRDD<Value> converted = inputRDD.map(t -> ValueConverter.convertToNotaQL(t._2));
        // filter the ones not fulfilling the input filter
        final JavaRDD<Value> filtered = converted.filter(v -> transformation.satisfiesInPredicate((ObjectValue) v));

        // process all input
        return evaluator.process(filtered);
    }

    @Override
    public void store(JavaRDD<ObjectValue> result) {
        if(NotaQL.prop.getProperty("log_output") != null && NotaQL.prop.getProperty("log_output").equals("true")) {
            final List<ObjectValue> collect = result.collect();
            collect.stream().forEach(t -> logger.info("Storing object: " + t.toString()));
        } else {
            logger.info("Storing objects.");
        }

        final Configuration conf = createConf();

        // create Table if it doesn't exist yet
        final HBaseAdmin admin;
        try {
            admin = new HBaseAdmin(conf);

            if(!admin.tableExists(tableId)) {
                logger.info("Table " + tableId + " does not exists yet.");
                final HTableDescriptor descriptor = new HTableDescriptor(TableName.valueOf(tableId));
                descriptor.addFamily(new HColumnDescriptor(HBaseOutputPathVisitor.DEFAULT_COL_FAMILY));
                admin.createTable(descriptor);
            }
            // TODO: disable for performance measuring
            if(true) {
                final HTable table = new HTable(conf, tableId);

                final List<String> families = result
                        .flatMap(ObjectValue::keySet)
                        .map(Step::getStep)
                        .filter(s -> !s.equals("_id") && !s.equals(HBaseOutputPathVisitor.DEFAULT_COL_FAMILY))
                        .distinct()
                        .collect();

                admin.disableTable(table.getTableName());
                families.forEach(s -> createFamily(s, table));
                admin.enableTable(table.getTableName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("Storing result.");

        JavaSparkContext sc = NotaQL.SparkFactory.getSparkContext();

        final JobConf jobConf = new JobConf(conf, this.getClass());
        jobConf.setOutputFormat(TableOutputFormat.class);
        jobConf.set(TableOutputFormat.OUTPUT_TABLE, tableId);

        JavaPairRDD<ImmutableBytesWritable,Put> output = result.filter(HBaseEngineEvaluator::hasCols).mapToPair(
                o -> new Tuple2<>(new ImmutableBytesWritable(), ValueConverter.convertFromNotaQL(o))
        );

        output.saveAsHadoopDataset(jobConf);
        logger.info("Stored result.");
    }

    private static boolean hasCols(ObjectValue o) {
        return o.toMap().entrySet().stream()
                .filter(e -> e.getValue() instanceof ObjectValue)
                .anyMatch(e -> ((ObjectValue) e.getValue()).size() > 0);
    }

    public static void createFamily(String family, HTable table) {
        try {
            final HBaseAdmin admin = new HBaseAdmin(table.getConfiguration());
            // check if column family exists
            boolean exists = false;
            for (HColumnDescriptor familyDescriptor : table.getTableDescriptor().getFamilies()) {
                if(Bytes.toString(familyDescriptor.getName()).equals(family)) {
                    exists = true;
                    break;
                }
            }
            // if not: add it
            if(!exists) {
                admin.addColumn(table.getTableName(), new HColumnDescriptor(family));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Configuration createConf() {
        final String host = NotaQL.prop.getProperty("hbase_host", "localhost");
        Configuration conf = HBaseConfiguration.create();

        conf.set("hbase.zookeeper.quorum", host);
        conf.set("hbase.master", host + ":60000");

        return conf;
    }

    @Override
    public ConstructorVData getConstructor(String name) {
        return null;
    }

    @Override
    public FunctionVData getFunction(String name) {
        if(name.equals("COL_COUNT"))
            return new ColCountFunctionVData();
        return null;
    }

}
