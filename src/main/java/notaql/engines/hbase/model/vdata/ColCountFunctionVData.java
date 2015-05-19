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

package notaql.engines.hbase.model.vdata;

import notaql.model.EvaluationException;
import notaql.model.vdata.FunctionVData;
import notaql.model.vdata.VData;

/**
 * Provides the number of columns in a row - or for a certain column family in a row
 */
public class ColCountFunctionVData implements FunctionVData {
    private static final long serialVersionUID = 7550027097513630199L;
    private VData colFamilyVData;

    public VData getExpression() {
        return colFamilyVData;
    }

    @Override
    public void init(VData... vDatas) {
        if(vDatas.length > 1)
            throw new EvaluationException("COL_COUNT expects at most one parameter which may specify the column family");

        if(vDatas.length == 1)
            this.colFamilyVData = vDatas[0];
        else
            this.colFamilyVData = null;
    }

    @Override
    public String toString() {
        return "COL_COUNT(" + (colFamilyVData!=null ? colFamilyVData.toString() : "") + ")";
    }
}
