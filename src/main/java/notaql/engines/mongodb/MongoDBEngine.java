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

package notaql.engines.mongodb;

import com.google.auto.service.AutoService;
import notaql.datamodel.AtomValue;
import notaql.engines.Engine;
import notaql.engines.EngineEvaluator;
import notaql.parser.TransformationParser;

import java.util.*;

/**
 * Created by Thomas Lottermann on 06.12.14.
 */
@AutoService(Engine.class)
public class MongoDBEngine implements Engine {
    private static final long serialVersionUID = 5898695057464458198L;


    @Override
    public EngineEvaluator createEvaluator(TransformationParser parser, Map<String, AtomValue<?>> params) {
        return new MongoDBEngineEvaluator(this, parser, params);
    }

    @Override
    public String getEngineName() {
        return "mongodb";
    }


    @Override
    public List<String> getArguments() {
        return Arrays.asList("database_name", "collection_name", "no_query");
    }

    @Override
    public String toString() {
        return this.getEngineName();
    }


}
