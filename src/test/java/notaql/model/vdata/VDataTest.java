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

package notaql.model.vdata;

import notaql.datamodel.*;
import notaql.datamodel.fixation.Fixation;
import notaql.model.AttributeSpecification;
import notaql.model.function.Argument;
import notaql.model.path.*;
import notaql.model.vdata.aggregation.CountVData;
import notaql.model.vdata.aggregation.ListVData;
import notaql.model.vdata.aggregation.SumVData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class VDataTest {
    private SongTestData songData;
    private ArithmeticTestData arithmeticData;

    @Before
    public void setUp() {
        songData = new SongTestData();
        arithmeticData = new ArithmeticTestData();
    }

    /**
     * Equivalent to the following query
     * <p>
     * OUT._id <- IN._id,
     * OUT.$(IN.*.name()) <- IN.@;
     *
     * @throws Exception
     */
    @Test
    public void testEvaluate_identity() throws Exception {
        final InternalObjectVData object = new InternalObjectVData(
                new Argument(
                        new OutputPath(
                                new IdStep<>(new Step<>("_id"))
                        ),
                        new InputVData(new InputPath(
                                new AnyBoundStep(),
                                new IdStep<>(new Step<>("_id"))
                        ))
                ),
                new Argument(
                        new OutputPath(
                                new ResolvedIdStep(
                                        new InputPath(
                                                new CurrentBoundStep(),
                                                new AnyBoundStep(),
                                                new NamePathMethodStep()
                                        )
                                )
                        ),
                        new InputVData(new InputPath(
                                new CurrentBoundStep(),
                                new CurrentBoundStep()
                        ))
                )
        );

        final ListVData identity = new ListVData(object);

    }

    /**
     * Equivalent to the following query
     * <p>
     * OUT._id <- IN._id,
     * OUT.terms <- LIST (
     * OBJECT (
     * term <- IN.*.split()[*],
     * songs <- LIST (
     * OBJECT (
     * song <- IN.@.name(),
     * number <- COUNT()
     * )
     * ),
     * number <- COUNT()
     * )
     * )
     *
     * @throws Exception
     */
    @Test
    public void testEvaluate_nestedGrouping() throws Exception {
        final InternalObjectVData object = new InternalObjectVData(
                new Argument(
                        new OutputPath(
                                new IdStep<>(new Step<>("_id"))
                        ),
                        new AtomVData(new StringValue("all"))
                ),
                new Argument(
                        new OutputPath(
                                new IdStep<>(new Step<>("terms"))
                        ),
                        new ListVData(
                                new InternalObjectVData(
                                        new Argument(
                                                new OutputPath(
                                                        new IdStep<>(new Step<>("term"))
                                                ),
                                                new InputVData(
                                                        new InputPath(
                                                                new AnyBoundStep(),
                                                                new AnyBoundStep(),
                                                                new StringSplitMethodStep(" "),
                                                                new AnyBoundStep()
                                                                )
                                                )
                                        ),
                                        new Argument(
                                                new OutputPath(
                                                        new IdStep<>(new Step<>("count"))
                                                ),
                                                new CountVData()
                                        ),
                                        new Argument(
                                                new OutputPath(
                                                        new IdStep<>(new Step<>("songs"))
                                                ),
                                                new ListVData(
                                                        new InternalObjectVData(
                                                                new Argument(
                                                                        new OutputPath(
                                                                                new IdStep<>(new Step<>("song"))
                                                                        ),
                                                                        new InputVData(
                                                                                new InputPath(
                                                                                        new CurrentBoundStep(),
                                                                                        new CurrentBoundStep(),
                                                                                        new NamePathMethodStep()
                                                                                )
                                                                        )
                                                                ),
                                                                new Argument(
                                                                        new OutputPath(
                                                                                new IdStep<>(new Step<>("band"))
                                                                        ),
                                                                        new InputVData(
                                                                                new InputPath(
                                                                                        new CurrentBoundStep(),
                                                                                        new IdStep<>(new Step<>("_id"))
                                                                                )
                                                                        )
                                                                ),
                                                                new Argument(
                                                                        new OutputPath(
                                                                                new IdStep<>(new Step<>("count"))
                                                                        ),
                                                                        new CountVData()
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );

        final ListVData identity = new ListVData(object);

    }

    /**
     * Equivalent to the following query
     * <p>
     * OUT._id <- IN._id,
     * OUT.weightedSum <- IN.weight * SUM(IN.values[*]);
     *
     * @throws Exception
     */
    @Test
    public void testEvaluate_nestedArithmetics() throws Exception {
        final InternalObjectVData object = new InternalObjectVData(
                new Argument(
                        new OutputPath(
                                new IdStep<>(new Step<>("_id"))
                        ),
                        new InputVData(new InputPath(
                                new AnyBoundStep(),
                                new IdStep<>(new Step<>("_id"))
                        ))
                ),
                new Argument(
                        new OutputPath(
                                new IdStep<>(new Step<>("weightedSum"))
                        ),
                        new ArithmeticVData(
                                new InputVData(new InputPath(
                                        new CurrentBoundStep(),
                                        new IdStep<>(new Step<>("weight"))
                                )),
                                new SumVData(
                                        new InputVData(new InputPath(
                                                new CurrentBoundStep(),
                                                new IdStep<>(new Step<>("values")),
                                                new AnyBoundStep()
                                        ))
                                ),
                                ArithmeticVData.Operation.MULTIPLY
                        )
                )
        );

        final ListVData identity = new ListVData(object);

    }
}