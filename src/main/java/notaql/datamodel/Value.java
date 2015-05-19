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

import java.io.Serializable;

/**
 * The inner model of values.
 *
 * @author Thomas Lottermann
 */
public interface Value extends Groupable, Serializable {
    /**
     * Provides the parent of the Value
     * @return
     */
    public ComplexValue<?> getParent();

    /**
     * Sets the parent of the value
     * @param parent
     */
    public void setParent(ComplexValue<?> parent);

    /**
     * Retruns true if this node is the root of the path
     * @return
     */
    public boolean isRoot();

    /**
     * Returns a deep copy of this value
     * @return
     */
    public Value deepCopy();
}
