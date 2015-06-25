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

package notaql.model.predicate;

import notaql.datamodel.fixation.Fixation;
import notaql.evaluation.ValueEvaluationResult;
import notaql.model.path.InputPath;

import java.util.List;

/**
 * Created by thomas on 03.12.14.
 */
public class PathExistencePredicate implements Predicate {
    private static final long serialVersionUID = -2936332787860100949L;
    private InputPath path;

    /**
     * @param path A full path from the root.
     */
    public PathExistencePredicate(InputPath path) {
        this.path = path;
    }

    @Override
    public boolean evaluate(ValueEvaluationResult step, Fixation contextFixation) {
        final List<ValueEvaluationResult> evaluation;
        if(path.isRelative())
            evaluation = path.evaluate(step, contextFixation);
        else
            evaluation = path.evaluate(contextFixation);

        return evaluation.size() > 0;
    }

    public InputPath getPath() {
        return path;
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
