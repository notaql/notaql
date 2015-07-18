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

package notaql.model.vdata.aggregation;

import notaql.datamodel.Value;
import notaql.model.vdata.VData;

import java.util.List;

/**
 * Created by Thomas Lottermann on 30.11.14.
 */
public abstract class SimpleAggregatingVData<T extends Value> implements AggregatingVData {
    private static final long serialVersionUID = 1120892492188598449L;
    private final VData expression;

    protected SimpleAggregatingVData(VData expression) {
        this.expression = expression;
    }

    public VData getExpression() {
        return expression;
    }

    public abstract T aggregate(List<Value> values);

    @Override
    public String toString() {
        return expression.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleAggregatingVData that = (SimpleAggregatingVData) o;

        if (expression != null ? !expression.equals(that.expression) : that.expression != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return expression != null ? expression.hashCode() : 0;
    }
}
