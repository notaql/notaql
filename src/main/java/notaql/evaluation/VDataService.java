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

package notaql.evaluation;

import notaql.model.vdata.VData;

import java.util.List;

/**
 * Base interface for evaluators for VData.
 */
public interface VDataService {
    /**
     * Provide the VData classes that are evaluated by this service
     * @return
     */
    public List<Class<? extends VData>> getProcessedClasses();
}
