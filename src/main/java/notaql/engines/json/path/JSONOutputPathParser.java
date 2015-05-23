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

package notaql.engines.json.path;

import notaql.NotaQL;
import notaql.model.path.OutputPath;
import notaql.model.path.OutputPathStep;
import notaql.parser.NotaQLErrorListener;
import notaql.parser.TransformationParser;
import notaql.parser.antlr.NotaQL2DocumentOutLexer;
import notaql.parser.antlr.NotaQL2DocumentOutParser;
import notaql.parser.path.OutputPathParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Parses mongodb output paths.
 */
public class JSONOutputPathParser implements OutputPathParser {
    private final TransformationParser transformationParser;

    public JSONOutputPathParser(TransformationParser parser) {
        this.transformationParser = parser;
    }

    @Override
    public OutputPath parse(String enginePath, boolean relative) {
        final NotaQL2DocumentOutLexer lexer = new NotaQL2DocumentOutLexer(new ANTLRInputStream(enginePath));
        final NotaQL2DocumentOutParser parser = new NotaQL2DocumentOutParser(new CommonTokenStream(lexer));

        parser.addErrorListener(new NotaQLErrorListener(parser));

        final NotaQL2DocumentOutParser.OutputPathContext outputPathContext = parser.outputPath();

        final List<OutputPathStep> steps = outputPathContext.outputPathStep().stream()
                .map(s -> new JSONOutputPathStepVisitor(this.transformationParser).visit(s))
                .collect(Collectors.toList());
        return new OutputPath(steps);
    }
}
