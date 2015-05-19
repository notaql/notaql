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

package notaql.engines.mongodb.datamodel;

import com.mongodb.BasicDBObject;
import notaql.datamodel.*;
import notaql.model.EvaluationException;
import org.bson.BSONObject;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Converts MongoDB's format to our internal format and vice versa.
 *
 * Supported types are:
 * - Null
 * - String
 * - Date
 * - Number
 * - Boolean
 * - ObjectId
 * - List
 * - Object
 */
public class ValueConverter {
    public static Value convertToNotaQL(Object o) {
        // Atom values
        if(o == null)
            return new NullValue();
        if(o instanceof String)
            return new StringValue((String) o);
        if(o instanceof Date)
            return new DateValue((Date) o);
        if(o instanceof Number)
            return new NumberValue((Number) o);
        if(o instanceof Boolean)
            return new BooleanValue((Boolean) o);
        if(o instanceof ObjectId)
            return new ObjectIdValue((ObjectId) o);

        // complex values
        if(o instanceof List) {
            final List list = (List) o;
            final ListValue result = new ListValue();
            for (Object item : list) {
                result.add(convertToNotaQL(item));
            }
            return result;
        }

        if(o instanceof BSONObject) {
            final BSONObject bsonObject = (BSONObject) o;
            final ObjectValue result = new ObjectValue();

            final Map map = bsonObject.toMap();

            for (Map.Entry<?, ?> entry : (Set<Map.Entry<?, ?>>) map.entrySet()) {
                final Step<String> step = new Step<>(entry.getKey().toString());
                final Value value = convertToNotaQL(entry.getValue());

                result.put(step, value);
            }

            return result;
        }

        throw new EvaluationException("Unsupported type read: " + o.getClass() + ": " + o.toString());
    }

    public static Object convertFromNotaQL(Value o) {
        // Atom values
        if(o instanceof NullValue)
            return null;
        if(o instanceof StringValue)
            return ((StringValue) o).getValue();
        if(o instanceof DateValue)
            return ((DateValue) o).getValue();
        if(o instanceof NumberValue)
            return ((NumberValue) o).getValue();
        if(o instanceof BooleanValue)
            return ((BooleanValue) o).getValue();
        if(o instanceof ObjectIdValue)
            return ((ObjectIdValue) o).getValue();
        if(o instanceof SplitAtomValue<?>)
            return ((SplitAtomValue<?>)o).getValue();

        // complex values
        if(o instanceof ListValue) {
            return ((ListValue)o).stream().map(ValueConverter::convertFromNotaQL).collect(Collectors.toList());
        }
        if(o instanceof ObjectValue) {
            final BasicDBObject dbObject = new BasicDBObject();
            for (Map.Entry<Step<String>, Value> entry : ((ObjectValue) o).toMap().entrySet()) {
                dbObject.put(entry.getKey().getStep(), convertFromNotaQL(entry.getValue()));
            }
            return dbObject;
        }

        throw new EvaluationException("Unsupported type written: " + o.getClass() + ": " + o.toString());
    }
}
