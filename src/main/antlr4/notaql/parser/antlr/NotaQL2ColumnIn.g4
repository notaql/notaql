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
grammar NotaQL2ColumnIn;

import NotaQL2;

inputPath
    : firstInputPathStep                            #simpleInputPath
    | firstInputPathStep '.' Split '(' atom? ')'    #splittedInputPath // TODO: splits cannot be referenced afterwards
    ;

firstInputPathStep
    : '.' Row                                                                                       #rowInputPathStep
    | '.' (colFamilyFilter=Name ':')? source=(Col | Val) ('?' '(' predicate ')')?                   #cellInputPathStep
    | '.' colId                                                                                     #colIdInputPathStep
    | '.' (colFamily=Name ':')? '$' '(' absoluteInputPath ')'                                       #resolvedInputPathStep
    |                                                                                               #relativeCurrentCellPathStep
    ;

/**
    A column identifier consists of an optional family and a column name
*/
colId
    : (colFamily=Name ':')? colName=Name
    ;

pathToken
    : genericPathToken
    | Row
    | Col
    | Val
    | Split
    ;

Row : '_r';
Col : '_c';
Val : '_v';

Split : 'split'; // FIXME: columns/families called split are not allowed this way!