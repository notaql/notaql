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

package notaql.model.vdata;

import notaql.model.function.Argument;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Represents any function (e.g. OBJECT, LIST, COL_COUNT, ...)
 */
public class GenericFunctionVData implements VData {
    private static final long serialVersionUID = 5778491600346079648L;
    private final String name;
    private final List<Argument> args;

    public GenericFunctionVData(String name, List<Argument> args) {
        this.name = name;
        this.args = args;
    }

    public String getName() {
        return name;
    }

    public List<Argument> getArgs() {
        return args;
    }

    @Override
    public String toString() {
        return name + '(' + args.stream().map(Argument::toString).collect(Collectors.joining(", ")) + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenericFunctionVData that = (GenericFunctionVData) o;

        if (args != null ? !args.equals(that.args) : that.args != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (args != null ? args.hashCode() : 0);
        return result;
    }
}
