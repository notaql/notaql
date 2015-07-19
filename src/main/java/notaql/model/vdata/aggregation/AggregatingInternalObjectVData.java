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

package notaql.model.vdata.aggregation;

import notaql.model.function.Argument;
import notaql.model.vdata.InternalObjectVData;

import java.util.*;

/**
 * This is used internally in the TransformationParser in order to translate multi-step paths.
 * This simply marks an object as an aggregating helper object.
 */
public class AggregatingInternalObjectVData extends InternalObjectVData implements AggregatingVData {
    private static final long serialVersionUID = 4560922144152759454L;

    public AggregatingInternalObjectVData() {
    }

    public AggregatingInternalObjectVData(List<Argument> specifications) {
        super(specifications);
    }

    public AggregatingInternalObjectVData(Argument... specifications) {
        super(specifications);
    }

    @Override
    public String toString() {
        return "AGGREGATING_" + super.toString();
    }

}