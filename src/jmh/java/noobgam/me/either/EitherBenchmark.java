package noobgam.me.either;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.Random;

@State(Scope.Benchmark)
public class EitherBenchmark {

    @Param({"1000000"})
    public int values;

    // array of raw readers
    public int[] dataL;
    public long[] dataR;
    public int[] order;

    @Setup
    public void init() {
        int n = values;
        dataL = new int[n];
        dataR = new long[n];
        order = new int[n];
        Random r = new Random();
        for (int i = 0; i < n; ++i) {
            dataL[i] = r.nextInt();
            dataR[i] = r.nextLong();
            order[i] = r.nextInt(2);
        }
    }

    @Benchmark
    public void eitherBool(Blackhole blackhole) {
        for (int i = 0; i < values; ++i) {
            final EitherBool<Integer, Long> either;
            if (order[i] == 0) {
                either = EitherBool.left(dataL[i]);
                blackhole.consume(either.getLeft());
            } else {
                either = EitherBool.right(dataR[i]);
                blackhole.consume(either.getRight());
            }
            blackhole.consume(either);
            blackhole.consume(either.isLeft());
            blackhole.consume(either.isRight());
        }
    }

    @Benchmark
    public void eitherVirtual(Blackhole blackhole) {
        for (int i = 0; i < values; ++i) {
            final EitherGeneric<Integer, Long> either;
            if (order[i] == 0) {
                either = EitherGeneric.left(dataL[i]);
                blackhole.consume(either.getLeft());
            } else {
                either = EitherGeneric.right(dataR[i]);
                blackhole.consume(either.getRight());
            }
            blackhole.consume(either);
            blackhole.consume(either.isLeft());
            blackhole.consume(either.isRight());
        }
    }

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(".*" + EitherBenchmark.class.getSimpleName() + ".*")
                .forks(0)
                .warmupIterations(1)
                .warmupTime(TimeValue.seconds(1))
                .measurementIterations(1)
                .measurementTime(TimeValue.seconds(1))
                .shouldDoGC(true)
                .build();
        new Runner(options).run();
    }

}
