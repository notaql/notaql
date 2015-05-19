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

package notaql.model.path;

import notaql.datamodel.*;
import notaql.datamodel.fixation.Fixation;
import notaql.evaluation.ValueEvaluationResult;
import notaql.model.EvaluationException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This splits the StringValues and creates an SplitAtomValue from them.
 * NOTE: this modifies the input!
 *
 * TODO: how to get rid of unchecked warning?
 *
 * @author Thomas Lottermann
 */
public class StringSplitMethodStep implements SplitMethodStep {
    private static final long serialVersionUID = 8671013110414981804L;
    private final String delimiter;

    public StringSplitMethodStep() {
        this(" ");
    }

    public StringSplitMethodStep(String delimiter) {
        this.delimiter = delimiter;
    }

    /**
     *
     * @param step
     * @param contextFixation
     * @return
     */
    @Override
    public List<ValueEvaluationResult> evaluate(ValueEvaluationResult step, Fixation contextFixation) {
        final Value value = step.getValue();
        if(!(value instanceof StringValue))
            throw new EvaluationException(".split() encountered something which was not a String: " + value.toString());

        final StringValue stringValue = (StringValue) value;

        final List<String> strings = Arrays.asList(stringValue.getValue().split(delimiter));
        final List<Value> splits = strings.stream().map(StringValue::new).collect(Collectors.toList());

        final ComplexValue<?> parent = stringValue.getParent();

        final SplitValue<Integer> split;

        // differentiate between temporary nodes and actual nodes from the input
        if(parent != null) {
            final Step attributeStep = parent.getStep(stringValue);

            // NOTE: this modifies the input
            split = parent.split(attributeStep, splits);
        } else {
            split = new SplitAtomValue<>(stringValue, splits);
        }



        return Arrays.asList(new ValueEvaluationResult(split, step.getFixation()));
    }

    @Override
    public String toString() {
        return "split('" + delimiter + "')";
    }
}
