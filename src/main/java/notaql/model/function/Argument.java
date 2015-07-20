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
import notaql.model.path.OutputPathStep;
import notaql.model.vdata.VData;

import java.io.Serializable;

/**
 * Represents an instance of a parameter
 */
public class Argument<T extends OutputPathStep> implements Serializable {
    private static final long serialVersionUID = -4613254486825466251L;
    private OutputPathStep name = null;
    private VData vData;

    public Argument(VData vData) {
        this.vData = vData;
    }

    public Argument(OutputPathStep name, VData vData) {
        this.name = name;
        this.vData = vData;
    }

    public Argument(String name, VData vData) {
        this.name = new IdStep<>(new Step<>(name));
        this.vData = vData;
    }

    public OutputPathStep getName() {
        return name;
    }

    public VData getVData() {
        return vData;
    }

    @Override
    public String toString() {
        return name + " <- " + vData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Argument argument = (Argument) o;

        if (name != null ? !name.equals(argument.name) : argument.name != null) return false;
        if (vData != null ? !vData.equals(argument.vData) : argument.vData != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (vData != null ? vData.hashCode() : 0);
        return result;
    }
}
