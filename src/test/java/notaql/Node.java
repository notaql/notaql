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

package notaql;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
* Created by thomas on 26.01.15.
*/
public class Node extends HashMap<String, String> {
    private static final long serialVersionUID = -567765250606441591L;
    private String id;

    public Node(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static List<Node> generateNodes() {
        final List<Node> nodes = new LinkedList<Node>();

        final Node a = new Node("A");
        final Node b = new Node("B");
        final Node c = new Node("C");
        final Node d = new Node("D");
        final Node e = new Node("E");

        a.put("B", "1");
        a.put("C", "2");
        a.put("D", "3");
        a.put("E", "4");

        b.put("A", "1");
        c.put("A", "2");
        d.put("A", "3");
        e.put("A", "4");

        nodes.add(a);
        nodes.add(b);
        nodes.add(c);
        nodes.add(d);
        nodes.add(e);

        return nodes;
    }
}
