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

import notaql.model.path.IdStep;
import notaql.model.path.OutputPathStep;
import notaql.model.vdata.VData;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Created by thomas on 7/18/15.
 */
public class Arguments<T extends OutputPathStep> implements Serializable {
    private static final long serialVersionUID = 3297507255859696672L;
    private List<Argument<T>> kwargs;
    private List<VData> vargs;

    public Arguments(List<Argument<T>> kwargs, List<VData> vargs) {
        this.kwargs = kwargs;
        this.vargs = vargs;
    }

    public Arguments(List<Argument<T>> kwargs) {
        this.kwargs = kwargs;
        this.vargs = new LinkedList<>();
    }

    public List<Argument<T>> getKWArgs() {
        return kwargs;
    }

    public List<VData> getVArgs() {
        return vargs;
    }

    /**
     * Provides a constant-keyword argument for with the given key or null if not found.
     * @param key
     * @return
     */
    public Argument<T> getFromConstantKWArgs(String key) {
        final Optional<Argument<T>> arg = kwargs.stream()
                .filter(
                        a -> a.getName() instanceof IdStep &&
                                ((IdStep) a.getName()).getId().getStep().equals(key)
                )
                .findAny();

        return arg.get();
    }

    @Override
    public String toString() {
        return "Arguments{" +
                "kwargs=" + kwargs +
                ", vargs=" + vargs +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Arguments arguments = (Arguments) o;

        if (kwargs != null ? !kwargs.equals(arguments.kwargs) : arguments.kwargs != null) return false;
        if (vargs != null ? !vargs.equals(arguments.vargs) : arguments.vargs != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = kwargs != null ? kwargs.hashCode() : 0;
        result = 31 * result + (vargs != null ? vargs.hashCode() : 0);
        return result;
    }
}
