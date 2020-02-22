package noobgam.me.maps;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class ReadOnlyMapBenchmark {

    public static class Queries {

        public final int keys[];
        public final int values[];
        public final int queries[];

        public Queries(int generatorId, int size) {

            if (generatorId == 0) {
                // fully random
                keys = ThreadLocalRandom.current().ints(size).toArray();
                values = ThreadLocalRandom.current().ints(size).toArray();
                queries = ThreadLocalRandom.current().ints(5 * size).toArray();
            } else if (generatorId == 1) {
                // 100% hit-rate
                keys = ThreadLocalRandom.current().ints(size).toArray();
                values = ThreadLocalRandom.current().ints(size).toArray();
                ArrayList<Integer> qqq = new ArrayList<>(5 * size);
                for (int i = 0; i < size; ++i) {
                    for (int j = 0; j < 5; ++j) {
                        qqq.add(keys[i]);
                    }
                }
                Collections.shuffle(qqq);
                queries = qqq.stream().mapToInt(x -> x).toArray();
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    @State(Scope.Thread)
    public static abstract class AbstractState {
        @Param({"0.2", "0.5", "0.9"})
        public float loadFactor;

        @Param({"10", "1000", "10000000"})
        public int size;

        @Param({"0", "1"})
        public int generatorId;

        public Queries queries;

        @Setup(Level.Iteration)
        public void setup() {
            queries = new Queries(generatorId, size);
            createAndFillMap();
        }

        public abstract void createAndFillMap();
    }

    @State(Scope.Thread)
    public static class JDKState extends AbstractState {

        public HashMap<Integer, Integer> hashMap;

        @Override
        public void createAndFillMap() {
            int mapSize = (int)((size / loadFactor) + 1.0F);
            hashMap = new HashMap<>(mapSize, loadFactor);
            for (int i = 0; i < size; ++i) {
                hashMap.put(queries.keys[i], queries.values[i]);
            }
        }
    }

    @State(Scope.Thread)
    public static class FastUtilState extends AbstractState {

        public Int2IntOpenHashMap openHashMap;

        @Override
        public void createAndFillMap() {
            openHashMap = new Int2IntOpenHashMap(size, loadFactor);
            for (int i = 0; i < size; ++i) {
                openHashMap.put(queries.keys[i], queries.values[i]);
            }
        }
    }

    @State(Scope.Thread)
    public static class RobinHoodState extends AbstractState {

        public RobinHoodInt2IntOpenHashMap robinHoodMap;

        @Override
        public void createAndFillMap() {
            robinHoodMap = new RobinHoodInt2IntOpenHashMap(size, loadFactor);
            for (int i = 0; i < size; ++i) {
                robinHoodMap.put(queries.keys[i], queries.values[i]);
            }
        }
    }

    // sanity check, should underperform all the time.
    @Benchmark
    public void javaCollections(
            JDKState state,
            Blackhole blackhole
    ) {
        for (int query : state.queries.queries) {
            blackhole.consume(state.hashMap.get(query));
        }
    }

    @Benchmark
    public void fastutil(
            FastUtilState state,
            Blackhole blackhole
    ) {
        for (int query : state.queries.queries) {
            int res = state.openHashMap.get(query);
            // box to attempt to emulate badness of jdk collections.
            if (res == 0) {
                blackhole.consume(null);
            } else {
                blackhole.consume(Integer.valueOf(res));
            }
        }
    }

    @Benchmark
    public void robinHood(
            RobinHoodState state,
            Blackhole blackhole
    ) {
        for (int query : state.queries.queries) {
            blackhole.consume(state.robinHoodMap.get(query));
        }
    }
}

