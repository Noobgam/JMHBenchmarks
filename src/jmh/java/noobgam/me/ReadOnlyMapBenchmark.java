package noobgam.me;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class ReadOnlyMapBenchmark {

    @State(Scope.Thread)
    public static class MyState {
        @Param({"0.5", "0.7"})
        public float loadFactor;

        @Param({"false", "true"})
        public boolean preSize;

        @Param({"10000000", "1000000", "1000", "10"})
        public int size;

        public transient int values[];

        @Setup(Level.Iteration)
        public void setup() {
            values = ThreadLocalRandom.current().ints(2 * size).toArray();
        }
    }

    // sanity check, should underperform all the time.
    @Benchmark
    public void javaCollections(
            MyState state,
            Blackhole blackhole
    ) {
        Map<Integer, Integer> map;
        if (state.preSize) {
            map = new HashMap<>(
                    (int) Math.ceil(state.size / state.loadFactor) + 1,
                    state.loadFactor
            );
        } else {
            map = new HashMap<>(16, state.loadFactor);
        }
        for (int i = 0; i < state.size; ++i) {
            map.put(state.values[2 * i], state.values[2 * i + 1]);
        }
        blackhole.consume(map);
    }

    // sanity check, should underperform all the time.
    @Benchmark
    public void fastutil(
            MyState state,
            Blackhole blackhole
    ) {
        Map<Integer, Integer> map;
        if (state.preSize) {
            map = new Int2IntOpenHashMap(
                    state.size,
                    state.loadFactor
            );
        } else {
            map = new Int2IntOpenHashMap(16, state.loadFactor);
        }
        for (int i = 0; i < state.size; ++i) {
            map.put(state.values[2 * i], state.values[2 * i + 1]);
        }
        blackhole.consume(map);
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