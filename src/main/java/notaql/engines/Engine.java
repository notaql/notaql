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

import notaql.datamodel.AtomValue;
import notaql.parser.TransformationParser;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * This class provides all interfaces necessary in order to add support for new databases.
 *
 * @author Thomas Lottermann
 */
public interface Engine  extends Serializable {
    /**
     * Provides an evaluator for this engine
     * @return
     */
    public EngineEvaluator createEvaluator(TransformationParser parser, Map<String, AtomValue<?>> params);

    /**
     * Provides the name by which this engines is identified in the notaql expression
     *
     * @return
     */
    public String getEngineName();

    /**
     * Provides the ordered names of the arguments
     *
     * @return
     */
    public List<String> getArguments();

    /**
     * A simple class that provides keyword-arguments for the engines
     */
    public static class EngineArgument {
        private String keyWord;
        private AtomValue<?> atom;

        /**
         * Construct a key-value based keyword argument
         * @param keyWord
         * @param atom
         */
        public EngineArgument(String keyWord, AtomValue<?> atom) {
            this.keyWord = keyWord;
            this.atom = atom;
        }

        /**
         * Provides the key
         * @return
         */
        public String getKey() {
            return keyWord;
        }

        /**
         * Provides the value
         * @return
         */
        public AtomValue<?> getValue() {
            return atom;
        }
    }
}
