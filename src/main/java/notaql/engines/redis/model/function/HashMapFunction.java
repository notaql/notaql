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

package notaql.engines.redis.model.function;

import notaql.engines.Engine;
import notaql.engines.redis.RedisEngine;
import notaql.model.function.*;

/**
 * Provides a hash map constructor for use in Redis
 */
public class HashMapFunction extends ObjectFunction {
    private static final long serialVersionUID = 547736819640753170L;

    @Override
    public String getName() {
        return "HASH_MAP";
    }

    @Override
    public boolean isApplicable(Engine inEngine, Engine outEngine) {
        return outEngine instanceof RedisEngine;
    }
}
