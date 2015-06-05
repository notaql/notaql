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

package notaql.engines.json.datamodel;

import notaql.datamodel.*;
import notaql.model.EvaluationException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        if(o.equals(JSONObject.NULL))
            return new NullValue();
        if(o instanceof String)
            return new StringValue((String) o);
        if(o instanceof Number)
            return new NumberValue((Number) o);
        if(o instanceof Boolean)
            return new BooleanValue((Boolean) o);

        // complex values
        if(o instanceof JSONArray) {
            final JSONArray array = (JSONArray) o;
            final ListValue result = new ListValue();
            IntStream.range(0, array.length())
                    .forEach(i -> result.add(convertToNotaQL(array.get(i))));
            return result;
        }

        if(o instanceof JSONObject) {
            final JSONObject jsonObject = (JSONObject) o;
            final ObjectValue result = new ObjectValue();

            final Iterator<String> keyIterator = jsonObject.keys();
            while(keyIterator.hasNext()) {
                final String key = keyIterator.next();
                final Step<String> step = new Step<>(key);
                final Value value = convertToNotaQL(jsonObject.get(key));

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
        if(o instanceof NumberValue)
            return ((NumberValue) o).getValue();
        if(o instanceof BooleanValue)
            return ((BooleanValue) o).getValue();
        if(o instanceof SplitAtomValue<?>)
            return ((SplitAtomValue<?>)o).getValue();

        // complex values
        if(o instanceof ListValue) {
            return new JSONArray(
                    ((ListValue)o).stream()
                            .map(ValueConverter::convertFromNotaQL)
                            .collect(Collectors.toList())
            );
        }
        if(o instanceof ObjectValue) {
            final JSONObject jsonObject = new JSONObject();
            for (Map.Entry<Step<String>, Value> entry : ((ObjectValue) o).toMap().entrySet()) {
                jsonObject.put(entry.getKey().getStep(), convertFromNotaQL(entry.getValue()));
            }
            return jsonObject;
        }

        throw new EvaluationException("Unsupported type written: " + o.getClass() + ": " + o.toString());
    }
}
