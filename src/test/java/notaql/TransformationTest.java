package notaql;


import junit.framework.Assert;
import notaql.datamodel.*;
import notaql.engines.json.datamodel.ValueConverter;
import notaql.model.EvaluationException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by thomas on 23.05.15.
 */
@RunWith(Parameterized.class)
public class TransformationTest {
    private static final String BASE_PATH = "/test/transformations";

    private Path inputPath;
    private Path outputPath;
    private Path expectedOutputPath;
    private String transformation;


    public TransformationTest(File inputPath, File expectedOutputPath, File transformation, String name) throws IOException {
        this.inputPath = convertToRows(inputPath.toPath());
        this.expectedOutputPath = convertToRows(expectedOutputPath.toPath());

        this.outputPath = Files.createTempFile("temp_output", ".json");
        outputPath.toFile().delete();

        this.transformation =
                "IN-ENGINE: json(path <- '" + this.inputPath.toAbsolutePath().toString() + "'),\n" +
                "OUT-ENGINE: json(path <- '" + this.outputPath.toAbsolutePath().toString() + "'),\n" +
                Files.lines(transformation.toPath()).collect(Collectors.joining("\n"));
    }

    @Test
    public void executionTest() throws Exception {
        NotaQL.evaluate(transformation);

        final Set<JSONObject> expectedObjects = readRows(expectedOutputPath);
        final Set<JSONObject> actualObjects = readSplitRows(outputPath);

        assertJSONEquals(expectedObjects, actualObjects);
    }

    private static Object normalize(Object o) {
        // complex values
        if(o instanceof JSONArray) {
            final JSONArray array = (JSONArray) o;
            final Set<Object> set = new HashSet<>();
            IntStream.range(0, array.length())
                    .forEach(i -> set.add(normalize(array.get(i))));
            return set;
        }

        if(o instanceof JSONObject) {
            final JSONObject jsonObject = (JSONObject) o;
            final SortedMap<String, Object> map = new TreeMap<>();

            for (String key : jsonObject.keySet()) {
                final Object value = normalize(jsonObject.get(key));

                map.put(key, value);
            }
            return map;
        }

        return o;
    }

    public void assertJSONEquals(Set<JSONObject> expected, Set<JSONObject> actual) {
        final Set<Object> normalizedExpected = expected.stream().map(TransformationTest::normalize).collect(Collectors.toSet());
        final Set<Object> normalizedActual = actual.stream().map(TransformationTest::normalize).collect(Collectors.toSet());

        if(!normalizedExpected.equals(normalizedActual))
            fail("Objects are not equal:\nExpected:\t" + expected.toString() + "\nActual:\t\t" + actual.toString());
    }

    @Parameterized.Parameters(name= "{index}: {3}")
    public static Collection<Object[]> data() throws URISyntaxException, IOException {
        URL basePath = NotaQL.class.getResource(BASE_PATH);
        final List<File> dirs = Files
                .list(Paths.get(basePath.toURI()))
                .map(Path::toFile)
                .filter(File::isDirectory)
                .collect(Collectors.toList());

        Collection<Object[]> data = new ArrayList<Object[]>();

        for (File dir : dirs) {
            final List<File> files = Arrays.asList(dir.listFiles());

            final Optional<File> input = files.stream().filter(f -> f.getName().equals("input.json")).findAny();
            final Optional<File> output = files.stream().filter(f -> f.getName().equals("output.json")).findAny();
            final Optional<File> transformation = files.stream().filter(f -> f.getName().equals("transformation.notaql")).findAny();

            if(!input.isPresent() || !output.isPresent() || !transformation.isPresent())
                continue;

            data.add(new Object[]{input.get(), output.get(), transformation.get(), dir.getName() });
        }

        return data;
    }

    private static Set<JSONObject> readSplitRows(Path path) throws IOException {
        Set<JSONObject> objects = new HashSet<>();
        final List<Path> files = Files.list(path)
                .filter(p -> p.getFileName().toString().startsWith("part-"))
                .collect(Collectors.toList());

        for (Path file : files) {
            Files.lines(file)
                    .map(JSONObject::new)
                    .forEach(objects::add);
        }

        return objects;
    }

    private static Set<JSONObject> readRows(Path path) throws IOException {
        Set<JSONObject> objects = new HashSet<>();

        Files.lines(path)
                .map(JSONObject::new)
                .forEach(objects::add);

        return objects;
    }

    private static Set<JSONObject> readArray(Path path) throws IOException {
        final String input = Files.lines(path).collect(Collectors.joining());
        final JSONArray jsonArray = new JSONArray(input);

        final HashSet<JSONObject> elements = new HashSet<>();
        for(int i = 0; i < jsonArray.length(); i++) {
            final Object element = jsonArray.get(i);
            if(!(element instanceof JSONObject))
                throw new IOException("Expecting an array of objects as input.");
            elements.add((JSONObject)element);
        }

        return elements;
    }

    private static Path convertToRows(Path input) throws IOException {
        final Set<JSONObject> objects = readArray(input);

        final Path path = Files.createTempFile("temp_input", ".json");

        Files.write(
                path,
                objects.stream()
                        .map(JSONObject::toString)
                        .collect(Collectors.toSet())
        );

        return path;
    }
}
