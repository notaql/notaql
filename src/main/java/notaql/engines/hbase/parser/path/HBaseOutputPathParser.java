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

package notaql.engines.hbase.parser.path;

import notaql.model.path.OutputPath;
import notaql.parser.NotaQLErrorListener;
import notaql.parser.TransformationParser;
import notaql.parser.antlr.NotaQL2ColumnOutLexer;
import notaql.parser.antlr.NotaQL2ColumnOutParser;
import notaql.parser.path.OutputPathParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * Parses any HBase specific output path. See grammar for more details
 */
public class HBaseOutputPathParser implements OutputPathParser {
    private final TransformationParser transformationParser;

    public HBaseOutputPathParser(TransformationParser parser) {
        this.transformationParser = parser;
    }

    @Override
    public OutputPath parse(String enginePath, boolean relative) {
        final NotaQL2ColumnOutLexer lexer = new NotaQL2ColumnOutLexer(new ANTLRInputStream(enginePath));
        final NotaQL2ColumnOutParser parser = new NotaQL2ColumnOutParser(new CommonTokenStream(lexer));

        parser.addErrorListener(new NotaQLErrorListener(parser));

        final NotaQL2ColumnOutParser.OutputPathContext outputPathContext = parser.outputPath();

        return new HBaseOutputPathVisitor(this.transformationParser, relative).visit(outputPathContext);
    }
}
