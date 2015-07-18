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

import notaql.model.AttributeSpecification;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class ObjectVData implements ConstructorVData {
    private static final long serialVersionUID = 1326445906026136294L;
    private List<AttributeSpecification> specifications;

    private final static Logger logger = Logger.getLogger(ObjectVData.class.getName());

    public ObjectVData() {

    }

    public ObjectVData(List<AttributeSpecification> specifications) {
        this.specifications = specifications;
    }

    public ObjectVData(AttributeSpecification... specifications) {
        init(specifications);
    }

    @Override
    public void init(AttributeSpecification... specifications) {
        this.specifications = Arrays.asList(specifications);
    }

    public List<AttributeSpecification> getSpecifications() {
        return this.specifications;
    }

    @Override
    public String toString() {
        final String join = specifications
                .stream()
                .map(s -> s.getOutputPath().toString() + "<-" + s.getVData().toString())
                .collect(Collectors.joining(",\n"));
        return "OBJECT(\n" + join + "\n)";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObjectVData that = (ObjectVData) o;

        if (specifications != null ? !specifications.equals(that.specifications) : that.specifications != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return specifications != null ? specifications.hashCode() : 0;
    }
}
