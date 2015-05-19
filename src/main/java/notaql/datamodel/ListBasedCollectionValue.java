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
 * This represents a complete collection of objects - just like a collection in MongoDB
 *
 * @author Thomas Lottermann
 */
public class ListBasedCollectionValue extends ListValue {
    private static final long serialVersionUID = 6922046174490242988L;

    public ListBasedCollectionValue() {
        super();
    }

    public ListBasedCollectionValue(ListValue value) {
        this.addAll(value);
    }

    @Override
    public ListBasedCollectionValue deepCopy() {
        final ListBasedCollectionValue copy = new ListBasedCollectionValue();
        this.stream().forEach(e -> copy.add(e.deepCopy()));
        return copy;
    }
}
