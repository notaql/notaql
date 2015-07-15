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
grammar NotaQL2DocumentIn;

import NotaQL2;

inputPath: inputPathStep+;

inputPathStep
    : '[' listStep ']'      #listPathStep
    | '.' attributeStep     #attributePathStep
    | '.' pathMethod        #methodPathStep
    | '.' splitMethod       #splitPathMethod
    ;

attributeStep
    : IdToken               #idStep
    | SplitToken            #splitNameStep // This fixes the bug that some attributes may be named "split"
    | fieldId               #fieldIdStep
    | genericStep           #genericAttributeStep
    ;

listStep
    : index                 #indexListStep
    | genericStep           #genericListStep
    ;

genericStep
    : '$' '(' absoluteInputPath ')'    #resolvedGenericStep
    | '?' '(' predicate ')'            #predicateGenericStep
    | '*'                           #anyGenericStep
    | '@'                           #currentGenericStep
    ;

pathMethod
    : NameToken              #namePathMethod
    | IndexToken             #indexPathMethod
    ;

splitMethod
    : SplitToken '(' atom? ')'     #stringSplitMethod
    ;

pathToken
    : genericPathToken
    | NameToken
    | IndexToken
    | SplitToken
    | IdToken
    ;

NameToken: 'name()';
IndexToken: 'index()';
SplitToken: 'split';
IdToken: '_id';





