package noobgam.me;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static it.unimi.dsi.fastutil.HashCommon.arraySize;

public class ReadOnlyMapBenchmark {

    static void shuffleArray(int[] ar)
    {
        Random rnd = ThreadLocalRandom.current();
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    @State(Scope.Thread)
    public static class MyState {

        @Param({"false"})
        public boolean robinHood;

        @Param({"0.5"})
        public float loadFactor;

        @Param({"100"})
        public int size;


        Int2IntMap map;
        Map<Integer, Integer> map2;

        int[] queries;

        @Setup(Level.Iteration)
        public void setup() {
            int[] keys
                    = ThreadLocalRandom.current().ints(size).toArray();
            int[] values
                    = ThreadLocalRandom.current().ints(size).toArray();
            queries = Arrays.copyOf(keys, size);
            shuffleArray(queries);
            Int2IntMap map = new Int2IntOpenHashMap(
                    size,
                    loadFactor
            );
            for (int i = 0; i < size; ++i) {
                map.put(keys[i], values[i]);
            }

            this.map2 = this.map = map;
        }
    }

    @Benchmark
    public void fastUtilInteger(
            MyState state,
            Blackhole blackhole
    ) {
        for (int query : state.queries) {
            Integer res = state.map2.get(query);
            blackhole.consume(res);
            if (res == null) {
                blackhole.consume(state.map2);
            }
        }
    }

    @Benchmark
    public void fastUtilInt(
            MyState state,
            Blackhole blackhole
    ) {
        final int defretval = state.map.defaultReturnValue();
        for (int query : state.queries) {
            int res = state.map.get(query);
            blackhole.consume(res);
            if (defretval == res) {
                blackhole.consume(state.map);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(".*" + ReadOnlyMapBenchmark.class.getSimpleName() + ".*")
                .forks(1)
                .warmupIterations(1)
                .warmupTime(TimeValue.seconds(1))
                .measurementIterations(1)
                .measurementTime(TimeValue.seconds(1))
                .shouldDoGC(true)
                .build();
        new Runner(options).run();
    }

}