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
grammar NotaQL2KeyValueIn;

import NotaQL2;

// TODO: relative input path "@" not yet supported
// TODO: split() missing

inputPath
    : '.' Key                                       #keyInputPath
    | '.' Val (complexTypeStep)? (splitStep)?       #valueInputPath
    ;

splitStep: '.' Split '(' atom? ')';

complexTypeStep
    : '[' listStep ']' ('.' listMethod)? #listPathStep
    | '.' hashStep ('.' hashMethod)?     #hashPathStep
    ;

hashStep
    : keyId                             #keyIdHashStep
    | genericComplexStep                #genericComplexHashStep
    ;

listStep
    : index                             #indexListStep
    | genericComplexStep                #genericComplexListStep
    ;

genericComplexStep
    : '$' '(' absoluteInputPath ')'        #resolvedGenericComplexStep
    | '*'                               #anyGenericComplexStep
    | '@'                               #currentGenericComplexStep
    | '?' '(' predicate ')'                #predicateGenericComplexStep
    ;

hashMethod
    : NameToken                          #nameHashMethod
    ;

listMethod
    : IndexToken                         #indexListMethod
    ;

keyId: Name (':' Name)*;

pathToken
    : genericPathToken
    | NameToken
    | IndexToken
    | Key
    | Val
    | Split
    ;

NameToken: 'name()';
IndexToken: 'index()';
Key : '_k';
Val : '_v';

Split : 'split'; // FIXME: columns/families called split are not allowed this way!