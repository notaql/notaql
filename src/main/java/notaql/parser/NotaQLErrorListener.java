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

import notaql.parser.antlr.NotaQL2Parser;
import org.antlr.v4.runtime.*;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.Collections;
import java.util.List;

/**
 * This class handles basic error output and stops the parser
 */
public class NotaQLErrorListener extends BaseErrorListener {
    private Parser parser;

    public NotaQLErrorListener(Parser parser) {
        this.parser = parser;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        String token = "UNKNOWN";
        if(offendingSymbol instanceof CommonToken) {
            try {
                token = parser.getTokenNames()[((CommonToken) offendingSymbol).getType()];
            } catch (ArrayIndexOutOfBoundsException ignored) {

            }
        }

        final List<String> stack = ((Parser) recognizer).getRuleInvocationStack();
        Collections.reverse(stack);
        System.err.println("rule stack: " + stack);
        printContextStack(((Parser) recognizer).getContext());

        throw new IllegalStateException(parser.getClass().getName() + " failed to parse at line " + line + " at character " + charPositionInLine + " due to " + msg + "; Offending symbol: " + offendingSymbol.getClass().toString() + ": " + token, e);
    }

    public static void printContextStack(ParserRuleContext ctx) {
        if(ctx.getParent() != null)
            printContextStack(ctx.getParent());
        System.err.println("context: " + ctx.getText() + " (" + ctx.getClass().toString() + ")");
    }
}
