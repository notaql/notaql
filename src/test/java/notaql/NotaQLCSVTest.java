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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableNotEnabledException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class NotaQLCSVTest {
    public static final String inTable = "NotaQLTestIn";
    public static final String outTable = "NotaQLTestOut";

    public static String engines;

    private Configuration conf;
    private HBaseAdmin admin;

    @Test
    public void testIdentity() throws Exception {
        createPaperIn();
        String transformation = engines +
                "IN-ENGINE: csv('src/main/resources/sample.csv'),\n" +
                "OUT-ENGINE: csv('src/main/resources/testout.csv'),\n" +
                "OUT.$(IN._c) <- IN._v;";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testColCount() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._r <- IN._r," +
                "OUT.count <- COL_COUNT();";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testFamilyPredicate() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._r <- IN._r," +
                "OUT.$(IN.children:_c) <- IN._v;";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testCellPredicate() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._r <- IN._r," +
                "OUT.$(IN._c?(@ > 8)) <- IN._v;";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testRowPredicate() throws Exception {
        createPaperIn();
        String transformation = engines +
                "IN-FILTER: IN.Susi > 8," +
                "OUT._r <- IN._r," +
                "OUT.$(IN._c) <- IN._v;";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testFamily() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._r <- IN._r," +
                "OUT.stuff:$(IN._c) <- IN._v;";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testColumnTranspose() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._r <- IN.children:_c," +
                "OUT.parents:$(IN._r) <- IN._v;";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testColumnAggregate() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._r <- IN.children:_c," +
                "OUT.aggr:taschengeld <- SUM(IN._v);";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testValueAggregate() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._r <- IN.children:_v," +
                "OUT.$(IN._c) <- IMPLODE(IN._r);";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testVDataAggregate() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._r <- IN.children:_v," +
                "OUT.$(IN.Susi) <- IMPLODE(IN._r);";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testRowSplitAggregate() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._r <- IN.school.split(' ')," +
                "OUT.child <- IMPLODE(IN._r);";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testColSplitAggregate() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._r <- IN._r," +
                "OUT.$(IN.school.split(' ')) <- COUNT();";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testMaximalAggregate() throws Exception {
        createPaperIn();
        String transformation = engines +
                "OUT._r <- 'alles',\n" +
                "OUT.$(IN.school.split(' ')) <- COUNT();";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testPageRank() throws Exception {
        createGraphIn();
        String transformation = engines +
                "OUT._r <- IN.edges:_c,\n" +
                "OUT.alg:pr <- SUM(IN.pr/(COL_COUNT('edges')));";

        NotaQL.evaluate(transformation);
    }

    @Before
    public void setUp() throws Exception {
        engines = "IN-ENGINE: hbase(csv_path <- '"+inTable +"'),OUT-ENGINE: hbase(csv_path <- '"+outTable +"'),";

        this.conf = HBaseConfiguration.create();

        NotaQL.loadConfig("settings.config");

        String host = NotaQL.prop.getProperty("hbase_host");

        if(host == null)
            host = "localhost";

        conf.set("hbase.zookeeper.quorum", host);
        conf.set("hbase.zookeeper.property.clientPort","2181");
        conf.set("hbase.master", host + ":60000");
    }

    @After
    public void tearDown() throws Exception {
        deleteTable(NotaQLCSVTest.inTable);
        deleteTable(NotaQLCSVTest.outTable);
    }

    public void createGraphIn() throws IOException {
        this.admin = new HBaseAdmin(conf);

        // Make sure the store is clean
        try {
            deleteTable(NotaQLCSVTest.inTable);
            deleteTable(NotaQLCSVTest.outTable);
        } catch(Exception ignored) {
        }

        // create table
        HTableDescriptor descriptor = new HTableDescriptor(NotaQLCSVTest.inTable);
        descriptor.addFamily(new HColumnDescriptor("edges"));
        descriptor.addFamily(new HColumnDescriptor("alg"));
        admin.createTable(descriptor);

        // create data
        final List<Node> nodes = Node.generateNodes();

        HTable table = new HTable(conf, NotaQLCSVTest.inTable);

        for (Node node : nodes) {
            Put put = new Put(Bytes.toBytes(node.getId()));
            put.add(Bytes.toBytes("alg"), Bytes.toBytes("pr"), Bytes.toBytes("0.2"));

            for (Map.Entry<String, String> edge : node.entrySet()) {
                put.add(Bytes.toBytes("edges"), Bytes.toBytes(edge.getKey()), Bytes.toBytes(edge.getValue()));
            }

            table.put(put);
        }

        table.flushCommits();
        table.close();
    }

    public void createPaperIn() throws IOException {
        this.admin = new HBaseAdmin(conf);

        // Make sure the store is clean
        try {
            deleteTable(NotaQLCSVTest.inTable);
            deleteTable(NotaQLCSVTest.outTable);
        } catch(Exception ignored) {
        }

        // create table
        HTableDescriptor descriptor = new HTableDescriptor(NotaQLCSVTest.inTable);
        descriptor.addFamily(new HColumnDescriptor("info"));
        descriptor.addFamily(new HColumnDescriptor("children"));
        admin.createTable(descriptor);

        // create data
        final List<Person> persons = Person.generatePersons();

        HTable table = new HTable(conf, NotaQLCSVTest.inTable);

        for (Person person : persons) {
            Put put = new Put(Bytes.toBytes(person.getName()));
            put.add(Bytes.toBytes("info"), Bytes.toBytes("born"), Bytes.toBytes(person.getBorn()));
            if(person.getSchool() != null)
                put.add(Bytes.toBytes("info"), Bytes.toBytes("school"), Bytes.toBytes(person.getSchool()));
            else {
                put.add(Bytes.toBytes("info"), Bytes.toBytes("cmpny"), Bytes.toBytes(person.getCmpny()));
                put.add(Bytes.toBytes("info"), Bytes.toBytes("salary"), Bytes.toBytes(person.getSalary()));
                for (Map.Entry<String,String> child : person.getChildren().entrySet()) {
                    put.add(Bytes.toBytes("children"), Bytes.toBytes(child.getKey()), Bytes.toBytes(child.getValue()));
                }
            }

            table.put(put);
        }

        table.flushCommits();
        table.close();
    }

    private void deleteTable(String name) throws IOException {
        try {
            this.admin.disableTable(name);
        } catch(TableNotEnabledException ignored) {

        }
        this.admin.deleteTable(name);
    }

}