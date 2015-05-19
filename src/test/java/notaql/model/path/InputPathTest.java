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

package notaql.model.path;

import notaql.evaluation.ValueEvaluationResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import notaql.model.EvaluationException;
import notaql.datamodel.*;
import notaql.datamodel.fixation.Fixation;
import notaql.datamodel.fixation.FixationStep;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InputPathTest {
    private NestedSongTestData data;

    @Before
    public void setUp() throws Exception {
        data = new NestedSongTestData();
    }

    /**
     * test constant path for collections
     *
     * @throws Exception
     */
    @Test
    public void testEvaluate_constantCollectionPath() throws Exception {
        final InputPath path = new InputPath(new IdStep<>(new Step<>(0)));

        final List<ValueEvaluationResult> e = path.evaluate(new Fixation(data.getCollection()));
        final Fixation f = new Fixation(data.getCollection(), new FixationStep<>(new Step<>(0), false));
        final List<ValueEvaluationResult> l = Arrays.asList(new ValueEvaluationResult(data.getO0(), f));
        Assert.assertEquals("Expected:\n" + l + "\ngot:\n" + e, e, l);
    }

    /**
     * test constant path for objects
     *
     * @throws Exception
     */
    @Test
    public void testEvaluate_constantObjectPath() throws Exception {
        final InputPath path = new InputPath(new IdStep<>(new Step<>(0)), new IdStep<>(new Step<>("_id")));

        final List<ValueEvaluationResult> e = path.evaluate(new Fixation(data.getCollection()));
        final Fixation f = new Fixation(
                data.getCollection(),
                new FixationStep<>(new Step<>(0), false),
                new FixationStep<>(new Step<>("_id"), false)
        );
        final List<ValueEvaluationResult> l = Arrays.asList(
                new ValueEvaluationResult(data.getO0().get(new Step<>("_id")), f)
        );
        Assert.assertEquals("Expected:\n" + l + "\ngot:\n" + e, e, l);
    }

    /**
     * test constant path for lists
     *
     * @throws Exception
     */
    @Test
    public void testEvaluate_constantListPath() throws Exception {
        final InputPath path = new InputPath(
                new IdStep<>(new Step<>(0)),
                new IdStep<>(new Step<>("list")),
                new IdStep<>(new Step<>(0))
        );

        final List<ValueEvaluationResult> e = path.evaluate(new Fixation(data.getCollection()));
        final Fixation f = new Fixation(
                data.getCollection(),
                new FixationStep<>(new Step<>(0), false),
                new FixationStep<>(new Step<>("list"), false),
                new FixationStep<>(new Step<>(0), false)
        );
        final List<ValueEvaluationResult> l = Arrays.asList(new ValueEvaluationResult(data.getList().get(0), f));
        Assert.assertEquals("Expected:\n" + l + "\ngot:\n" + e, e, l);
    }

    /**
     * test any path
     *
     * @throws Exception
     */
    @Test
    public void testEvaluate_anyPath() throws Exception {
        final InputPath path = new InputPath(new AnyBoundStep());

        final List<ValueEvaluationResult> e = path.evaluate(new Fixation(data.getCollection()));

        final Fixation f0 = new Fixation(data.getCollection(), new FixationStep<>(new Step<>(0), true));
        final Fixation f1 = new Fixation(data.getCollection(), new FixationStep<>(new Step<>(1), true));

        final List<ValueEvaluationResult> l = Arrays.asList(new ValueEvaluationResult(data.getO0(), f0), new ValueEvaluationResult(data.getO1(), f1));
        Assert.assertEquals("Expected:\n" + l + "\ngot:\n" + e, e, l);
    }

    /**
     * test double any path
     *
     * @throws Exception
     */
    @Test
    public void testEvaluate_doubleAnyPath() throws Exception {
        final InputPath path = new InputPath(new AnyBoundStep(), new AnyBoundStep());

        final List<ValueEvaluationResult> e = path.evaluate(new Fixation(data.getCollection()));

        final List<ValueEvaluationResult> l = new LinkedList<>();

        for (Value object : data.getCollection()) {
            l.addAll(
                    ((ObjectValue) object)
                            .toMap()
                            .entrySet()
                            .stream() // stream the entries of the object
                            .filter(step -> !step.getKey().getStep().equals("_id"))
                            .map(step -> new ValueEvaluationResult( // and parse a list of results from it
                                            step.getValue(),
                                            new Fixation(
                                                    data.getCollection(),
                                                    new FixationStep<>(data.getCollection().getStep(object), true),
                                                    new FixationStep<>(step.getKey(), true)
                                            )
                                    )
                            ).collect(Collectors.toList())
            );
        }

        final Stream<Value> objectStream = data.getCollection().stream();
        objectStream.flatMap(o -> ((ObjectValue) o).toMap().values().stream()).collect(Collectors.toList());

        Assert.assertEquals("Expected:\n" + l + "\ngot:\n" + e, e, l);
    }

    /**
     * test current path
     *
     * @throws Exception
     */
    @Test
    public void testEvaluate_currentPath() throws Exception {
        final InputPath path = new InputPath(new CurrentBoundStep());

        final Fixation f = new Fixation(data.getCollection(), new FixationStep<>(new Step<>(0), true));

        final List<ValueEvaluationResult> e = path.evaluate(f);

        final List<ValueEvaluationResult> l = Arrays.asList(new ValueEvaluationResult(data.getO0(), f));
        Assert.assertEquals("Expected:\n" + l + "\ngot:\n" + e, e, l);
    }

    /**
     * test double current path
     *
     * @throws Exception
     */
    @Test
    public void testEvaluate_doubleCurrentPath() throws Exception {
        final InputPath path = new InputPath(new CurrentBoundStep(), new CurrentBoundStep());

        final Fixation f = new Fixation(
                data.getCollection(),
                new FixationStep<>(new Step<>(0), true),
                new FixationStep<>(new Step<>("_id"), true)
        );

        final List<ValueEvaluationResult> e = path.evaluate(f);

        final List<ValueEvaluationResult> l = Arrays.asList(
                new ValueEvaluationResult(data.getO0().get(new Step<>("_id")), f)
        );
        Assert.assertEquals("Expected:\n" + l + "\ngot:\n" + e, e, l);
    }

    /**
     * test unbound current path
     *
     * @throws Exception
     */
    @Test(expected = EvaluationException.class)
    public void testEvaluate_unboundCurrentPath() throws Exception {
        final InputPath path = new InputPath(new CurrentBoundStep());

        final Fixation f = new Fixation(data.getCollection(), new FixationStep<>(new Step<>(0), false));

        final List<ValueEvaluationResult> e = path.evaluate(f);
    }

    /**
     * Test if splits work
     *
     * @throws Exception
     */
    @Test
    public void testEvaluate_splitMethodPath() throws Exception {
        final InputPath path = new InputPath(
                new IdStep<>(new Step<>(0)),
                new IdStep<>(new Step<>("under_the_bridge")),
                new StringSplitMethodStep(" ")
        );

        final List<ValueEvaluationResult> e = path.evaluate(new Fixation(data.getCollection()));
        final Fixation f = new Fixation(
                data.getCollection(),
                new FixationStep<>(new Step<>(0), false),
                new FixationStep<>(new Step<>("under_the_bridge"), false)
        );
        final Value value = data.getO0().get(new Step<>("under_the_bridge"));
        final List<ValueEvaluationResult> l = Arrays.asList(new ValueEvaluationResult(value, f));

        Assert.assertEquals("Expected:\n" + l + "\ngot:\n" + e, e, l);

        assert value instanceof SplitAtomValue : "The item was not split";

        final SplitAtomValue under_the_bridge = (SplitAtomValue) value;

        final List<StringValue> split = Arrays.asList("sometimes i feel like sometimes".split(" "))
                .stream()
                .map(s -> new StringValue(s, under_the_bridge))
                .collect(Collectors.toList());

        final List<StringValue> values = under_the_bridge
                .stream()
                .filter(s -> s instanceof StringValue)
                .map(s -> (StringValue)s)
                .collect(Collectors.toList());

        Assert.assertEquals("Not all splits found:\nExpected:\n" + split + "\ngot:\n" + values, values, split);
        Assert.assertEquals("Unexpected base value", under_the_bridge.getBaseValue(), new StringValue("sometimes i feel like sometimes", data.getO0()));
    }

    /**
     * Tests if the name method works
     *
     * @throws Exception
     */
    @Test
    public void testEvaluate_nameMethodStep() throws Exception {
        final InputPath path = new InputPath(
                new IdStep<>(new Step<>(0)),
                new AnyBoundStep(),
                new NamePathMethodStep()
        );

        final List<ValueEvaluationResult> e = path.evaluate(new Fixation(data.getCollection()));
        final Fixation baseFixation = new Fixation(
                data.getCollection(),
                new FixationStep<>(new Step<>(0), false)
        );

        final List<ValueEvaluationResult> expected = data.getO0().keySet()
                .stream()
                .filter(step -> !step.getStep().equals("_id"))
                .map(
                        s -> new ValueEvaluationResult(
                                new StringValue(s.getStep()),
                                new Fixation(baseFixation, new FixationStep<>(s, true))
                        )
                )
                .collect(Collectors.toList());

        Assert.assertEquals("Expected:\n" + expected + "\ngot:\n" + e, e, expected);
    }
}