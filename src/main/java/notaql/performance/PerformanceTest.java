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

package notaql.performance;

import notaql.NotaQL;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * TODO: The mongodb collections must be compacted before running performance tests!
 */
public class PerformanceTest {
    private static final String BASE_PATH = "/performanceTest/tests";
    private static final int RUNS = 5;

    public static void main(String... args) throws IOException, URISyntaxException {
        if (args.length > 0 && args[0].startsWith("--config=")) {

            final String path = args[0].substring(args[0].indexOf("=") + 1);

            NotaQL.loadConfig(path);
        }

        runTests();
    }

    public static void runTests() throws URISyntaxException, IOException {
        URI basePath = NotaQL.class.getResource(BASE_PATH).toURI();

        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        FileSystem zipfs = FileSystems.newFileSystem(basePath, env);

        final List<Path> tests = Files
                .list(Paths.get(basePath))
                .filter(p -> p.toString().endsWith(".json"))
                .sorted((a, b) -> a.toString().compareTo(b.toString()))
                .collect(Collectors.toList());

        List<String> csv = new LinkedList<>();

        for (Path test : tests) {
            final List<JSONObject> transformations = readArray(test);

            csv.add(test.getFileName().toString());

            int i = 1;

            for (JSONObject transformation : transformations) {
                final String name = transformation.getString("name");
                final String notaqlTransformation = composeTransformation(transformation);

                System.out.println("Evaluation test ("+ i++ +"/"+ transformations.size() +"): " + test.getFileName() + ": " + name);

                List<Duration> durations = new LinkedList<>();

                for(int j = 0; j < RUNS; j++) {
                    clean(transformation);

                    final Instant startTime = Instant.now();

                    NotaQL.evaluate(notaqlTransformation);

                    final Instant endTime = Instant.now();

                    final Duration duration = Duration.between(startTime, endTime);

                    durations.add(duration);

                    System.out.println("Testrun(" + j + ") " + name + " took: " + duration);
                }

                System.out.println("!!=================================================================!!");


                System.out.println(
                        "Test " + name + " took: " +
                                durations.stream().map(Duration::toString).collect(Collectors.joining(", "))
                );

                System.out.println(
                        "Test " + name + " took millis: " +
                                durations.stream().map(d -> Long.toString(d.toMillis())).collect(Collectors.joining(", "))
                );

                System.out.println(
                        "Test " + name + " took on average (millis): " +
                            durations.stream().mapToLong(Duration::toMillis).average()
                );

                System.out.println(
                        "Test " + name + " took on average (ignoring first - millis): " +
                        durations.stream().skip(1).mapToLong(Duration::toMillis).average()
                );

                System.out.println("!!=================================================================!!");

                csv.add(name);
                csv.add(durations.stream().map(d -> Long.toString(d.toMillis())).collect(Collectors.joining(",")));
            }

        }

        System.out.println(csv.stream().collect(Collectors.joining("\n")));
    }

    private static void clean(JSONObject transformation) throws IOException {
        final JSONObject outEngine = transformation.getJSONObject("OUT-ENGINE");

        final String engineName = outEngine.getString("engine");
        if(engineName.equals("csv")) {
            deleteRecursive(Paths.get(outEngine.getString("csv_path")));
        }
        if(engineName.equals("json")) {
            deleteRecursive(Paths.get(outEngine.getString("path")));
        }
    }

    private static void deleteRecursive(Path path) throws IOException {
        if(!Files.exists(path))
            return;

        Files.walk(path)
                .sorted((a, b) -> b.compareTo(a)). // reverse; files before dirs
                forEach(p -> {
            try { Files.delete(p); }
            catch(IOException e) { /* ... */ }
        });
    }

    private static String composeTransformation(JSONObject transformation) {
        final JSONObject inEngine = transformation.getJSONObject("IN-ENGINE");
        final JSONObject outEngine = transformation.getJSONObject("OUT-ENGINE");
        final String mapping = transformation.getString("transformation");

        return "IN-ENGINE: " + composeEngine(inEngine) + ",\n" + "OUT-ENGINE: " + composeEngine(outEngine) + ",\n" + mapping;
    }

    private static String composeEngine(JSONObject engine) {
        final StringBuilder builder = new StringBuilder();
        builder.append(engine.get("engine"));
        builder.append("(");

        final String params = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(engine.keys(), Spliterator.ORDERED),
                false
        )
                .filter(k -> !k.equals("engine"))
                .map(k -> k + " <- " + toArg(k, engine))
                .collect(Collectors.joining(", "));

        builder.append(params);
        builder.append(")");

        return builder.toString();
    }

    private static String toArg(String key, JSONObject engine) {
        final Object val = engine.get(key);
        if(val instanceof Boolean || val instanceof Number)
            return val.toString();

        return "'" + val.toString() + "'";
    }

    private static List<JSONObject> readArray(Path path) throws IOException {
        final String input = Files.lines(path).collect(Collectors.joining());
        final JSONArray jsonArray = new JSONArray(input);

        final List<JSONObject> elements = new LinkedList<>();
        for(int i = 0; i < jsonArray.length(); i++) {
            final Object element = jsonArray.get(i);
            if(!(element instanceof JSONObject))
                throw new IOException("Expecting an array of objects as input.");
            elements.add((JSONObject)element);
        }

        return elements;
    }
}
