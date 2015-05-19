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

import notaql.datamodel.*;
import notaql.datamodel.fixation.Fixation;
import notaql.engines.Engine;
import notaql.engines.EngineEvaluator;
import notaql.evaluation.ValueEvaluationResult;
import notaql.model.predicate.Predicate;
import notaql.model.vdata.ObjectVData;
import notaql.model.vdata.aggregation.ListVData;

import java.util.Arrays;
import java.util.List;

/**
 * Created by thomas on 17.11.14.
 */
public class Transformation extends ListVData {
    private static final long serialVersionUID = 875340950573567153L;
    private ObjectVData objectVData;

    private Predicate inPredicate;
    private Predicate outPredicate;

    private transient EngineEvaluator inEngineEvaluator;
    private transient EngineEvaluator outEngineEvaluator;

    public Transformation(Predicate inPredicate, Predicate outPredicate, EngineEvaluator inEngineEvaluator, EngineEvaluator outEngineEvaluator, List<AttributeSpecification> specifications) {
        super(new ObjectVData(specifications));
        objectVData = (ObjectVData)super.getExpression();
        this.inPredicate = inPredicate;
        this.outPredicate = outPredicate;
        this.inEngineEvaluator = inEngineEvaluator;
        this.outEngineEvaluator = outEngineEvaluator;
    }

    public Transformation(Predicate inPredicate, Predicate outPredicate, EngineEvaluator inEngineEvaluator, EngineEvaluator outEngineEvaluator, AttributeSpecification... specifications) {
        this(inPredicate, outPredicate, inEngineEvaluator, outEngineEvaluator, Arrays.asList(specifications));
    }

    @Override
    public ObjectVData getExpression() {
        return (ObjectVData)super.getExpression();
    }

    public Predicate getInPredicate() {
        return inPredicate;
    }

    public Predicate getOutPredicate() {
        return outPredicate;
    }

    public EngineEvaluator getInEngineEvaluator() {
        return inEngineEvaluator;
    }

    public EngineEvaluator getOutEngineEvaluator() {
        return outEngineEvaluator;
    }

    public boolean satisfiesInPredicate(ObjectValue value) {
        if(getInPredicate() == null)
            return true;

        return getInPredicate().evaluate(
                new ValueEvaluationResult(
                        value,
                        new Fixation(
                                value
                        )
                ),
                new Fixation(value)
        );
    }

    @Override
    public String toString() {
        return
                "IN-ENGINE:" + inEngineEvaluator.toString() + ",\n" +
                "OUT-ENGINE:" + outEngineEvaluator.toString() + ",\n" +
                (inPredicate!=null?"IN-FILTER: " + inPredicate.toString() + ",\n":"") +
                (outPredicate!=null?"OUT-FILTER: " + outPredicate.toString() + ",\n":"") +
                objectVData.toString();
    }
}
