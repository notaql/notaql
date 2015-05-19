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

import com.mongodb.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class NotaQLMongoDBTest {
    public static final String inCol = "NotaQLTestIn";
    public static final String outCol = "NotaQLTestOut";

    public static String engines;

    private MongoClient mongoClient;
    private DB db;

    @Test
    public void testIdentity() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._id <- IN._id," +
                "OUT.$(IN.*.name()) <- IN.@;";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testFamilyPredicate() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._id <- IN._id," +
                "OUT.children <- LIST(IN.children[*]);";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testStepPredicate() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._id <- IN._id," +
                "OUT.$(IN.children[?(@.allowance > '8')].name) <- IN.children[@].allowance;";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testInFilter() throws Exception {
        createPaperIn();
        String transformation = engines +
                "IN-FILTER: IN.children[?(@.name = 'John')].allowance > '8'," +
                "OUT._id <- IN._id;";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testTranspose() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._id <- IN.children[*].name," +
                "OUT.parents <- LIST(" +
                "                 OBJECT(" +
                "                   name <- IN._id," +
                "                   allowance <- IN.children[@].allowance" +
                "                 )" +
                "               );";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testAllowanceAggregate() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._id <- IN.children[*].name," +
                "OUT.taschengeld <- AVG(IN.children[@].allowance);";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testValueAggregate() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._id <- IN.children[*].allowance," +
                "OUT.$(IN.children[@].name) <- IMPLODE(IN._id);";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testBracedValueAggregate() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._id <- IN.children[*].allowance," +
                "OUT.$(IN.children[@].name) <- (IMPLODE(IN._id));";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testSimpleValueAggregate() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._id <- IN.children[*].allowance," +
                "OUT.count <- COUNT();";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testSimpleAvg() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._id <- 'all'," +
                "OUT.avg <- AVG(IN.info.born);";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testSimpleMax() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._id <- 'all'," +
                "OUT.avg <- MAX(IN.info.born);";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testRowSplitAggregate() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._id <- IN.info.school.split(' ')[*]," +
                "OUT.child <- IMPLODE(IN._id);";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testRowSplitAggregateList() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._id <- IN.info.school.split(' ')[*]," +
                "OUT.child <- LIST(IN._id);";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testAttrSplitAggregate() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._id <- IN._id," +
                "OUT.$(IN.info.school.split(' ')[*]) <- COUNT();";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testMaximalAggregate() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._id <- 'alles',\n" +
                "OUT.$(IN.info.school.split(' ')[*]) <- COUNT();";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testSimpleArithmetics() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._id <- 'alles',\n" +
                "OUT.sum <- ('5' + '7') / '2';";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testLeftAggregateArithmetics() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._id <- 'alles',\n" +
                "OUT.sum <- SUM(IN.info.born) + '7';";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testRightAggregateArithmetics() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._id <- 'alles',\n" +
                "OUT.sum <- '7' + SUM(IN.info.born);";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testHash() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._id <- HASH(IN._id)," +
                "OUT.$(IN.*.name()) <- IN.@;";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testObjectId() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._id <- OBJECT_ID()," +
                "OUT.child <- IN.children[*].name," +
                "OUT.parents <- LIST(" +
                "                 OBJECT(" +
                "                   name <- IN._id," +
                "                   allowance <- IN.children[@].allowance" +
                "                 )" +
                "               );";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testIgnoredTest() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._id <- OBJECT_ID()," +
                "OUT._ <- IN.children[*].name," +
                "OUT.parents <- LIST(" +
                "                 OBJECT(" +
                "                   name <- IN._id," +
                "                   allowance <- IN.children[@].allowance" +
                "                 )" +
                "               );";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testPageRank() throws Exception {
        createGraphIn();
        String transformation = engines +
                "OUT._id <- IN.edges[*].neighbour,\n" +
                "OUT.pr <- SUM(IN.pr/(LIST_COUNT(IN.edges)));";

        NotaQL.evaluate(transformation);
    }

    @Before
    public void setUp() throws Exception {
        engines = "IN-ENGINE: mongodb(database_name <- 'unittests', collection_name <- '"+ inCol +"')," +
                "OUT-ENGINE: mongodb(database_name <- 'unittests', collection_name <- '"+ outCol +"'),";

        NotaQL.loadConfig("settings.config");

        String host = NotaQL.prop.getProperty("mongodb_host");

        if(host == null)
            host = "localhost";

        mongoClient = new MongoClient(host);
        db = mongoClient.getDB("unittests");
    }

    public void printOut() {
        System.out.println("================= OUT ==================");
        if(!db.collectionExists(outCol)) {
            System.out.println("Collection does not exists!");
        }
        final DBCursor dbObjects = db.getCollection(outCol).find();
        for (DBObject dbObject : dbObjects) {
            System.out.println(dbObject.toString());
        }
    }

    @After
    public void tearDown() throws Exception {
        printOut();

        deleteCollection(NotaQLMongoDBTest.inCol);
        deleteCollection(NotaQLMongoDBTest.outCol);
    }

    public void createGraphIn() throws IOException {
        // Make sure the store is clean
        try {
            deleteCollection(NotaQLMongoDBTest.inCol);
            deleteCollection(NotaQLMongoDBTest.outCol);
        } catch(Exception ignored) {
        }

        // create table
        final DBCollection inCol = db.createCollection(NotaQLMongoDBTest.inCol, new BasicDBObject());

        // create data
        final List<Node> nodes = Node.generateNodes();

        for (Node node : nodes) {
            final BasicDBObject object = new BasicDBObject();
            object.put("_id", node.getId());
            object.put("pr", 0.2);

            final BasicDBList edges = new BasicDBList();
            object.put("edges", edges);

            for (Map.Entry<String, String> entry : node.entrySet()) {
                final BasicDBObject edge = new BasicDBObject();
                edge.put("neighbour", entry.getKey());
                edge.put("weight", Double.parseDouble(entry.getValue()));

                edges.add(edge);
            }

            inCol.insert(object, WriteConcern.NORMAL);
        }
    }

    public void createPaperIn() throws IOException {
        // Make sure the store is clean
        try {
            deleteCollection(NotaQLMongoDBTest.inCol);
            deleteCollection(NotaQLMongoDBTest.outCol);
        } catch(Exception ignored) {
        }

        // create table
        final DBCollection inCol = db.createCollection(NotaQLMongoDBTest.inCol, new BasicDBObject());

        // create data
        final List<Person> persons = Person.generatePersons();

        for (Person person : persons) {
            final BasicDBObject object = new BasicDBObject();
            object.put("_id", person.getName());

            final BasicDBObject infoObject = new BasicDBObject();
            object.put("info", infoObject);

            infoObject.put("born", Integer.parseInt(person.getBorn()));
            if(person.getSchool() != null)
                infoObject.put("school", person.getSchool());
            else {
                infoObject.put("cmpny", person.getCmpny());
                infoObject.put("salary", Integer.parseInt(person.getSalary()));

                final BasicDBList children = new BasicDBList();
                object.put("children", children);

                for (Map.Entry<String, String> entry : person.getChildren().entrySet()) {
                    final BasicDBObject child = new BasicDBObject();
                    child.put("name", entry.getKey());
                    child.put("allowance", Integer.parseInt(entry.getValue()));

                    children.add(child);
                }
            }

            inCol.insert(object, WriteConcern.NORMAL);
        }
    }

    private void deleteCollection(String name) throws IOException {
        DBCollection coll = db.getCollection(name);
        coll.drop();
    }

}