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

package notaql;

import notaql.datamodel.ObjectValue;
import notaql.engines.Engine;
import notaql.engines.EngineEvaluator;
import notaql.model.NotaQLExpression;
import notaql.model.Transformation;
import notaql.parser.NotaQLExpressionParser;
import org.apache.log4j.BasicConfigurator;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.*;
import java.net.ConnectException;
import java.util.Properties;

/**
 * The NotaQL main class. Here is where the magic starts.
 */
public class NotaQL {
    public static Properties prop = new Properties();

    /**
     * This is a simple program which expects a NotaQL query to execute.
     * It also allows you to specify a config file.
     *
     * The config file is sometimes necessary to provide information about the whereabouts of databases.
     *
     * @param args
     * @throws IOException
     */
    public static void main(String... args) throws IOException {
        BasicConfigurator.configure();

        if (args.length < 1)
            throw new IllegalArgumentException("The input must be provided as follows: [--config=PATH/TO/settings.config] notaql_query");

        int start = 0;

        if (args[0].startsWith("--config=")) {

            final String path = args[0].substring(args[0].indexOf("=") + 1);

            loadConfig(path);

            start = 1;
        }

        final StringBuilder builder = new StringBuilder();
        for(int i = start; i < args.length; i++) {
            builder.append(args[i]);
            if(i < args.length-1)
                builder.append(" ");
        }

        evaluate(builder.toString());
    }

    /**
     * Load the config file and store it in prop
     *
     * @param path
     * @throws IOException
     */
    public static void loadConfig(String path) throws IOException {
        InputStream stream;
        try {
            stream = new FileInputStream(new File(path));
        } catch(FileNotFoundException e) {
            stream = NotaQL.class.getClassLoader().getResourceAsStream(path);
        }

        if(stream == null) {
            throw new FileNotFoundException("Unable to find " + path + " in classpath.");
        }

        // load a properties file
        prop.load(stream);
    }

    /**
     * Evaluates a query, transformation after transformation.
     *
     * @param query
     * @throws ConnectException
     */
    public static void evaluate(String query) throws ConnectException {
        final NotaQLExpression expression = NotaQLExpressionParser.parse(query);
        for (Transformation transformation : expression.getTransformations()) {
            final EngineEvaluator inEngineEvaluator = transformation.getInEngineEvaluator();
            final EngineEvaluator outEngineEvaluator = transformation.getOutEngineEvaluator();

            final JavaRDD<ObjectValue> result = inEngineEvaluator.evaluate(transformation);

            outEngineEvaluator.store(result);
        }
    }

    public static class SparkFactory {
        private static JavaSparkContext jsc = null;

        private SparkFactory(){

        }

        public static JavaSparkContext getSparkContext() {
            if(jsc != null)
                return jsc;

            SparkConf conf = new SparkConf().setAppName("NotaQL").setMaster(NotaQL.prop.getProperty("spark_master", "local"));
            if(NotaQL.prop.getProperty("parallelism") != null)
                conf.set("spark.default.parallelism", NotaQL.prop.getProperty("parallelism"));
            if(NotaQL.prop.getProperty("kryo") != null && NotaQL.prop.getProperty("kryo").equals("true")) {
                conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
                conf.set("spark.kryoserializer.buffer.mb", "24");
            }

            final SparkContext sc = new SparkContext(conf);
            // TODO: Add progress/timing magic here!

            /*sc.addSparkListener(new SparkListener() {
                @Override
                public void onStageCompleted(SparkListenerStageCompleted sparkListenerStageCompleted) {
                    System.out.println("Stage completed!");
                }

                @Override
                public void onStageSubmitted(SparkListenerStageSubmitted sparkListenerStageSubmitted) {
                    System.out.println("Stage submitted!");
                }

                @Override
                public void onTaskStart(SparkListenerTaskStart sparkListenerTaskStart) {
                    System.out.println("Task started!");
                }

                @Override
                public void onTaskGettingResult(SparkListenerTaskGettingResult sparkListenerTaskGettingResult) {
                    System.out.println("Task getting Result!");
                }

                @Override
                public void onTaskEnd(SparkListenerTaskEnd sparkListenerTaskEnd) {
                    System.out.println("Task end!");
                }

                @Override
                public void onJobStart(SparkListenerJobStart sparkListenerJobStart) {
                    System.out.println("Job start!");
                }

                @Override
                public void onJobEnd(SparkListenerJobEnd sparkListenerJobEnd) {
                    System.out.println("Job end!");
                }

                @Override
                public void onEnvironmentUpdate(SparkListenerEnvironmentUpdate sparkListenerEnvironmentUpdate) {
                    System.out.println("Env update!");
                }

                @Override
                public void onBlockManagerAdded(SparkListenerBlockManagerAdded sparkListenerBlockManagerAdded) {
                    System.out.println("block manager added!");
                }

                @Override
                public void onBlockManagerRemoved(SparkListenerBlockManagerRemoved sparkListenerBlockManagerRemoved) {
                    System.out.println("block manager removed!");
                }

                @Override
                public void onUnpersistRDD(SparkListenerUnpersistRDD sparkListenerUnpersistRDD) {
                    System.out.println("unpersist rdd!");
                }

                @Override
                public void onApplicationStart(SparkListenerApplicationStart sparkListenerApplicationStart) {
                    System.out.println("app start!");
                }

                @Override
                public void onApplicationEnd(SparkListenerApplicationEnd sparkListenerApplicationEnd) {
                    System.out.println("app end!");
                }

                @Override
                public void onExecutorMetricsUpdate(SparkListenerExecutorMetricsUpdate sparkListenerExecutorMetricsUpdate) {
                    System.out.println("metrics update!");
                }
            });*/

            jsc = new JavaSparkContext(sc);

            final String jarPath = prop.getProperty("dependency_jar");

            if(jarPath == null)
                return jsc;

            final File jar = new File(jarPath);

            jsc.addJar(jar.getAbsolutePath());

            return jsc;
        }
    }
}
