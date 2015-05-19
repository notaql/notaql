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
public class SongTestData {
    private ListBasedCollectionValue collection;
    private ObjectValue o0;
    private ObjectValue o1;

    public SongTestData() {
        collection = new ListBasedCollectionValue();

        o0 = new ObjectValue();
        o0.put(new Step<>("_id"), new StringValue("rhcp"));
        o0.put(new Step<>("under_the_bridge"), new StringValue("sometimes i feel like sometimes"));
        o0.put(new Step<>("californication"), new StringValue("psychic spies sometimes the"));

        o1 = new ObjectValue();
        o1.put(new Step<>("_id"), new StringValue("tenacious d"));
        o1.put(new Step<>("kickapoo"), new StringValue("flapp flupp ding dong sometimes the"));
        o1.put(new Step<>("tribute"), new StringValue("this is not the greatest an best song in the world"));

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
}
