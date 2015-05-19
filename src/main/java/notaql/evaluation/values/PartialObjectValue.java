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

package notaql.evaluation.values;

import notaql.datamodel.*;
import notaql.datamodel.fixation.Fixation;
import notaql.model.vdata.VData;

import java.util.HashMap;
import java.util.Map;

/**
 * Partial object. This is specifically used in the OBJECT() constructor.
 */
public class PartialObjectValue extends ObjectValue implements UnresolvedValue {
    private static final long serialVersionUID = 3540348258515596632L;
    private Map<Step<String>, VData> vDataMap = new HashMap<>();

    public PartialObjectValue() {

    }

    public PartialObjectValue(PartialObjectValue v) {
        this(v, null);
    }

    public PartialObjectValue(PartialObjectValue v, ComplexValue<?> parent) {
        super(parent);

        vDataMap.putAll(v.vDataMap);
        super.putAll(v);
    }

    public VData getVData(Step<String> key) {
        return vDataMap.get(key);
    }

    public Value put(Step<String> key, Value value, VData vData) {
        vDataMap.put(key, vData);

        if(!containsKey(key))
            return super.put(key, value);


        final Value oldValue = get(key);

        final KeyGroupValue keyGroup;
        if (!(oldValue instanceof KeyGroupValue)) {
            keyGroup = new KeyGroupValue();
            keyGroup.add(oldValue);
        } else {
            keyGroup = (KeyGroupValue) oldValue;
        }

        keyGroup.add(value);

        return super.put(key, keyGroup);
    }

    @Override
    public Value removeKey(Step<String> key) {
        vDataMap.remove(key);
        return super.removeKey(key);
    }

    @Override
    public void putAll(Map<Step<String>, Value> m) {
        throw new UnsupportedOperationException("Using this method is not allowed for PartialObjectValues");
    }

    @Override
    public void putAll(ComplexValue<String> v) {
        throw new UnsupportedOperationException("Using this method is not allowed for PartialObjectValues");
    }

    @Override
    public Value put(Step<String> key, Value value) {
        throw new UnsupportedOperationException("Using this method is not allowed for PartialObjectValues");
    }

    @Override
    public Value deepCopy() {
        final PartialObjectValue copy = new PartialObjectValue();
        this.toMap().entrySet().stream().forEach(e -> copy.put(e.getKey(), e.getValue().deepCopy(), this.getVData(e.getKey())));
        return copy;
    }

    @Override
    public String groupKey() {
        if(isRoot()) {
            final Value id = this.get(new Step<>("_id"));
            if (id != null && !(id instanceof UnresolvedValue)) {
                if (id instanceof StringValue)
                    return ((StringValue) id).getValue();
                return id.groupKey();
            }
        }
        return super.groupKey();
    }
}


