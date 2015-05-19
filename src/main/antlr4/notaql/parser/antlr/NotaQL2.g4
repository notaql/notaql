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
grammar NotaQL2;

import NotaQL2LexerRules;

/**
The complete notaql script consists of multiple transformations
*/
notaql: (transformation  ';')* transformation ';'? EOF;


/**
A transformation consists of input and output engine,
optional input and output filters, and some attribute specifications
*/
transformation: inEngine ','
                outEngine ','
                (inPredicate ',')?
                (outPredicate ',')?
                attributeSpecification (',' attributeSpecification)*;

/**
Input and output engines are composed by the key word IN-ENGINE/OUT-ENGINE followed by a keyword attribute list
*/
inEngine: 'IN-ENGINE:' engine;
outEngine: 'OUT-ENGINE:' engine;

engine: engineName=Name '(' (Name '<-' atom (',' Name '<-' atom)?)? ')';

/**
An attribute specification consistes of an output path and an arbitrary vData expression
*/
attributeSpecification
    : genericOutputPath ARROW vData
    ;

/**
Output paths may be absolute (i.e. starting with IN) or absolute (according to the current nesting level)
*/
genericOutputPath
    : relativeOutputPath    #relativeGenericOutputPath
    | absoluteOutputPath    #absoluteGenericOutputPath
    ;


absoluteOutputPath: OUT '.' path;
relativeOutputPath: path;

/**
An input path may be absolute (i.e. starting wit IN) or relative (i.e. starting with @).
Relative input paths are used in "?()" expressions
*/
absoluteInputPath: IN path;
relativeInputPath: AT path;

/**
In order to define paths it is necessary to provide the set of allowed tokens.
This seems a little hacky and limiting. We did not find a better way though.

Paths are then later parsed by a engine specific parser.
*/
path: pathToken*?;

pathToken: genericPathToken;

genericPathToken
    : OPEN path CLOSE
    | ARRAYOPEN path ARRAYCLOSE
    | RESOLVE
    | PREDICATE
    | STAR
    | AT
    | String
    | Int
    | Float
    | Index
    | Name
    | DOT
    | COLON
    | IN
    | EQ
    | NEQ
    | LT
    | LTEQ
    | GT
    | GTEQ
    | PLUS
    | MINUS
    | DIV
    | AND
    | OR
    ;

/**
vData makes complex expressions possible. They allow an arbitrary nesting level.
TODO: make this only have one class of functions. The keywords don't need to be part of the grammar.
*/
vData
    : atom                          #atomVData
    | '(' vData ')'                 #bracedVData
    | vData op=(STAR|DIV) vData     #multiplicatedVData
    | vData op=(PLUS|MINUS) vData   #addedVData
    | aggregationFunction           #aggregateVData
    | constructorFunction           #constructorVData
    | genericFunction               #genericFunctionVData
    | absoluteInputPath             #absoluteInputPathVData
    | relativeInputPath             #relativeInputPathVData
    ;

aggregationFunction
    : function=(SUM | AVG | MIN | MAX | COUNT | IMPLODE | LIST) '(' vData? (',' atom)* ')'
    ;

constructorFunction
    : OBJECT '(' attributeSpecification (',' attributeSpecification)* ')'   #objectConstructorFunction
    | Name '(' attributeSpecification (',' attributeSpecification)* ')'     #genericConstructorFunction
    ;

genericFunction: Name '(' (vData (',' vData)*)? ')';

standalonePredicate
    : predicate EOF
    ;

/**
Predicates let the user define simple boolean expressions depending on arbitrarily complex vData.
*/
predicate
    : '(' predicate ')'                                                     #bracedPredicate
    | '!' predicate                                                         #negatedPredicate
    | predicate AND predicate                                               #andPredicate
    | predicate OR predicate                                                #orPredicate
    | vData op=(LT | LTEQ | GT | GTEQ | EQ | NEQ) vData                     #relationalPredicate
    | absoluteInputPath                                                     #absolutePathExistencePredicate
    | relativeInputPath                                                     #relativePathExistencePredicate
    ;

inPredicate
    : 'IN-FILTER' ':' predicate
    ;

outPredicate
    : 'OUT-FILTER' ':' predicate
    ;



/**
    Represents an atomic value (Int, Float or String) all wrapped in apostrophes
    TODO: we might even remove the quotes around numbers at some point
*/
atom
    : (Int | Float)     #numberAtom
    | String            #stringAtom
    ;

attributeId: attributeName = Name;

index: indexNumber = Index;