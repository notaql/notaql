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
import notaql.engines.mongodb.model.vdata.HashFunctionVData;
import notaql.evaluation.Evaluator;
import notaql.evaluation.EvaluatorService;
import notaql.evaluation.ValueEvaluationResult;
import notaql.model.EvaluationException;
import notaql.model.vdata.VData;
import notaql.evaluation.values.ResolvingUtils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Allows calculating a hash code
 */
public class HashFunctionVDataEvaluator implements Evaluator {
    private static final long serialVersionUID = -4658960416178787343L;

    @Override
    public List<ValueEvaluationResult> evaluate(VData vData, Fixation fixation) {
        assert vData instanceof HashFunctionVData;

        final HashFunctionVData functionVData = (HashFunctionVData) vData;

        final VData pathVData = functionVData.getPath();

        final List<ValueEvaluationResult> eval = EvaluatorService.getInstance().evaluate(pathVData, fixation);

        if(eval.stream().anyMatch(r -> ResolvingUtils.isUnresolved(r.getValue())))
            throw new EvaluationException("Aggregation functions are not allowed in HASH");

        if(eval.stream().anyMatch(r -> !(r.getValue() instanceof AtomValue<?>)))
            throw new EvaluationException("HASH() expects a atom value as argument");

        final MessageDigest md5;

        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new EvaluationException(e);
        }

        return eval.stream().map(
                r -> new ValueEvaluationResult(
                        new StringValue(
                                new BigInteger(
                                        1,
                                        md5.digest(((AtomValue) r.getValue()).getValue().toString().getBytes())
                                ).toString(16)
                        ),
                        r.getFixation()
                )
        ).collect(Collectors.toList());
    }

    @Override
    public boolean canReduce(VData vData) {
        return false;
    }

    @Override
    public List<Class<? extends VData>> getProcessedClasses() {
        return Arrays.asList(HashFunctionVData.class);
    }
}
