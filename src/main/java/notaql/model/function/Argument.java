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

import notaql.datamodel.Step;
import notaql.model.path.IdStep;
import notaql.model.path.OutputPath;
import notaql.model.vdata.VData;

/**
 * Represents an instance of a parameter
 */
public class Argument {
    private OutputPath path = null;
    private VData vData;

    public Argument(VData vData) {
        this.vData = vData;
    }

    public Argument(OutputPath path, VData vData) {
        this.path = path;
        this.vData = vData;
    }

    public Argument(String name, VData vData) {
        this.path = new OutputPath(new IdStep<>(new Step<>(name)));
        this.vData = vData;
    }

    public OutputPath getPath() {
        return path;
    }

    public VData getVData() {
        return vData;
    }

    @Override
    public String toString() {
        return path + " <- " + vData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Argument argument = (Argument) o;

        if (path != null ? !path.equals(argument.path) : argument.path != null) return false;
        if (vData != null ? !vData.equals(argument.vData) : argument.vData != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (vData != null ? vData.hashCode() : 0);
        return result;
    }
}
