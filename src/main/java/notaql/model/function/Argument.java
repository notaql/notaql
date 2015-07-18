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

package notaql.model.function;

import notaql.model.vdata.VData;

/**
 * Represents an instance of a parameter
 */
public class Argument {
    private String name = null;
    private VData vData;

    public Argument(VData vData) {
        this.vData = vData;
    }

    public Argument(String name, VData vData) {
        this.name = name;
        this.vData = vData;
    }

    public String getName() {
        return name;
    }

    public VData getVData() {
        return vData;
    }
}
