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

package notaql.model;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents a complete NotaQL expression.
 *
 * @author Thomas Lottermann
 */
public class NotaQLExpression {
    private List<Transformation> transformations;

    public NotaQLExpression(List<Transformation> transformations) {
        this.transformations = transformations;
    }

    public List<Transformation> getTransformations() {
        return transformations;
    }

    @Override
    public String toString() {
        return transformations.stream().map(Transformation::toString).collect(Collectors.joining(";\n\n"));
    }
}
