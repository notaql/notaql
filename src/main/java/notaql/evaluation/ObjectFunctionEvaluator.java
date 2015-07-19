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

package notaql.evaluation;

import notaql.datamodel.ObjectValue;
import notaql.datamodel.Step;
import notaql.datamodel.Value;
import notaql.datamodel.fixation.Fixation;
import notaql.model.EvaluationException;
import notaql.model.function.Argument;
import notaql.model.function.Arguments;
import notaql.model.function.FunctionEvaluator;
import notaql.model.function.FunctionReducer;
import notaql.model.path.IgnoredIdStep;
import notaql.model.path.StepNameEvaluationResult;
import notaql.evaluation.values.PartialObjectValue;
import notaql.model.vdata.VData;
import notaql.evaluation.values.KeyGroupValue;
import scala.Tuple2;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * This is the most complex evaluator in the whole project.
 *
 * The complicated part is evaluating multiple specifications after each other, because each specification may be ambigous
 * and may bind new paths.
 *
 * Every evaluation of ObjectVData starts by simply providing a "PartialObjectValue" which is then later
 * resolved into an "ObjectValue".
 *
 * FIXME: Transformations are probably broken now!
 * FIXME: AGGREGATINGOBJECTVDATA SUPPORT MISSING!
 */
public class ObjectFunctionEvaluator implements FunctionEvaluator, FunctionReducer {
    private static final long serialVersionUID = 2062262076467699656L;

    @Override
    public List<ValueEvaluationResult> evaluate(Arguments args, Fixation fixation) {
        final List<Argument> specifications = args.getKWArgs();

        // start with a single empty result
        List<ValueEvaluationResult> partialResults = Arrays.asList(new ValueEvaluationResult(new PartialObjectValue(), fixation));

        // for each attribute definition...
        for (Argument specification : specifications) {
            final List<ValueEvaluationResult> newPartialResults = new LinkedList<>();
            // iterate over the already created partial results
            for (ValueEvaluationResult partialResult : partialResults) {
                // evaluate the specification by extending the current partial result
                newPartialResults.addAll(evaluateSpecification(specification, partialResult));
            }
            // update the partial results for the next attribute specification
            partialResults = newPartialResults;
        }

        return partialResults;
    }

    @Override
    public boolean canReduce(Arguments args) {
        return true;
    }

    /**
     * Evaluates a specification by extending a given partial result and returning the (possibly many) extended results.
     * <p>
     * The resulting fixations may be more specific than before.
     *
     * @param specification    The specification that shall be evaluated
     * @param extendableResult The intermediate result that shall be extended by evaluating this specification.
     * @return
     */
    private List<ValueEvaluationResult> evaluateSpecification(Argument specification, ValueEvaluationResult extendableResult) {
        assert specification.getPath() != null;

        // collect the new partial results
        List<ValueEvaluationResult> partialResults = new LinkedList<>();

        assert extendableResult.getValue() instanceof PartialObjectValue;
        final PartialObjectValue partialObject = (PartialObjectValue) extendableResult.getValue();
        final Fixation partialFixation = extendableResult.getFixation();

        final PartialObjectValue startNewPartialObject = new PartialObjectValue(partialObject);
        partialResults.add(new ValueEvaluationResult(startNewPartialObject, partialFixation));

        // first evaluate all the possible target paths. This might specify the fixation further
        final List<StepNameEvaluationResult> targetPaths = specification.getPath().evaluate(partialFixation);

        // Then iterate over the resulting paths - each path should be appended to the input object gaining multiple objects.
        // The objects are copied in case the vData evaluation is ambiguous
        for (StepNameEvaluationResult targetPath : targetPaths) {
            // in case we have multiple steps: abort
            if (targetPath.getSteps().size() > 1)
                throw new EvaluationException("Multiple steps of output paths are not allowed in the OBJECT constructor.");

            // in case we have no step: abort
            if (targetPath.getSteps().size() < 1)
                throw new EvaluationException("The evaluation of a target path in the OBJECT constructor lead to an empty path. This is not allowed.");

            // get the single step
            final Step<?> step = targetPath.getSteps().get(0);
            final Step<String> objectStep;
            if (step instanceof IgnoredIdStep.IgnoredStep)
                objectStep = (IgnoredIdStep.IgnoredStep) step;
            else
                objectStep = new Step<>(step.toString());

            // make sure to have the most specific fixation in hand.
            final Fixation moreSpecificFixation = targetPath.getFixation().getMoreSpecific(partialFixation);

            // evaluate the vData - multiple possible results can be returned here
            final List<ValueEvaluationResult> valueEvaluationResults = EvaluatorService.getInstance()
                    .evaluate(specification.getVData(), moreSpecificFixation);

            final List<ValueEvaluationResult> extendedPartialResults = new LinkedList<>();

            // iterate over the vData results and clone and extend the partial objects from the last round
            // (of target paths) for each result
            for (ValueEvaluationResult valueEvaluationResult : valueEvaluationResults) {
                for (ValueEvaluationResult partialResult : partialResults) {
                    final PartialObjectValue newPartialObject = (PartialObjectValue) partialResult.getValue();
                    // copy the object
                    final PartialObjectValue splitPartialObject = new PartialObjectValue(newPartialObject);

                    // extend it with the new data
                    splitPartialObject.put(objectStep, valueEvaluationResult.getValue().deepCopy(), specification.getVData());
                    // store it in the temporary splitting list
                    extendedPartialResults.add(new ValueEvaluationResult(splitPartialObject, valueEvaluationResult.getFixation().getMoreSpecific(moreSpecificFixation)));
                }
            }

            // in case the expression does not generate any results and: keep the current value (if it already contains data)
            if(valueEvaluationResults.isEmpty() && ((PartialObjectValue) extendableResult.getValue()).size() > 0)
                extendedPartialResults.add(extendableResult);

            partialResults = extendedPartialResults;
        }

        return partialResults;
    }

    /**
     * @param args
     * @param v1    reduced as much as possible
     * @param v2    to add to v1
     * @return
     */
    @Override
    public PartialObjectValue reduce(Arguments args, Value v1, Value v2) {
        assert v1 instanceof PartialObjectValue && v2 instanceof PartialObjectValue;

        final PartialObjectValue merge = merge((PartialObjectValue) v1, (PartialObjectValue) v2);
        final PartialObjectValue result = new PartialObjectValue();

        merge.toMap().entrySet()
                .stream()
                .forEach( // add all attributes to the resulting object
                        e -> {
                            final VData mergeVData = merge.getVData(e.getKey());
                            if (EvaluatorService.getInstance().canReduce(mergeVData)) // in case the step is reducable: do so in any case!
                            {
                                if (e.getValue() instanceof KeyGroupValue) // unnest for keygroupvalues
                                    result.put(
                                            e.getKey(),
                                            ((KeyGroupValue) e.getValue()).stream().reduce(EvaluatorService.getInstance().createIdentity(mergeVData), (a, b) -> EvaluatorService.getInstance()
                                                    .reduce(mergeVData, a, b)),
                                            mergeVData
                                    );
                                else
                                    result.put(
                                            e.getKey(),
                                            EvaluatorService.getInstance().reduce(mergeVData, EvaluatorService.getInstance().createIdentity(mergeVData), e.getValue()),
                                            mergeVData
                                    );
                            } else { // for any other value: just add it
                                result.put(e.getKey(), e.getValue(), mergeVData);
                            }
                        }
                );

        return result;
    }

    @Override
    public Value createIdentity(Arguments args) {
        return new PartialObjectValue();
    }

    /**
     * Simply copy the data back to a non-partial object.
     *
     * @param args
     * @param value
     * @return
     */
    @Override
    public Value finalize(Arguments args, Value value) {
        assert value instanceof PartialObjectValue;
        final PartialObjectValue partialObjectValue = (PartialObjectValue) value;

        final ObjectValue result = new ObjectValue();


        partialObjectValue.toMap().entrySet().stream()
                .filter(e -> !(e.getKey() instanceof IgnoredIdStep.IgnoredStep))
                .map(e -> new Tuple2<>(
                                e.getKey(),
                                !EvaluatorService.getInstance().canReduce(partialObjectValue.getVData(e.getKey())) ?
                                        e.getValue()
                                        : EvaluatorService.getInstance().finalize(partialObjectValue.getVData(e.getKey()), e.getValue())
                        )
                )
                .forEach(t -> result.put(t._1, t._2));

        return result;
    }

    public static PartialObjectValue merge(PartialObjectValue o1, PartialObjectValue o2) {
        final PartialObjectValue target = new PartialObjectValue();

        crossCopy(o1, target);
        crossCopy(o2, target);

        return target;
    }

    /**
     * for each value put the step into the partial object. In case we have a KeyGroupValue, put all in.
     *
     * @param source
     * @param target
     */
    private static void crossCopy(PartialObjectValue source, PartialObjectValue target) {
        source.toMap().entrySet().stream().forEach(e ->
                {
                    final VData vData = source.getVData(e.getKey());
                    if (e.getValue() instanceof KeyGroupValue) {
                        ((KeyGroupValue) e.getValue()).stream()
                                .forEach(
                                        f -> target.put(
                                                e.getKey(),
                                                f,
                                                vData
                                        )
                                );
                        // in case we have an Unresolved value or
                        // any value which has not been added yet: add it
                    } else if (EvaluatorService.getInstance().canReduce(vData) || !target.containsKey(e.getKey())) {
                        target.put(
                                e.getKey(),
                                e.getValue(),
                                vData
                        );
                    }
                }
        );
    }


}
