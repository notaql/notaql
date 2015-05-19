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

package notaql.engines.csv.datamodel;

import notaql.datamodel.*;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Provides the tools to convert to and from CSV
 */
public class ValueConverter {
    /**
     * Generates ObjectValues from a given csv record.
     * @param result
     * @return
     */
    public static Value convertToNotaQL(CSVRecord result) {
        final ObjectValue object = new ObjectValue();

        for (Map.Entry<String, String> cell : result.toMap().entrySet()) {
            object.put(new Step<>(cell.getKey()), ValueUtils.parse(cell.getValue()));
        }

        return object;
    }

    /**
     * Provides conversion from notaql's inner representation of rows (i.e. ObjectValues) to a list of columns.
     * @param object The "row"
     * @param table The column names of the output - in the right order
     * @return list of entries
     * @throws IOException
     */
    public static List<String> convertFromNotaQL(ObjectValue object, String[] table) throws IOException {
        final List<String> strings = new LinkedList<>();

        for (String key : table) {
            final Value value = object.get(new Step<>(key));

            if(value == null) {
                strings.add("");
                continue;
            }

            if(value instanceof AtomValue<?>) {
                strings.add(((AtomValue<?>)value).getValue().toString());
            } else {
                strings.add(value.toString());
            }
        }

        return strings;
    }

    public static boolean isEmpty(ObjectValue object) {
        return object.size() == 0;
    }
}
