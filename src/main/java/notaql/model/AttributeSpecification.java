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

package notaql.model;

import notaql.model.path.OutputPath;
import notaql.model.vdata.VData;

import java.io.Serializable;

/**
 * Created by thomas on 17.11.14.
 *
 * FIXME: still necessary?
 */
public class AttributeSpecification implements Serializable {
    private static final long serialVersionUID = 7276769913257427685L;
    private OutputPath outputPath;
    private VData vData;

    public AttributeSpecification(OutputPath outputPath, VData vData) {
        this.outputPath = outputPath;
        this.vData = vData;
    }

    public OutputPath getOutputPath() {
        return outputPath;
    }

    public VData getVData() {
        return vData;
    }
}
