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
grammar NotaQL2CSVIn;

import NotaQL2;

inputPath
    : firstInputPathStep                            #simpleInputPath
    | firstInputPathStep '.' Split '(' atom? ')'    #splittedInputPath
    ;

firstInputPathStep
    : '.' source=(Col | Val) ('?' '(' predicate ')')?       #cellInputPathStep
    | '.' colId                                             #colIdInputPathStep
    | '.' '$' '(' absoluteInputPath ')'                     #resolvedInputPathStep
    |                                                       #relativeCurrentCellPathStep
    ;

colId
    : colName=Name
    ;

pathToken
    : genericPathToken
    | Col
    | Val
    | Split
    ;

Col : '_c';
Val : '_v';

Split : 'split'; // FIXME: columns called split are not allowed this way!