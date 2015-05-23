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
lexer grammar NotaQL2LexerRules;

/**
    Skip comments
*/
SL_COMMENT: '#' .*? '\n' -> skip;
OPTWS: (' ' | '\t') -> skip;
OPTNL: '\r'? '\n' -> skip;

IN: 'IN';
OUT: 'OUT';

ARROW: '<-';

EQ: '=';
NEQ: '!=';
LT: '<';
LTEQ: '<=';
GT: '>';
GTEQ: '>=';

AND: '&&';
OR: '||';

PLUS: '+';
MINUS: '-';
DIV: '/';

Int: [0-9]+;
Float: [0-9]+ '.' [0-9]+;

String: '\'' ~('\r' | '\n' | '\'')* '\'';

AVG: 'AVG';
COUNT: 'COUNT';
MAX: 'MAX';
MIN: 'MIN';
SUM: 'SUM';
IMPLODE: 'IMPLODE';

LIST: 'LIST';
OBJECT: 'OBJECT';

Name: [a-zA-Z0-9_]+;

OPEN: '(';
CLOSE: ')';

ARRAYOPEN: '[';
ARRAYCLOSE: ']';

RESOLVE: '$';
PREDICATE: '?';

STAR: '*';
AT: '@';

DOT: '.';
COLON: ':';