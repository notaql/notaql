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

package notaql.engines;

import notaql.model.EvaluationException;

import java.util.*;

/**
 * A utility function, which provides the tools to resolve engines based on their name
 */
public class EngineService {
    private Map<String, Engine> engines = new HashMap<>();
    private static EngineService service = null;

    private EngineService() {
        final ServiceLoader<Engine> engineLoader = ServiceLoader.load(Engine.class);

        for (Engine engine : engineLoader) {
            engines.put(engine.getEngineName(), engine);
        }
    }

    public static EngineService getInstance() {
        if(service == null)
            service = new EngineService();
        return service;
    }

    /**
     * Provides all engines
     * @return
     */
    public List<Engine> getEngines() {
        return new LinkedList<>(engines.values());
    }

    /**
     * Provides the engine with the given name
     * @param name
     * @return
     */
    public Engine getEngine(String name) {
        final Engine engine = engines.get(name);

        if(engine != null)
            return engine;

        throw new EvaluationException("Unsupported engines: " + name);
    }
}
