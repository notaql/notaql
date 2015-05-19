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

import notaql.model.EvaluationException;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This defines the equivalence groups of values. All grouping is based on the provided value.
 */
public interface Groupable {
    /**
     * This defines the equivalence groups of values. All grouping is based on the provided value.
     * It should try to reduce collisions as much as possible.
     * @return
     */
    public String groupKey();

    /**
     * A helper class to make generating groups as easy as possible
     */
    public static class GroupHelper {
        /**
         * Provides the hex representation of the md5 digest of the given string
         * @param string
         * @return
         */
        public static String digest(String string) {
            final MessageDigest digest = getDigest();
            final byte[] hash = digest.digest(string.getBytes());
            return new BigInteger(1, hash).toString(16);
        }

        private static MessageDigest getDigest() {
            try {
                return MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new EvaluationException("Hashfunction not available!");
            }
        }
    }

}
