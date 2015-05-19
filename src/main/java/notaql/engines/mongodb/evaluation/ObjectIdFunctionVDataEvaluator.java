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

package notaql.engines.mongodb.evaluation;

import notaql.datamodel.*;
import notaql.datamodel.fixation.Fixation;
import notaql.engines.mongodb.datamodel.ObjectIdValue;
import notaql.engines.mongodb.model.vdata.ObjectIdFunctionVData;
import notaql.evaluation.Evaluator;
import notaql.evaluation.Reducer;
import notaql.evaluation.ValueEvaluationResult;
import notaql.model.vdata.VData;
import notaql.evaluation.values.UnresolvedValue;
import org.bson.types.ObjectId;

import java.util.Arrays;
import java.util.List;

/**
 * Created by thomas on 19.02.15.
 */
public class ObjectIdFunctionVDataEvaluator implements Evaluator, Reducer {
    private static final long serialVersionUID = -2787475906497490293L;

    @Override
    public List<ValueEvaluationResult> evaluate(VData vData, Fixation fixation) {
        assert vData instanceof ObjectIdFunctionVData;

        final ObjectIdFunctionVData functionVData = (ObjectIdFunctionVData) vData;

        return Arrays.asList(new ValueEvaluationResult(new PartialObjectIdValue(), fixation));
    }

    @Override
    public boolean canReduce(VData vData) {
        return true;
    }

    @Override
    public List<Class<? extends VData>> getProcessedClasses() {
        return Arrays.asList(ObjectIdFunctionVData.class);
    }

    @Override
    public Value reduce(VData vData, Value v1, Value v2) {
        return v1;
    }

    @Override
    public Value createIdentity(VData vData) {
        return new PartialObjectIdValue();
    }

    @Override
    public Value finalize(VData vData, Value value) {
        return new ObjectIdValue(new ObjectId());
    }

    public static class PartialObjectIdValue extends NullValue implements UnresolvedValue {
        private static final long serialVersionUID = -5284589563312461349L;

        @Override
        public Value deepCopy() {
            return new PartialObjectIdValue();
        }
    }
}
