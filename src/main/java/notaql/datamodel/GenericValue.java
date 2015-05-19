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
 * This is a subclass of BaseAtomValue which shall be used to introduce new types.
 */
public abstract class GenericValue<T> extends BaseAtomValue<T> {
    private static final long serialVersionUID = 5961093139700312602L;

    public GenericValue(T value) {
        super(value);
    }

    public GenericValue(T value, ComplexValue<?> parent) {
        super(value, parent);
    }

    public abstract byte[] toBytes();
}
