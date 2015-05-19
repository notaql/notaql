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

package notaql.engines.redis.parser.path;

import notaql.model.path.OutputPath;
import notaql.model.path.OutputPathStep;
import notaql.parser.NotaQLErrorListener;
import notaql.parser.TransformationParser;
import notaql.parser.antlr.NotaQL2DocumentOutLexer;
import notaql.parser.antlr.NotaQL2DocumentOutParser;
import notaql.parser.antlr.NotaQL2KeyValueOutLexer;
import notaql.parser.antlr.NotaQL2KeyValueOutParser;
import notaql.parser.path.OutputPathParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by thomas on 07.01.15.
 */
public class RedisOutputPathParser implements OutputPathParser {
    private final TransformationParser transformationParser;

    public RedisOutputPathParser(TransformationParser parser) {
        this.transformationParser = parser;
    }

    @Override
    public OutputPath parse(String enginePath, boolean relative) {
        final NotaQL2KeyValueOutLexer lexer = new NotaQL2KeyValueOutLexer(new ANTLRInputStream(enginePath));
        final NotaQL2KeyValueOutParser parser = new NotaQL2KeyValueOutParser(new CommonTokenStream(lexer));

        parser.addErrorListener(new NotaQLErrorListener(parser));

        final NotaQL2KeyValueOutParser.OutputPathContext outputPathContext = parser.outputPath();

        return new RedisOutputPathVisitor(this.transformationParser).visit(outputPathContext);
    }
}
