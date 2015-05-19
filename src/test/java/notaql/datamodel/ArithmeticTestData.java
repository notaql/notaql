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

package notaql.datamodel;

/**
 * Created by thomas on 01.12.14.
 */
public class ArithmeticTestData {
    private final ListValue o0Values;
    private final ListValue o1Values;
    private ListBasedCollectionValue collection;
    private ObjectValue o0;
    private ObjectValue o1;

    public ArithmeticTestData() {
        collection = new ListBasedCollectionValue();

        o0 = new ObjectValue();
        o0.put(new Step<>("_id"), new StringValue("a"));
        o0.put(new Step<>("weight"), new NumberValue(2));

        o0Values = new ListValue();
        o0Values.add(new NumberValue(1));
        o0Values.add(new NumberValue(2.5));
        o0Values.add(new NumberValue(3));

        o0.put(new Step<>("values"), o0Values);

        o1 = new ObjectValue();
        o1.put(new Step<>("_id"), new StringValue("b"));
        o1.put(new Step<>("weight"), new NumberValue(10));

        o1Values = new ListValue();
        o1Values.add(new NumberValue(1));
        o1Values.add(new NumberValue(2.5));
        o1Values.add(new NumberValue(3));

        o1.put(new Step<>("values"), o1Values);

        collection.add(o0);
        collection.add(o1);
    }

    public ListBasedCollectionValue getCollection() {
        return collection;
    }

    public ObjectValue getO0() {
        return o0;
    }

    public ObjectValue getO1() {
        return o1;
    }

    public ListValue getO0Values() {
        return o0Values;
    }

    public ListValue getO1Values() {
        return o1Values;
    }
}
