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

package notaql.parser;

import notaql.datamodel.Step;
import notaql.model.AttributeSpecification;
import notaql.model.NotaQLExpression;
import notaql.model.Transformation;
import notaql.model.path.IdStep;
import notaql.model.path.InputPath;
import notaql.model.path.OutputPath;
import notaql.model.vdata.InputVData;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class NotaQLExpressionParserTest {

    @Test
    public void testParseMongoDBIdentity() throws Exception {
        final NotaQLExpression parse = NotaQLExpressionParser.parse("IN-ENGINE:mongodb(database_name <- 'test', collection_name <- 'test'), OUT-ENGINE:mongodb(database_name <- 'test', collection_name <- 'test'), OUT._id <- IN._id, OUT.$(IN.*.name()) <- IN.@");

        assertEquals(
                "Expressions don't match",
                new NotaQLExpression(
                        Arrays.asList(
                                new Transformation(
                                        null,
                                        null,
                                        parse.getTransformations().get(0).getInEngineEvaluator(),
                                        parse.getTransformations().get(0).getOutEngineEvaluator(),
                                        new AttributeSpecification(
                                                new OutputPath(new IdStep<>(new Step<>("_id"))),
                                                new InputVData(new InputPath(new IdStep<>(new Step<>("_id"))))
                                        )
                                )
                        )
                ),
                parse
        );
    }

    @Test
    public void testParseHBaseIdentity() throws Exception {
        final NotaQLExpression parse = NotaQLExpressionParser.parse("IN-ENGINE:hbase(table_id <- 'test'), OUT-ENGINE:hbase(table_id <- 'test'), OUT._r <- IN._r, OUT.$(IN._c) <- IN._v");

        assertEquals(
                "Expressions don't match",
                new NotaQLExpression(
                        Arrays.asList(
                                new Transformation(
                                        null,
                                        null,
                                        parse.getTransformations().get(0).getInEngineEvaluator(),
                                        parse.getTransformations().get(0).getOutEngineEvaluator(),
                                        new AttributeSpecification(
                                                new OutputPath(new IdStep<>(new Step<>("_id"))),
                                                new InputVData(new InputPath(new IdStep<>(new Step<>("_id"))))
                                        )
                                )
                        )
                ),
                parse
        );
    }

    @Test
    public void testParseHBasePredicate1() throws Exception {
        final NotaQLExpression parse = NotaQLExpressionParser.parse(
                "IN-ENGINE:hbase(table_id <- 'test'), OUT-ENGINE:hbase(table_id <- 'test'), OUT._r <- IN._r, OUT.$(IN._c?(@ < '5')) <- IN._v"
        );

        assertEquals(
                "Expressions don't match",
                new NotaQLExpression(
                        Arrays.asList(
                                new Transformation(
                                        null,
                                        null,
                                        parse.getTransformations().get(0).getInEngineEvaluator(),
                                        parse.getTransformations().get(0).getOutEngineEvaluator(),
                                        new AttributeSpecification(
                                                new OutputPath(new IdStep<>(new Step<>("_id"))),
                                                new InputVData(new InputPath(new IdStep<>(new Step<>("_id"))))
                                        )
                                )
                        )
                ),
                parse
        );
    }

    @Test
    public void testParseHBasePredicate2() throws Exception {
        final NotaQLExpression parse = NotaQLExpressionParser.parse(
                "IN-ENGINE:hbase(table_id <- 'test'), OUT-ENGINE:hbase(table_id <- 'test'), OUT._r <- IN._r, OUT.$(IN._c?(IN.test < '5')) <- IN._v"
        );

        assertEquals(
                "Expressions don't match",
                new NotaQLExpression(
                        Arrays.asList(
                                new Transformation(
                                        null,
                                        null,
                                        parse.getTransformations().get(0).getInEngineEvaluator(),
                                        parse.getTransformations().get(0).getOutEngineEvaluator(),
                                        new AttributeSpecification(
                                                new OutputPath(new IdStep<>(new Step<>("_id"))),
                                                new InputVData(new InputPath(new IdStep<>(new Step<>("_id"))))
                                        )
                                )
                        )
                ),
                parse
        );
    }

    @Test
    public void testParseHBasePredicate3() throws Exception {
        final NotaQLExpression parse = NotaQLExpressionParser.parse(
                "IN-ENGINE:hbase(table_id <- 'test'), OUT-ENGINE:hbase(table_id <- 'test'), OUT._r <- IN._r, OUT.$(IN._c?(IN.test < '5' && '6' > '3')) <- IN._v"
        );

        assertEquals(
                "Expressions don't match",
                new NotaQLExpression(
                        Arrays.asList(
                                new Transformation(
                                        null,
                                        null,
                                        parse.getTransformations().get(0).getInEngineEvaluator(),
                                        parse.getTransformations().get(0).getOutEngineEvaluator(),
                                        new AttributeSpecification(
                                                new OutputPath(new IdStep<>(new Step<>("_id"))),
                                                new InputVData(new InputPath(new IdStep<>(new Step<>("_id"))))
                                        )
                                )
                        )
                ),
                parse
        );
    }

    @Test
    public void testParseHBasePredicate4() throws Exception {
        final NotaQLExpression parse = NotaQLExpressionParser.parse(
                "IN-ENGINE:hbase(table_id <- 'test'), OUT-ENGINE:hbase(table_id <- 'test'), OUT._r <- IN._r, OUT.$(IN._c?(@.test < '5')) <- IN._v"
        );

        assertEquals(
                "Expressions don't match",
                new NotaQLExpression(
                        Arrays.asList(
                                new Transformation(
                                        null,
                                        null,
                                        parse.getTransformations().get(0).getInEngineEvaluator(),
                                        parse.getTransformations().get(0).getOutEngineEvaluator(),
                                        new AttributeSpecification(
                                                new OutputPath(new IdStep<>(new Step<>("_id"))),
                                                new InputVData(new InputPath(new IdStep<>(new Step<>("_id"))))
                                        )
                                )
                        )
                ),
                parse
        );
    }

    @Test
    public void testParseHBasePredicate5() throws Exception {
        final NotaQLExpression parse = NotaQLExpressionParser.parse(
                "IN-ENGINE:hbase(table_id <- 'test'), OUT-ENGINE:hbase(table_id <- 'test'), OUT._r <- IN._r, OUT.$(IN._c?(IN.bla * '5' = '5' * '4' && @ * '0.6' < '3' * @.bla)) <- IN._v"
        );

        assertEquals(
                "Expressions don't match",
                new NotaQLExpression(
                        Arrays.asList(
                                new Transformation(
                                        null,
                                        null,
                                        parse.getTransformations().get(0).getInEngineEvaluator(),
                                        parse.getTransformations().get(0).getOutEngineEvaluator(),
                                        new AttributeSpecification(
                                                new OutputPath(new IdStep<>(new Step<>("_id"))),
                                                new InputVData(new InputPath(new IdStep<>(new Step<>("_id"))))
                                        )
                                )
                        )
                ),
                parse
        );
    }
}