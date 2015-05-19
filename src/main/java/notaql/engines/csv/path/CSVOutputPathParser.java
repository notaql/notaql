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

import notaql.model.path.OutputPath;
import notaql.parser.NotaQLErrorListener;
import notaql.parser.TransformationParser;
import notaql.parser.antlr.NotaQL2CSVOutLexer;
import notaql.parser.antlr.NotaQL2CSVOutParser;
import notaql.parser.antlr.NotaQL2ColumnOutLexer;
import notaql.parser.antlr.NotaQL2ColumnOutParser;
import notaql.parser.path.OutputPathParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * Parses CSV output paths
 */
public class CSVOutputPathParser implements OutputPathParser {
    private final TransformationParser transformationParser;

    public CSVOutputPathParser(TransformationParser parser) {
        this.transformationParser = parser;
    }

    @Override
    public OutputPath parse(String enginePath, boolean relative) {
        final NotaQL2CSVOutLexer lexer = new NotaQL2CSVOutLexer(new ANTLRInputStream(enginePath));
        final NotaQL2CSVOutParser parser = new NotaQL2CSVOutParser(new CommonTokenStream(lexer));

        parser.addErrorListener(new NotaQLErrorListener(parser));

        final NotaQL2CSVOutParser.OutputPathContext outputPathContext = parser.outputPath();

        return new CSVOutputPathVisitor(this.transformationParser, relative).visit(outputPathContext);
    }
}
