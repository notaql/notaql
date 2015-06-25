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

import notaql.engines.redis.datamodel.ValueConverter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class NotaQLRedisTest {
    public static final int dbInId = 11;
    public static final int dbOutId = 12;

    public static String engines;
    private Jedis jedis;

    @Test
    public void testIdentity() throws Exception {
        createGraphIn();
        String transformation = engines +
                "OUT._k <- IN._k," +
                "OUT._v <- IN._v;";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testInFilter() throws Exception {
        createGraphIn();
        String transformation = engines +
                "IN-FILTER: IN._v.* > 2," +
                "OUT._k <- IN._k," +
                "OUT._v <- IN._v;";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testTranspose() throws Exception {
        createGraphIn();
        String transformation = engines +
                "OUT._k <- IN._v.*.name()," +
                "OUT.$(IN._k) <- IN._v.@;";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testAggregate() throws Exception {
        createGraphIn();
        String transformation = engines +
                "OUT._k <- IN._k," +
                "OUT.sum <- SUM(IN._v.*);";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testRowSplitAggregate() throws Exception {
        createSplitIn();
        String transformation = engines +
                "OUT._k <- IN._v.split(' ')," +
                "OUT._v <- COUNT();";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testMaximalAggregate() throws Exception {
        createGraphIn();
        String transformation = engines +
                "OUT._k <- 'alles',\n" +
                "OUT.sum <- SUM(IN._v.*);";

        NotaQL.evaluate(transformation);
    }

    @Test
    public void testPageRank() throws Exception {
        createGraphIn();
        String transformation = engines +
                "OUT._k <- IN._v.*.name(),\n" +
                "OUT.pr <- SUM(0.2/(LIST_COUNT(IN._v)));";

        NotaQL.evaluate(transformation);
    }

    @Before
    public void setUp() throws Exception {
        engines = "IN-ENGINE: redis(database_id <- '"+ dbInId +"'),OUT-ENGINE: redis(database_id <- '"+ dbOutId +"'),";

        NotaQL.loadConfig("settings.config");

        jedis = new Jedis(NotaQL.prop.getProperty("redis_host", "localhost"));

        jedis.select(dbInId);
    }

    @After
    public void tearDown() throws Exception {
        printOut();

        jedis.flushDB();
        jedis.select(dbOutId);
        jedis.flushDB();
        jedis.close();
    }

    public void printOut() {
        System.out.println("================= OUT ==================");
        final ScanResult<String> scan = jedis.scan("0");

        final ValueConverter converter = new ValueConverter(NotaQL.prop.getProperty("redis_host", "localhost"), dbOutId);

        for (String s : scan.getResult()) {
            System.out.println(s + ": " + converter.readFromRedis(s));
        }
    }

    public void createGraphIn() throws IOException {
        // Make sure the store is clean
        try {
            deleteCollection(dbInId);
            deleteCollection(dbOutId);
        } catch(Exception ignored) {
        }

        jedis.select(dbInId);

        // create data
        final List<Node> nodes = Node.generateNodes();

        for (Node node : nodes) {
            for (Map.Entry<String, String> entry : node.entrySet()) {
                jedis.hset(node.getId(), entry.getKey(), entry.getValue());
            }
        }
    }

    public void createSplitIn() throws IOException {
        // Make sure the store is clean
        try {
            deleteCollection(dbInId);
            deleteCollection(dbOutId);
        } catch(Exception ignored) {
        }

        jedis.select(dbInId);

        jedis.set("test", "hallo das ist ein hallo welt test");
    }

    private void deleteCollection(int id) throws IOException {
        jedis.select(id);
        jedis.flushDB();
    }

}