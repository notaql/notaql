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

package notaql.engines.csv.path;

import notaql.model.path.InputPath;
import notaql.model.path.InputPathStep;
import notaql.parser.NotaQLErrorListener;
import notaql.parser.TransformationParser;
import notaql.parser.antlr.NotaQL2CSVInLexer;
import notaql.parser.antlr.NotaQL2CSVInParser;
import notaql.parser.path.InputPathParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.LinkedList;
import java.util.List;

/**
 * Parses CSV input paths
 */
public class CSVInputPathParser implements InputPathParser {
    private final TransformationParser transformationParser;

    public CSVInputPathParser(TransformationParser parser) {
        this.transformationParser = parser;
    }

    @Override
    public InputPath parse(String enginePath, boolean relative) {
        final NotaQL2CSVInLexer lexer = new NotaQL2CSVInLexer(new ANTLRInputStream(enginePath));
        final NotaQL2CSVInParser parser = new NotaQL2CSVInParser(new CommonTokenStream(lexer));

        parser.addErrorListener(new NotaQLErrorListener(parser));

        final NotaQL2CSVInParser.InputPathContext inputPathContext = parser.inputPath();

        final List<InputPathStep> steps = new LinkedList<>(new CSVInputPathVisitor(transformationParser, relative)
                .visit(inputPathContext)
                .getPathSteps()
        );

        return new InputPath(relative, steps);
    }
}
