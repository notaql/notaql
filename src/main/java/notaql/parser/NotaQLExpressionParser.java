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

import notaql.model.NotaQLExpression;
import notaql.model.Transformation;
import notaql.parser.antlr.NotaQL2Lexer;
import notaql.parser.antlr.NotaQL2Parser;
import org.antlr.v4.runtime.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by thomas on 03.12.14.
 */
public class NotaQLExpressionParser {
    public static NotaQLExpression parse(String expression) {

        final NotaQL2Lexer lexer = new NotaQL2Lexer(new ANTLRInputStream(expression));
        final NotaQL2Parser parser = new NotaQL2Parser(new CommonTokenStream(lexer));

        parser.addErrorListener(new NotaQLErrorListener(parser));

        // evaluate all transformations
        final NotaQL2Parser.NotaqlContext notaql = parser.notaql();

        final List<NotaQL2Parser.TransformationContext> transformationContexts = notaql.transformation();

        final List<Transformation> transformations = transformationContexts
                .stream()
                .map(ctx -> new TransformationParser(ctx).parse())
                .collect(Collectors.toList());

        // join them to a complete notaql expression
        return new NotaQLExpression(transformations);
    }
}
