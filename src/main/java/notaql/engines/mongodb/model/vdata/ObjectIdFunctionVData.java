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

package notaql.engines.mongodb.model.vdata;

import notaql.model.EvaluationException;
import notaql.model.vdata.FunctionVData;
import notaql.model.vdata.VData;

/**
 * Provides a simple way to auto-generate object ids
 */
public class ObjectIdFunctionVData implements FunctionVData {
    private static final long serialVersionUID = 573537753259592567L;

    @Override
    public void init(VData... vDatas) {
        if(vDatas.length != 0)
            throw new EvaluationException("OBJECT_ID expects no argument");
    }

    @Override
    public String toString() {
        return "OBJECT_ID()";
    }
}
