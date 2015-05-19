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

package notaql.parser;

import notaql.datamodel.AtomValue;
import notaql.engines.Engine;
import notaql.engines.EngineEvaluator;
import notaql.engines.EngineService;
import notaql.model.AttributeSpecification;
import notaql.model.EvaluationException;
import notaql.model.Transformation;
import notaql.model.path.OutputPath;
import notaql.model.predicate.Predicate;
import notaql.model.vdata.VData;
import notaql.model.vdata.aggregation.AggregatingObjectVData;
import notaql.parser.antlr.NotaQL2Parser;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.*;

/**
 * Created by thomas on 08.01.15.
 */
public class TransformationParser {
    private final NotaQL2Parser.TransformationContext transformationContext;

    private final EngineEvaluator inEngineEvaluator;
    private final EngineEvaluator outEngineEvaluator;

    private final AtomValueVisitor atomValueVisitor;
    private final InputPathVisitor inputPathVisitor;
    private final OutputPathVisitor outputPathVisitor;
    private final PredicateVisitor predicateVisitor;
    private final VDataVisitor vDataVisitor;

    public TransformationParser(NotaQL2Parser.TransformationContext transformationContext) {
        this.transformationContext = transformationContext;

        // create all visitors necessary to parse stuff
        this.atomValueVisitor = new AtomValueVisitor(this);
        this.inputPathVisitor = new InputPathVisitor(this);
        this.outputPathVisitor = new OutputPathVisitor(this);
        this.predicateVisitor = new PredicateVisitor(this);
        this.vDataVisitor = new VDataVisitor(this);

        // get the engines
        this.inEngineEvaluator = getEngineEvaluator(transformationContext.inEngine().engine());
        this.outEngineEvaluator = getEngineEvaluator(transformationContext.outEngine().engine());
    }

    public Transformation parse() {
        Predicate inPredicate = null;
        if(transformationContext.inPredicate() != null) {
            inPredicate = predicateVisitor.visit(transformationContext.inPredicate().predicate());
        }

        final List<AttributeSpecification> specifications = new LinkedList<>();

        for (NotaQL2Parser.AttributeSpecificationContext specificationCtx : transformationContext.attributeSpecification()) {
            final OutputPath outputPath = getOutputPathVisitor().visit(specificationCtx.genericOutputPath());
            final VData vData = getVDataVisitor().visit(specificationCtx.vData());

            appendObjectifiedPath(outputPath, vData, specifications);
        }

        return new Transformation(inPredicate, null, getEngineEvaluator(transformationContext.inEngine().engine()), getEngineEvaluator(transformationContext.outEngine().engine()), specifications);
    }

    /**
     * Objectify the paths and then append it to the specifications (or replace)
     * @param path
     * @param vData
     * @param specifications
     */
    private void appendObjectifiedPath(OutputPath path, VData vData, List<AttributeSpecification> specifications) {
        final AttributeSpecification specification = objectifyPath(path, vData, specifications);

        // look if we already aggregated something of this name
        final Optional<AttributeSpecification> optionalSpecification = specifications
                .stream()
                .filter(s -> s.getOutputPath().getPathSteps().get(0).equals(path.getPathSteps().get(0)))
                .findAny();

        if(optionalSpecification.isPresent()) {
            Collections.replaceAll(specifications, optionalSpecification.get(), specification);
            return;
        }

        specifications.add(specification);
    }

    /**
     * This ensures to reduce the steps in the output paths to one
     *
     * @param path
     * @param vData
     * @return
     */
    private AttributeSpecification objectifyPath(OutputPath path, VData vData, List<AttributeSpecification> specifications) {
        int size = path.getPathSteps().size();
        if(size > 1) {
            final OutputPath subPath = new OutputPath(new LinkedList<>(path.getPathSteps().subList(1, size)));
            final AttributeSpecification subSpecification;
            final LinkedList<AttributeSpecification> nestedSpecifications;

            // look if we already aggregated something of this name
            final Optional<AttributeSpecification> optionalSpecification = specifications
                    .stream()
                    .filter(s -> s.getOutputPath().getPathSteps().get(0).equals(path.getPathSteps().get(0)))
                    .findAny();

            // in case we did: make sure it is an AggregatingObjectVData and then remember the old specifications
            if(optionalSpecification.isPresent()) {
                if(!(optionalSpecification.get().getVData() instanceof AggregatingObjectVData))
                    throw new EvaluationException("The output path '"+ path.toString() + "' clashes with the same one.");

                nestedSpecifications = new LinkedList<>(((AggregatingObjectVData) optionalSpecification.get().getVData()).getSpecifications());

                subSpecification = objectifyPath(subPath, vData, nestedSpecifications);
            } else {
                nestedSpecifications = new LinkedList<>();
                subSpecification = objectifyPath(subPath, vData, Collections.<AttributeSpecification>emptyList());
            }

            // Construct the new Aggregating VData
            nestedSpecifications.add(subSpecification);
            final AggregatingObjectVData resultingVData = new AggregatingObjectVData(nestedSpecifications);

            final OutputPath reducedPath = new OutputPath(path.getPathSteps().get(0));
            return new AttributeSpecification(reducedPath, resultingVData);
        }

        return new AttributeSpecification(path, vData);
    }

    private EngineEvaluator getEngineEvaluator(@NotNull NotaQL2Parser.EngineContext ctx) {
        final HashMap<String, AtomValue<?>> params = new HashMap<>();
        for (int i = 0; i < ctx.atom().size(); i++) {
            final String key = ctx.Name(i + 1).getText();
            final AtomValue<?> value = getAtomValueVisitor().visit(ctx.atom(i));
            params.put(key, value);
        }

        return EngineService.getInstance().getEngine(ctx.engineName.getText()).createEvaluator(this, params);
    }

    public EngineEvaluator getInEngineEvaluator() {
        return inEngineEvaluator;
    }

    public EngineEvaluator getOutEngineEvaluator() {
        return outEngineEvaluator;
    }

    public AtomValueVisitor getAtomValueVisitor() {
        return atomValueVisitor;
    }

    public InputPathVisitor getInputPathVisitor() {
        return inputPathVisitor;
    }

    public OutputPathVisitor getOutputPathVisitor() {
        return outputPathVisitor;
    }

    public PredicateVisitor getPredicateVisitor() {
        return predicateVisitor;
    }

    public VDataVisitor getVDataVisitor() {
        return vDataVisitor;
    }
}
