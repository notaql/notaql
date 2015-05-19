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

import notaql.datamodel.*;
import notaql.datamodel.fixation.Fixation;
import notaql.datamodel.fixation.FixationStep;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OutputPathTest {
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
        final OutputPath path = new OutputPath(new IdStep<>(new Step<>(0)));

        final List<StepNameEvaluationResult> evaluate = path.evaluate(new Fixation(data.getCollection()));

        final Fixation f = new Fixation(data.getCollection());
        final List<StepNameEvaluationResult> l = Arrays.asList(new StepNameEvaluationResult(f, Arrays.asList(new Step<>(0))));
        Assert.assertEquals(l, evaluate);
    }

    /**
     * test resolved path for collections
     *
     * @throws Exception
     */
    @Test
    public void testEvaluate_resolvedCollectionPath() throws Exception {
        final OutputPath path = new OutputPath(
                new IdStep<>(new Step<>(0)),
                new ResolvedIdStep(
                        new InputPath(
                                new IdStep<>(new Step<>(0)),
                                new AnyBoundStep(),
                                new NamePathMethodStep()
                        )
                )

        );

        final List<StepNameEvaluationResult> evaluate = path.evaluate(new Fixation(data.getCollection()));

        final List<StepNameEvaluationResult> expected = data.getO0()
                .keySet()
                .stream()
                .map(s ->
                                new StepNameEvaluationResult(
                                        new Fixation(
                                                data.getCollection(),
                                                new FixationStep<>(new Step<>(0), false),
                                                new FixationStep<>(new Step<>(s.getStep()), true)
                                        ),
                                        new Step<>(0),
                                        new Step<>(s.getStep())
                                )
                )
                .collect(Collectors.toList());

    }

}