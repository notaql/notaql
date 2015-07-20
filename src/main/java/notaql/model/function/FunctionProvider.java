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

package notaql.model.function;

import notaql.engines.Engine;
import notaql.model.NotaQLException;
import notaql.model.path.OutputPathStep;
import notaql.model.vdata.VData;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

/**
 * Provides complex functions which may deeply influence the evaluation process.
 */
public interface FunctionProvider<T extends OutputPathStep> extends Serializable {
    /**
     * Provides the function name (used in the NotaQL transformations)
     * @return
     */
    public String getName();

    /**
     * Provides the parameters that the function accepts.
     * Every usage of this function is checked for correctness in terms of parameters (i.e. number of parameters and order - this is in Python style)
     * @return
     */
    public List<Parameter> getParameters();

    /**
     * Tells if the function can be used with the given in and out engines
     * @param inEngine
     * @param outEngine
     * @return
     */
    public boolean isApplicable(Engine inEngine, Engine outEngine);

    /**
     * Provides the evaluator for this function
     * @return
     */
    public FunctionEvaluator<T> getEvaluator();

    /**
     * Provides the reducer for this function
     * @return
     */
    public FunctionReducer<T> getReducer();

    public static class Resolver {
        /**
         * Checks that the following properties:
         * - Values with default values come after the ones without
         * - Varargs and Keyword arguments are come last (in this order)
         * - No duplicate names
         * This is modelled after the python style of function parameters.
         * @param provider
         */
        public static void validate(FunctionProvider provider) {
            final String name = provider.getName();
            final List<Parameter> parameters = provider.getParameters();

            // make sure that there are no duplicate argument names
            final Stream<String> distinct = parameters.stream().map(Parameter::getName).distinct();

            if(distinct.count() < parameters.size())
                throw new NotaQLException(
                        String.format("The function '%1$s' has duplicate arguments.", name)
                );

            boolean foundDefault = false;
            boolean foundVArgs = false;
            boolean foundKWArgs = false;

            for (Parameter parameter : parameters) {
                final Parameter.ArgumentType type = parameter.getArgType();

                if (foundKWArgs)
                    throw new NotaQLException(
                            String.format("The function '%1$s' has key word arguments that do not come last. It is followed by an argument of type %2$s", name, type)
                    );

                if (type == Parameter.ArgumentType.KEYWORD_ARG) {
                    foundKWArgs = true;
                    continue;
                }

                // type != Parameter.ArgumentType.KEYWORD_ARG;

                if (foundVArgs) {
                    throw new NotaQLException(
                            String.format("The function '%1$s' has varargs that do not come last or immediately before the key word arguments. It is followed by an argument of type %2$s", name, type)
                    );
                }

                if (type == Parameter.ArgumentType.VAR_ARG) {
                    foundVArgs = true;
                    continue;
                }

                // type != Parameter.ArgumentType.KEYWORD_ARG && type != Parameter.ArgumentType.VAR_ARG;

                if (type == Parameter.ArgumentType.DEFAULT) {
                    foundDefault = true;
                    continue;
                }

                // type != Parameter.ArgumentType.KEYWORD_ARG && type != Parameter.ArgumentType.VAR_ARG && type != Parameter.ArgumentType.DEFAULT;
                // => NORMAL

                if (foundDefault) {
                    throw new NotaQLException(
                            String.format("The function '%1$s' has non-default argument following default argument.", name)
                    );
                }
            }
        }

        /**
         * Extracts the arguments for a given function from a given list of plain arguments
         * @param function
         * @param plainArgs
         * @return
         */
        public static Arguments extractArgs(FunctionProvider function, List<Argument> plainArgs) {
            final List<Argument> args = new LinkedList<>();
            final List<VData> vargs = new LinkedList<>();

            final List<Parameter> params = function.getParameters();

            // first check that there are no non-named arguments after the first named one
            boolean named = false;
            for (Argument plainArg : plainArgs) {
                if(named && plainArg.getPath() == null)
                    throw new NotaQLException(
                            String.format(
                                    "The function '%1$s' has non-named attributes following named attributes.",
                                    function.getName()
                            )
                    );

                if(plainArg.getPath() != null)
                    named = true;
            }

            // extract all non-named arguments first
            final Iterator<Argument> argIter = plainArgs.iterator();
            final Iterator<Parameter> paramIter = params.iterator();

            Parameter.ArgumentType stage = Parameter.ArgumentType.NORMAL;
            Parameter curParam = null;
            Argument curArg = null;

            argLoop:
            while(argIter.hasNext() && (paramIter.hasNext() || stage == Parameter.ArgumentType.VAR_ARG)) {
                curArg = argIter.next();

                if(curArg.getPath() != null)
                    break;

                if(stage == Parameter.ArgumentType.NORMAL || stage == Parameter.ArgumentType.DEFAULT) {
                    curParam = paramIter.next();
                    stage = curParam.getArgType();
                }

                switch (stage) {
                    case NORMAL:
                        args.add(new Argument(curParam.getName(), curArg.getVData()));
                        break;
                    case DEFAULT:
                        args.add(new Argument(curParam.getName(), curArg.getVData()));
                        break;
                    case VAR_ARG:
                        vargs.add(curArg.getVData());
                        break;
                    case KEYWORD_ARG:
                        break argLoop;
                }
            }

            // in case there are still non-named attributes left: there are too many
            // e.g. params: a, b; args: "a", "b", "c"
            if(argIter.hasNext() && !paramIter.hasNext())
                throw new NotaQLException(
                        String.format(
                                "The function '%1$s' was provided with too many non-named attributes.",
                                function.getName()
                        )
                );

            // extract the named arguments
            for (Argument arg : plainArgs) {
                // skip non-named args
                if(arg.getPath() == null)
                    continue;

                final VData put;
                args.add(new Argument(arg.getPath(), arg.getVData()));
            }

            return new Arguments(args, vargs);
        }
    }
}
