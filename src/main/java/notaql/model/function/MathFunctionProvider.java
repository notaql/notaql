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

import com.google.auto.service.AutoService;
import notaql.datamodel.NumberValue;

/**
 * Provides simple mathematical functions
 */
@AutoService(SimpleFunctionProvider.class)
public class MathFunctionProvider implements SimpleFunctionProvider {
    @SimpleFunction(name="PI")
    public static NumberValue pi() {
        return new NumberValue(Math.PI);
    }

    @SimpleFunction(name="EXP")
    public static NumberValue exp(NumberValue v) {
        return new NumberValue(Math.exp(v.getValue().doubleValue()));
    }

    @SimpleFunction(name="SQRT")
    public static NumberValue sqrt(NumberValue v) {
        return new NumberValue(Math.sqrt(v.getValue().doubleValue()));
    }

    @SimpleFunction(name="ABS")
    public static NumberValue abs(NumberValue v) {
        return new NumberValue(Math.abs(v.getValue().doubleValue()));
    }
}
