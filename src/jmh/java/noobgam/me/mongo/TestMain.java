package noobgam.me.mongo;

import org.openjdk.jmh.infra.Blackhole;

public class TestMain {
    public static void main(String[] args) {
        MongoCodecBenchmark test = new MongoCodecBenchmark();
        test.values = 100;
        test.init();
        test.mongo(new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous."));
    }
}
