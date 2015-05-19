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

package notaql.engines.redis.datamodel;

import notaql.datamodel.*;
import notaql.model.EvaluationException;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Thomas Lottermann on 06.12.14.
 */
public class ValueConverter {
    public static Value readFromRedis(Jedis jedis, String key) {
        // get the type
        final String type = jedis.type(key);

        if(type.equals("string")) {
            return ValueUtils.parse(jedis.get(key));
        }
        if(type.equals("list")) {
            final List<String> list = jedis.lrange(key, 0, jedis.llen(key));
            final ListValue listValue = new ListValue();

            list.forEach(e -> listValue.add(ValueUtils.parse(e)));

            return listValue;
        }
        if(type.equals("set")) {
            // TODO: we might want to support a real set type?
            final Set<String> set = jedis.smembers(key);
            final ListValue listValue = new ListValue();

            set.forEach(e -> listValue.add(ValueUtils.parse(e)));

            return listValue;
        }
        if(type.equals("hash")) {
            final Map<String, String> map = jedis.hgetAll(key);
            final ObjectValue object = new ObjectValue();

            map.forEach((k,v) -> object.put(new Step<>(k), ValueUtils.parse(v)));

            return object;
        }

        throw new EvaluationException("Unsupported type read: " + type);
    }

    public static void writeToRedis(Jedis jedis, ObjectValue o) {
        final Value id = o.get(new Step<>("_id"));
        final Value v = o.get(new Step<>("_v"));
        assert id != null && id instanceof AtomValue<?> && v != null;

        final String k = ((AtomValue<?>)id).getValue().toString();

        // atom values
        if(v instanceof NullValue) {
            return;
        }
        if(v instanceof AtomValue<?>) {
            final String s = ((AtomValue<?>) v).getValue().toString();
            jedis.set(k, s);
            return;
        }

        // complex values
        if(v instanceof ListValue) {
            final List<String> strings = ((ListValue) v)
                    .stream()
                    .filter(m -> m instanceof AtomValue<?> && !(m instanceof NullValue))
                    .map(m -> ((AtomValue<?>) m).getValue().toString()).collect(Collectors.toList());
            jedis.lpush(k, strings.toArray(new String[strings.size()]));
            return;
        }
        if(v instanceof ObjectValue) {
            ((ObjectValue) v)
                    .toMap()
                    .entrySet()
                    .stream()
                    .filter(e -> (e.getValue() instanceof AtomValue<?> && !(e.getValue() instanceof NullValue)))
                    .forEach(
                            e -> jedis.hset(
                                    k,
                                    e.getKey().getStep(),
                                    ((AtomValue<?>) e.getValue()).getValue().toString()
                            )
                    );
            return;
        }

        throw new EvaluationException("Unsupported type written: " + v.getClass() + ": " + v.toString());
    }
}
