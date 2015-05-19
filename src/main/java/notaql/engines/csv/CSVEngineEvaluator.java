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

package notaql.engines.csv;

import notaql.NotaQL;
import notaql.datamodel.AtomValue;
import notaql.datamodel.ObjectValue;
import notaql.datamodel.Step;
import notaql.datamodel.Value;
import notaql.engines.Engine;
import notaql.engines.EngineEvaluator;
import notaql.engines.csv.datamodel.ValueConverter;
import notaql.engines.csv.model.vdata.ColCountFunctionVData;
import notaql.engines.csv.path.CSVInputPathParser;
import notaql.engines.csv.path.CSVOutputPathParser;
import notaql.evaluation.SparkTransformationEvaluator;
import notaql.model.EvaluationException;
import notaql.model.Transformation;
import notaql.model.vdata.ConstructorVData;
import notaql.model.vdata.FunctionVData;
import notaql.parser.TransformationParser;
import notaql.parser.path.InputPathParser;
import notaql.parser.path.OutputPathParser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This is the simple spark implementation of CSV evaluation.
 * It reads the text file and uses the Apache CSV lib to do so.
 *
 * The expected format is "DEFAULT" with headers.
 *
 * TODO: more options could be provided to further specify the format.
 */
public class CSVEngineEvaluator implements EngineEvaluator {
    private final TransformationParser parser;
    private String path;

    private final static Logger logger = Logger.getLogger(CSVEngineEvaluator.class.getName());
    private Engine engine;

    public CSVEngineEvaluator(Engine engine, TransformationParser parser, Map<String, AtomValue<?>> params) {
        this.parser = parser;
        this.engine = engine;

        if (!params.keySet().equals(new HashSet<>(engine.getArguments())))
            throw new EvaluationException(
                    "CSV engine expects the following parameters on initialization: " +
                            engine.getArguments().stream().collect(Collectors.joining(", "))
            );

        this.path = params.get("csv_path").getValue().toString();
    }

    @Override
    public InputPathParser getInputPathParser() {
        return new CSVInputPathParser(parser);
    }

    @Override
    public OutputPathParser getOutputPathParser() {
        return new CSVOutputPathParser(parser);
    }

    /**
     * Evaluates the given transformation.
     *
     * This first parses the document (with the first line being the header) and then evaluates on our framework.
     *
     * TODO: this assumes a header line. It might happen that it is not provided.
     *
     * @param transformation
     * @return
     */
    @Override
    public JavaRDD<ObjectValue> evaluate(Transformation transformation) {
        final SparkTransformationEvaluator evaluator = new SparkTransformationEvaluator(transformation);

        final JavaSparkContext sc = NotaQL.SparkFactory.getSparkContext();

        final CSVFormat format = CSVFormat.DEFAULT;

        final JavaRDD<String> csv = sc.textFile(path);

        final String first = csv.first();

        final CSVRecord header;
        try {
            header = format.parse(new StringReader(first)).iterator().next();
        } catch (IOException e) {
            e.printStackTrace();
            throw new AssertionError("Header could not be read for some reason.");
        }

        String[] headerCols = new String[header.size()];
        for (int i = 0; i < header.size(); i++) {
            headerCols[i] = header.get(i);
        }

        final CSVFormat headerFormat = CSVFormat.DEFAULT.withHeader(headerCols);

        final JavaRDD<CSVRecord> records = csv
                .filter(f -> !f.equals(first))
                .map(line -> headerFormat.parse(new StringReader(line)).iterator().next());

        final JavaRDD<Value> converted = records.map(ValueConverter::convertToNotaQL);

        final JavaRDD<Value> filtered = converted.filter(o -> transformation.satisfiesInPredicate((ObjectValue)o));

        return evaluator.process(filtered);
    }

    /**
     * Stores the resulting rows to disk.
     *
     * @param result
     */
    @Override
    public void store(JavaRDD<ObjectValue> result) {
        final JavaSparkContext sc = NotaQL.SparkFactory.getSparkContext();

        final String[] header = getHeader(result);
        final CSVFormat format = CSVFormat.DEFAULT;

        final JavaRDD<String> headerRDD = sc.parallelize(Arrays.asList(format.format(header)));

        final JavaRDD<String> lines = result
                .map(o -> ValueConverter.convertFromNotaQL(o, header))
                .map(strings -> format.format(strings.toArray()));

        sc.union(headerRDD, lines).saveAsTextFile(path);
    }

    private String[] getHeader(JavaRDD<ObjectValue> result) {
        Set<Step<String>> set = new HashSet<>();

        final List<String> strings = result.flatMap(ObjectValue::keySet).distinct().map(Step::getStep).collect();

        return strings.toArray(new String[strings.size()]);
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
