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

package notaql.engines.mongodb;

import notaql.datamodel.Step;
import notaql.model.EvaluationException;
import notaql.model.path.IdStep;
import notaql.model.path.InputPath;
import notaql.model.path.InputPathStep;
import notaql.model.predicate.*;
import notaql.model.vdata.AtomVData;
import notaql.model.vdata.InputVData;
import notaql.model.vdata.VData;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.bson.types.BasicBSONList;

import java.util.stream.Collectors;

/**
 * Created by thomas on 12.06.15.
 */
public class FilterTranslator {
    public static BSONObject toMongoDBQuery(Predicate filter) {
        // Drop all incompatible parts of the predicate
        final Predicate reducedFilter = reducePredicate(filter);
        // Convert to MongoDB query
        final BSONObject query = translatePredicate(reducedFilter);

        return query;
    }

    /**
     * Translates compatible predicates to MongoDB queries
     * @param predicate
     * @return
     */
    private static BSONObject translatePredicate(Predicate predicate) {
        if(predicate == null)
            return new BasicBSONObject();

        if(predicate instanceof LogicalOperationPredicate)
            return translateLogicalOperationPredicate((LogicalOperationPredicate) predicate);
        if(predicate instanceof NegatedPredicate)
            return translateNegatedPredicate((NegatedPredicate) predicate);
        if(predicate instanceof PathExistencePredicate)
            return translatePathExistencePredicate((PathExistencePredicate) predicate);
        if(predicate instanceof RelationalPredicate)
            return translateRelationalPredicate((RelationalPredicate) predicate);

        throw new EvaluationException("Unknown predicate: " + predicate.toString());
    }

    private static BSONObject translateRelationalPredicate(RelationalPredicate predicate) {
        final VData left = predicate.getLeft();
        final VData right = predicate.getRight();

        assert left instanceof AtomVData && right instanceof InputVData || right instanceof AtomVData && left instanceof InputVData;

        final InputVData field;
        final AtomVData value;
        final String op;

        if(right instanceof AtomVData) {
            field = (InputVData)left;
            value = (AtomVData)right;
            switch (predicate.getOperator()) {
                case LT:
                    op = "$lt";
                    break;
                case LTEQ:
                    op = "$lte";
                    break;
                case GT:
                    op = "$gt";
                    break;
                case GTEQ:
                    op = "$gte";
                    break;
                case EQ:
                    op = "$eq";
                    break;
                case NEQ:
                    op = "$ne";
                    break;
                default:
                    throw new EvaluationException("Unexpected operator: " + predicate.getOperator());
            }
        } else {
            field = (InputVData)right;
            value = (AtomVData)left;
            switch (predicate.getOperator()) {
                case LT:
                    op = "$gt";
                    break;
                case LTEQ:
                    op = "$gte";
                    break;
                case GT:
                    op = "$lt";
                    break;
                case GTEQ:
                    op = "$lte";
                    break;
                case EQ:
                    op = "$eq";
                    break;
                case NEQ:
                    op = "$ne";
                    break;
                default:
                    throw new EvaluationException("Unexpected operator: " + predicate.getOperator());
            }
        }

        assert field.getPath().getPathSteps().size() >= 1 && field.getPath().getPathSteps().stream().allMatch(s -> s instanceof IdStep);

        final String path = field.getPath().getPathSteps().stream()
                .map(s -> ((IdStep) s).getId().getStep().toString())
                .collect(Collectors.joining("."));

        return new BasicBSONObject(path, new BasicBSONObject(op, value.getValue().getValue()));
    }

    private static BSONObject translatePathExistencePredicate(PathExistencePredicate predicate) {
        assert predicate.getPath().getPathSteps().size() >= 1 && predicate.getPath().getPathSteps().stream().allMatch(s -> s instanceof IdStep);

        final String path = predicate.getPath().getPathSteps().stream()
                .map(s -> ((IdStep) s).getId().getStep().toString())
                .collect(Collectors.joining("."));

        final BasicBSONObject query = new BasicBSONObject();
        query.put(path, new BasicBSONObject("$exists", true));
        return query;
    }

    private static BSONObject translateNegatedPredicate(NegatedPredicate predicate) {
        final BasicBSONList norList = new BasicBSONList();
        norList.add(translatePredicate(predicate.getPredicate()));

        return new BasicBSONObject("$nor", norList);
    }

    private static BSONObject translateLogicalOperationPredicate(LogicalOperationPredicate predicate) {
        final BasicBSONList opList = new BasicBSONList();
        opList.add(translatePredicate(predicate.getLeft()));
        opList.add(translatePredicate(predicate.getRight()));

        final String op;

        if(predicate.getOperator().equals(LogicalOperationPredicate.Operator.AND))
            op = "$and";
        else if(predicate.getOperator().equals(LogicalOperationPredicate.Operator.OR))
            op = "$or";
        else
            throw new EvaluationException("Unexpected operator: " + predicate.getOperator());

        return new BasicBSONObject(op, opList);
    }

    /**
     * Drops all incompatible parts of a predicate tree
     * @param predicate
     * @return
     */
    private static Predicate reducePredicate(Predicate predicate) {
        if(predicate instanceof LogicalOperationPredicate)
            return reduceLogicalOperationPredicate((LogicalOperationPredicate) predicate);
        if(predicate instanceof NegatedPredicate)
            return reduceNegatedPredicate((NegatedPredicate) predicate);
        if(predicate instanceof PathExistencePredicate)
            return reducePathExistencePredicate((PathExistencePredicate) predicate);
        if(predicate instanceof RelationalPredicate)
            return reduceRelationalPredicate((RelationalPredicate) predicate);

        throw new EvaluationException("Unknown predicate: " + predicate.toString());
    }

    private static Predicate reduceRelationalPredicate(RelationalPredicate predicate) {
        final VData left = reduceVData(predicate.getLeft());
        final VData right = reduceVData(predicate.getRight());

        if(left != null && right != null && (left instanceof AtomVData && right instanceof InputVData || right instanceof AtomVData && left instanceof InputVData))
            return new RelationalPredicate(left, right, predicate.getOperator());

        return null;
    }

    private static Predicate reducePathExistencePredicate(PathExistencePredicate predicate) {
        final InputPath path = reduceInputPath(predicate.getPath());

        if(path != null)
            return new PathExistencePredicate(path);

        return null;
    }

    private static Predicate reduceNegatedPredicate(NegatedPredicate predicate) {
        final Predicate reduced = reducePredicate(predicate.getPredicate());

        if(reduced != null)
            return new NegatedPredicate(reduced);

        return null;
    }

    private static Predicate reduceLogicalOperationPredicate(LogicalOperationPredicate predicate) {
        final Predicate left = reducePredicate(predicate.getLeft());
        final Predicate right = reducePredicate(predicate.getRight());

        // TODO: This may allow more complex stuff: AND would be okay here for example
        if(left != null && right != null)
            return new LogicalOperationPredicate(left, right, predicate.getOperator());

        return null;
    }

    private static VData reduceVData(VData vData) {
        if(vData instanceof AtomVData)
            return reduceAtomVData((AtomVData) vData);
        if(vData instanceof InputVData)
            return reduceInputVData((InputVData) vData);
        return null;
    }

    private static VData reduceInputVData(InputVData vData) {
        final InputPath path = reduceInputPath(vData.getPath());
        if(path != null)
            return new InputVData(path);

        return null;
    }

    private static InputPath reduceInputPath(InputPath path) {
        if(path.getPathSteps().size() < 1)
            return null;

        if(!path.getPathSteps().stream().allMatch(s -> s instanceof IdStep))
            return null;

        return path;
    }

    private static VData reduceAtomVData(AtomVData vData) {
        return new AtomVData(vData.getValue());
    }


}
