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

package notaql.engines.hbase;

import com.google.auto.service.AutoService;
import notaql.datamodel.*;
import notaql.engines.Engine;
import notaql.engines.EngineEvaluator;
import notaql.parser.TransformationParser;

import java.util.*;

/**
 * The HBase engine. This expects this argument: table_id
 */
@AutoService(Engine.class)
public class HBaseEngine implements Engine {
    private static final long serialVersionUID = -4279357370408702185L;

    @Override
    public EngineEvaluator createEvaluator(TransformationParser parser, Map<String, AtomValue<?>> params) {
        return new HBaseEngineEvaluator(this, parser, params);
    }

    @Override
    public String getEngineName() {
        return "hbase";
    }


    @Override
    public List<String> getArguments() {
        return Arrays.asList("table_id");
    }

    @Override
    public String toString() {
        return this.getEngineName();
    }
}
