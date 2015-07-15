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
public class Parameter<T extends Value> {
    private String name;
    private Class<T> type;
    private T defaultValue = null;
    private boolean isVarArg = false;

    public Parameter(String name,  Class<T> type) {
        this.name = name;
    }

    public Parameter(String name, Class<T> type, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public Parameter(String name, Class<T> type, boolean isVarArg) {
        this.name = name;
        this.isVarArg = isVarArg;
    }

    public String getName() {
        return name;
    }

    public Class<T> getType() {
        return type;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public boolean isVarArg() {
        return isVarArg;
    }
}
