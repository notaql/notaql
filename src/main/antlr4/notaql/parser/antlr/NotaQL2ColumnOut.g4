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
grammar NotaQL2ColumnOut;

import NotaQL2;


outputPath
    : Row                                                       #rowOutputPath
    | colId                                                     #colIdOutputPath
    | (colFamily=Name ':')? '$' '(' absoluteInputPath ')'          #resolvedOutputPath
    | '$' '(' family=absoluteInputPath ')' ':' '$' '(' column=absoluteInputPath ')' #resolvedOutputPathFamily
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
    ;

Row : '_r';