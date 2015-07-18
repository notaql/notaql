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

import notaql.datamodel.Value;

/**
 * Represents a parameter
 */
public class Parameter {
    private String name;
    private Value defaultValue = null;
    private ArgumentType argType;

    public Parameter(String name) {
        this.name = name;
        this.argType = ArgumentType.NORMAL;
    }

    public Parameter(String name, Value defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.argType = ArgumentType.DEFAULT;
    }

    public Parameter(String name, ArgumentType argType) {
        this.name = name;
        this.argType = argType;
    }

    public String getName() {
        return name;
    }

    public Value getDefaultValue() {
        return defaultValue;
    }

    public ArgumentType getArgType() {
        return argType;
    }

    public static enum ArgumentType {
        NORMAL, DEFAULT, VAR_ARG, KEYWORD_ARG
    }
}
