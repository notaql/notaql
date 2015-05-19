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

package notaql.engines.hbase.datamodel;

import notaql.datamodel.*;
import notaql.model.EvaluationException;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.*;

/**
 * Provides the tools to convert to and from HBase's internal format
 */
public class ValueConverter {
    /**
     * Generates an ObjectValue from a row. The rows are translated as follows:
     *
     *          colfamily1  colfamily2
     *          col1  col2  col3
     * rowid    val1  val2  val3
     *
     * to
     *
     * {
     *     "_id": "rowid",
     *     "colfamily1": {
     *         "col1": "val1",
     *         ...
     *     }
     *     ...
     * }
     *
     * @param result
     * @return
     */
    public static Value convertToNotaQL(Result result) {
        final NavigableMap<byte[], NavigableMap<byte[], byte[]>> inCols = result.getNoVersionMap();

        final String rowId = Bytes.toString(result.getRow());
        final ObjectValue object = new ObjectValue();
        object.put(new Step<>("_id"), new StringValue(rowId));

        for (Map.Entry<byte[], NavigableMap<byte[], byte[]>> colFamily : inCols.entrySet()) {
            final String colFamilyId = Bytes.toString(colFamily.getKey());

            final ObjectValue familyObject = new ObjectValue();

            for (Map.Entry<byte[], byte[]> col : colFamily.getValue().entrySet()) {
                // TODO: This just supports Strings due to HBase's idea of just allowing "Bytes" and not giving any information about the actual type.
                // We could solve this by either only allowing Tuples as input which also tell the type OR providing a constructor in NotaQL which specifies the expected type
                final String colId = Bytes.toString(col.getKey());
                final AtomValue<?> value = ValueUtils.parse(Bytes.toString(col.getValue()));

                familyObject.put(new Step<>(colId), value);
            }
            object.put(new Step<>(colFamilyId), familyObject);
        }

        return object;
    }

    public static Put convertFromNotaQL(ObjectValue object) throws IOException {
        return convertFromNotaQL(object, null);
    }

    /**
     * Convert object to row by creating a put from it
     * @param object
     * @param table
     * @return
     * @throws IOException
     */
    public static Put convertFromNotaQL(ObjectValue object, HTable table) throws IOException {

        final Value rowIdValue = object.get(new Step<>("_id"));

        if(rowIdValue == null)
            throw new EvaluationException("Row id was null.");

        if(!(rowIdValue instanceof AtomValue<?>))
            throw new EvaluationException("Row id may only be of an atomic value");

        final String rowId = ((AtomValue<?>)rowIdValue).getValue().toString();

        final Put put = new Put(Bytes.toBytes(rowId));

        // create metadata in case it doesn't already exist
        if(table != null)
            createMetaData(object, table);

        for (Map.Entry<Step<String>, Value> columnFam : object.toMap().entrySet()) {
            if(columnFam.getKey().getStep().equals("_id"))
                continue;

            final String colFamilyId = columnFam.getKey().getStep();

            if(!(columnFam.getValue() instanceof ObjectValue))
                throw new EvaluationException("Column families must be of ObjectValues");

            final ObjectValue cols = (ObjectValue)columnFam.getValue();

            for (Map.Entry<Step<String>, Value> col : cols.toMap().entrySet()) {
                final String colId = col.getKey().getStep();
                final String value;
                if(col.getValue() instanceof AtomValue<?>) {
                    value = ((AtomValue<?>)col.getValue()).getValue().toString();
                } else {
                    value = col.getValue().toString();
                }

                put.add(Bytes.toBytes(colFamilyId), Bytes.toBytes(colId), Bytes.toBytes(value));
            }
        }
        return put;
    }

    public static void createMetaData(ObjectValue object, HTable table) throws IOException {
        final HBaseAdmin admin = new HBaseAdmin(table.getConfiguration());
        // create non existing column families
        for (Step<String> columnFamily : object.keySet()) {
            if(columnFamily.getStep().equals("_id"))
                continue;

            final String columnFamilyId = columnFamily.getStep();

            // check if column family exists
            boolean exists = false;
            for (HColumnDescriptor familyDescriptor : table.getTableDescriptor().getFamilies()) {
                if(Bytes.toString(familyDescriptor.getName()).equals(columnFamilyId)) {
                    exists = true;
                    break;
                }
            }
            // if not: add it
            if(!exists) {
                admin.disableTable(table.getTableName());
                admin.addColumn(table.getTableName(), new HColumnDescriptor(columnFamilyId));
                admin.enableTable(table.getTableName());
            }
        }
    }

    public static boolean isEmpty(ObjectValue object) {
        return object.toMap().entrySet()
                .stream()
                .filter(e->e.getValue() instanceof ObjectValue)
                .allMatch(e->((ObjectValue)e.getValue()).size() == 0);
    }
}
