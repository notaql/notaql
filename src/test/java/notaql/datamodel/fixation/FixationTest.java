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

package notaql.datamodel.fixation;

import notaql.datamodel.Step;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public class FixationTest {

    @Test
    public void testGetNextStep() throws Exception {
        final List<FixationStep<?>> base = new LinkedList<>();
        base.add(new FixationStep<>(new Step<>(0), true));
        base.add(new FixationStep<>(new Step<>("info"), false));

        final FixationStep<String> testStep = new FixationStep<>(new Step<>("test"), false);

        final Fixation baseFixation = new Fixation(null, base);
        final Fixation deeperFixation = new Fixation(baseFixation, testStep);
        final Fixation wrongFixation = new Fixation(baseFixation, new FixationStep<>(new Step<>("test2"), false));

        assert deeperFixation.getNextStep(baseFixation).equals(testStep);
        assert deeperFixation.getNextStep(deeperFixation) == null;
        assert deeperFixation.getNextStep(wrongFixation) == null;
    }

    @Test
    public void testGetCommonPrefix() throws Exception {
        // TODO - this method is still unused!
    }
}