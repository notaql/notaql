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
import notaql.datamodel.StringValue;
import notaql.engines.Engine;
import notaql.evaluation.Evaluator;
import notaql.evaluation.Reducer;
import notaql.model.NotaQLException;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class ComplexFunctionProviderTest {
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
        ComplexFunctionProvider.Validator.validate(provider);
    }

    @Test
    public void testValidateDefaultOnly() {
        final TestFunctionProvider provider = new TestFunctionProvider() {
            @Override
            public List<Parameter> getParameters() {
                return Arrays.asList(new Parameter("a", new NullValue()), new Parameter("b", new NullValue()));
            }
        };
        ComplexFunctionProvider.Validator.validate(provider);
    }

    @Test
    public void testValidateVarArgOnly() {
        final TestFunctionProvider provider = new TestFunctionProvider() {
            @Override
            public List<Parameter> getParameters() {
                return Arrays.asList(new Parameter("a", Parameter.ArgumentType.VAR_ARG));
            }
        };
        ComplexFunctionProvider.Validator.validate(provider);
    }

    @Test
    public void testValidateKeywordArgOnly() {
        final TestFunctionProvider provider = new TestFunctionProvider() {
            @Override
            public List<Parameter> getParameters() {
                return Arrays.asList(new Parameter("a", Parameter.ArgumentType.KEYWORD_ARG));
            }
        };
        ComplexFunctionProvider.Validator.validate(provider);
    }

    @Test
    public void testValidateNormalDefault() {
        final TestFunctionProvider provider = new TestFunctionProvider() {
            @Override
            public List<Parameter> getParameters() {
                return Arrays.asList(new Parameter("a"), new Parameter("b", new NullValue()));
            }
        };
        ComplexFunctionProvider.Validator.validate(provider);
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
        ComplexFunctionProvider.Validator.validate(provider);
    }

    @Test(expected = NotaQLException.class)
    public void testValidateDuplicateException() {
        final TestFunctionProvider provider = new TestFunctionProvider() {
            @Override
            public List<Parameter> getParameters() {
                return Arrays.asList(new Parameter("a"), new Parameter("a"));
            }
        };
        ComplexFunctionProvider.Validator.validate(provider);
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
        ComplexFunctionProvider.Validator.validate(provider);
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
        ComplexFunctionProvider.Validator.validate(provider);
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
        ComplexFunctionProvider.Validator.validate(provider);
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
        ComplexFunctionProvider.Validator.validate(provider);
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
        ComplexFunctionProvider.Validator.validate(provider);
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
        ComplexFunctionProvider.Validator.validate(provider);
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
        ComplexFunctionProvider.Validator.validate(provider);
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
        ComplexFunctionProvider.Validator.validate(provider);
    }

    public static abstract class TestFunctionProvider implements ComplexFunctionProvider {
        @Override
        public String getName() {
            return "TEST";
        }

        @Override
        public boolean isApplicable(Engine inEngine, Engine outEngine) {
            return true;
        }

        @Override
        public Evaluator getEvaluator() {
            return null;
        }

        @Override
        public Reducer getReducer() {
            return null;
        }
    }
}