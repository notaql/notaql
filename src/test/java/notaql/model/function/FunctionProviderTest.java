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

import notaql.datamodel.NullValue;
import notaql.datamodel.Step;
import notaql.datamodel.StringValue;
import notaql.engines.Engine;
import notaql.model.NotaQLException;
import notaql.model.path.IdStep;
import notaql.model.path.OutputPath;
import notaql.model.vdata.AtomVData;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class FunctionProviderTest {
    @Test
    public void testTrickDefault() {
        final Parameter parameter = new Parameter("a", Parameter.ArgumentType.DEFAULT);
        assert parameter.getDefaultValue() instanceof NullValue;
    }

    @Test
    public void testValidateNormalOnly() {
        final TestFunctionProvider provider = new TestFunctionProvider() {
            @Override
            public List<Parameter> getParameters() {
                return Arrays.asList(new Parameter("a"), new Parameter("b"));
            }
        };
        FunctionProvider.Resolver.validate(provider);
    }

    @Test
    public void testValidateDefaultOnly() {
        final TestFunctionProvider provider = new TestFunctionProvider() {
            @Override
            public List<Parameter> getParameters() {
                return Arrays.asList(new Parameter("a", new NullValue()), new Parameter("b", new NullValue()));
            }
        };
        FunctionProvider.Resolver.validate(provider);
    }

    @Test
    public void testValidateVarArgOnly() {
        final TestFunctionProvider provider = new TestFunctionProvider() {
            @Override
            public List<Parameter> getParameters() {
                return Arrays.asList(new Parameter("a", Parameter.ArgumentType.VAR_ARG));
            }
        };
        FunctionProvider.Resolver.validate(provider);
    }

    @Test
    public void testValidateKeywordArgOnly() {
        final TestFunctionProvider provider = new TestFunctionProvider() {
            @Override
            public List<Parameter> getParameters() {
                return Arrays.asList(new Parameter("a", Parameter.ArgumentType.KEYWORD_ARG));
            }
        };
        FunctionProvider.Resolver.validate(provider);
    }

    @Test
    public void testValidateNormalDefault() {
        final TestFunctionProvider provider = new TestFunctionProvider() {
            @Override
            public List<Parameter> getParameters() {
                return Arrays.asList(new Parameter("a"), new Parameter("b", new NullValue()));
            }
        };
        FunctionProvider.Resolver.validate(provider);
    }

    @Test
    public void testValidateNormalDefaultVarArgKeywordArg() {
        final TestFunctionProvider provider = new TestFunctionProvider() {
            @Override
            public List<Parameter> getParameters() {
                return Arrays.asList(
                        new Parameter("a"),
                        new Parameter("b", new StringValue("HELLO")),
                        new Parameter("c", Parameter.ArgumentType.VAR_ARG),
                        new Parameter("d", Parameter.ArgumentType.KEYWORD_ARG)
                );
            }
        };
        FunctionProvider.Resolver.validate(provider);
    }

    @Test(expected = NotaQLException.class)
    public void testValidateDuplicateException() {
        final TestFunctionProvider provider = new TestFunctionProvider() {
            @Override
            public List<Parameter> getParameters() {
                return Arrays.asList(new Parameter("a"), new Parameter("a"));
            }
        };
        FunctionProvider.Resolver.validate(provider);
    }

    @Test(expected = NotaQLException.class)
    public void testValidateDefaultNormalException() {
        final TestFunctionProvider provider = new TestFunctionProvider() {
            @Override
            public List<Parameter> getParameters() {
                return Arrays.asList(
                        new Parameter("b", new NullValue()),
                        new Parameter("a")
                );
            }
        };
        FunctionProvider.Resolver.validate(provider);
    }

    @Test(expected = NotaQLException.class)
    public void testValidateVarArgNormalException() {
        final TestFunctionProvider provider = new TestFunctionProvider() {
            @Override
            public List<Parameter> getParameters() {
                return Arrays.asList(
                        new Parameter("b", Parameter.ArgumentType.VAR_ARG),
                        new Parameter("a")
                );
            }
        };
        FunctionProvider.Resolver.validate(provider);
    }

    @Test(expected = NotaQLException.class)
    public void testValidateKeywordArgNormalException() {
        final TestFunctionProvider provider = new TestFunctionProvider() {
            @Override
            public List<Parameter> getParameters() {
                return Arrays.asList(
                        new Parameter("b", Parameter.ArgumentType.KEYWORD_ARG),
                        new Parameter("a")
                );
            }
        };
        FunctionProvider.Resolver.validate(provider);
    }

    @Test(expected = NotaQLException.class)
    public void testValidateVarArgDefaultsException() {
        final TestFunctionProvider provider = new TestFunctionProvider() {
            @Override
            public List<Parameter> getParameters() {
                return Arrays.asList(
                        new Parameter("b", Parameter.ArgumentType.VAR_ARG),
                        new Parameter("a", new NullValue())
                );
            }
        };
        FunctionProvider.Resolver.validate(provider);
    }

    @Test(expected = NotaQLException.class)
    public void testValidateKeywordArgDefaultException() {
        final TestFunctionProvider provider = new TestFunctionProvider() {
            @Override
            public List<Parameter> getParameters() {
                return Arrays.asList(
                        new Parameter("b", Parameter.ArgumentType.KEYWORD_ARG),
                        new Parameter("a", new NullValue())
                );
            }
        };
        FunctionProvider.Resolver.validate(provider);
    }

    @Test(expected = NotaQLException.class)
    public void testValidateKeywordArgVarArgException() {
        final TestFunctionProvider provider = new TestFunctionProvider() {
            @Override
            public List<Parameter> getParameters() {
                return Arrays.asList(
                        new Parameter("a", Parameter.ArgumentType.KEYWORD_ARG),
                        new Parameter("b", Parameter.ArgumentType.VAR_ARG)
                );
            }
        };
        FunctionProvider.Resolver.validate(provider);
    }

    @Test(expected = NotaQLException.class)
    public void testValidateDoubleVarArgException() {
        final TestFunctionProvider provider = new TestFunctionProvider() {
            @Override
            public List<Parameter> getParameters() {
                return Arrays.asList(
                        new Parameter("a", Parameter.ArgumentType.VAR_ARG),
                        new Parameter("b", Parameter.ArgumentType.VAR_ARG)
                );
            }
        };
        FunctionProvider.Resolver.validate(provider);
    }

    @Test(expected = NotaQLException.class)
    public void testValidateDoubleKeywordArgException() {
        final TestFunctionProvider provider = new TestFunctionProvider() {
            @Override
            public List<Parameter> getParameters() {
                return Arrays.asList(
                        new Parameter("a", Parameter.ArgumentType.KEYWORD_ARG),
                        new Parameter("b", Parameter.ArgumentType.KEYWORD_ARG)
                );
            }
        };
        FunctionProvider.Resolver.validate(provider);
    }

    @Test
    public void testExtractArgsNormalArgOnly() {
        final TestFunctionProvider provider = new TestFunctionProvider() {
            @Override
            public List<Parameter> getParameters() {
                return Arrays.asList(
                        new Parameter("a")
                );
            }
        };

        final Arguments args = FunctionProvider.Resolver
                .extractArgs(
                        provider,
                        Arrays.asList(
                                new Argument(new AtomVData(new NullValue()))
                        )
                );


        assertEquals(args, new Arguments(Arrays.asList(
                new Argument(new OutputPath(new IdStep<>(new Step<>("a"))), new AtomVData(new NullValue()))
        )));
    }

    @Test
    public void testExtractArgsDefaultArgOnly() {
        final TestFunctionProvider provider = new TestFunctionProvider() {
            @Override
            public List<Parameter> getParameters() {
                return Arrays.asList(
                        new Parameter("a", new StringValue("a"))
                );
            }
        };

        final Arguments args = FunctionProvider.Resolver
                .extractArgs(
                        provider,
                        Arrays.asList(
                                new Argument(new AtomVData(new NullValue()))
                        )
                );


        assertEquals(args, new Arguments(Arrays.asList(
                new Argument(new OutputPath(new IdStep<>(new Step<>("a"))), new AtomVData(new NullValue()))
        )));
    }

    @Test
    public void testExtractArgsDefaultArgUsedOnly() {
        final TestFunctionProvider provider = new TestFunctionProvider() {
            @Override
            public List<Parameter> getParameters() {
                return Arrays.asList(
                        new Parameter("a", new StringValue("a"))
                );
            }
        };

        final Arguments args = FunctionProvider.Resolver
                .extractArgs(
                        provider,
                        new LinkedList<>()
                );


        assertEquals(args, new Arguments(Arrays.asList(
                new Argument(new OutputPath(new IdStep<>(new Step<>("a"))), new AtomVData(new StringValue("a")))
        )));
    }

    public static abstract class TestFunctionProvider implements FunctionProvider {
        @Override
        public String getName() {
            return "TEST";
        }

        @Override
        public boolean isApplicable(Engine inEngine, Engine outEngine) {
            return true;
        }

        @Override
        public FunctionEvaluator getEvaluator() {
            return null;
        }

        @Override
        public FunctionReducer getReducer() {
            return null;
        }
    }
}