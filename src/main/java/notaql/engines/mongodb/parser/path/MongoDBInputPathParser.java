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

package notaql.engines.mongodb.parser.path;

import notaql.model.path.InputPath;
import notaql.model.path.InputPathStep;
import notaql.parser.NotaQLErrorListener;
import notaql.parser.TransformationParser;
import notaql.parser.antlr.NotaQL2DocumentInLexer;
import notaql.parser.antlr.NotaQL2DocumentInParser;
import notaql.parser.path.InputPathParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Parses mongodb input path expressions. This is the most advanced parser in the framework.
 */
public class MongoDBInputPathParser implements InputPathParser {
    private final TransformationParser transformationParser;

    public MongoDBInputPathParser(TransformationParser parser) {
        this.transformationParser = parser;
    }

    @Override
    public InputPath parse(String enginePath, boolean relative) {
        final NotaQL2DocumentInLexer lexer = new NotaQL2DocumentInLexer(new ANTLRInputStream(enginePath));
        final NotaQL2DocumentInParser parser = new NotaQL2DocumentInParser(new CommonTokenStream(lexer));

        parser.addErrorListener(new NotaQLErrorListener(parser));

        final NotaQL2DocumentInParser.InputPathContext inputPathContext = parser.inputPath();

        final List<InputPathStep> steps = inputPathContext.inputPathStep()
                .stream()
                .map(s -> new MongoDBInputPathStepVisitor(this.transformationParser).visit(s))
                .collect(Collectors.toList());
        return new InputPath(relative, steps);
    }
}
