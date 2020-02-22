package noobgam.me.mongo;

import com.mongodb.MongoClientSettings;
import noobgam.me.mongo.codec.Pojo;
import noobgam.me.mongo.codec.SelfMadeCodec;
import noobgam.me.mongo.codec.SelfMadeCodecOptimized;
import org.bson.*;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.io.BasicOutputBuffer;
import org.bson.io.BsonInput;
import org.bson.io.ByteBufferBsonInput;
import org.bson.types.ObjectId;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@State(Scope.Benchmark)
public class MongoCodecBenchmark {

    private static final CodecRegistry CODEC_REGISTRY =
            CodecRegistries.fromRegistries(
                    MongoClientSettings.getDefaultCodecRegistry(),
                    CodecRegistries.fromProviders(
                            PojoCodecProvider.builder()
                                    .register(Pojo.class).build()
                    )
            );

    private static final Codec<Pojo> MONGO_CODEC = CODEC_REGISTRY.get(Pojo.class);
    private static final Codec<Pojo> CUSTOM_CODEC = new SelfMadeCodec();
    private static final Codec<Pojo> CUSTOM_FAST_CODEC = new SelfMadeCodecOptimized();
    private static final EncoderContext ENCODER_CONTEXT = EncoderContext.builder().build();
    private static final DecoderContext DECODER_CONTEXT = DecoderContext.builder().build();

    @Param({"1000"})
    public int values;

    // array of raw readers
    public byte[][] data;

    @Setup
    public void init() {
        int n = values;
        data = new byte[n][0];
        Random r = new Random();
        for (int i = 0; i < n; ++i) {
            BsonDocument document = new BsonDocument();
            ObjectId id = genId(r);
            String name = generateString(r);
            int cnt = r.nextInt(10);
            HashSet<ObjectId> friends = new HashSet<>();
            for (int j = 0; j < cnt; ++j) {
                friends.add(genId(r));
            }
            Pojo pojo = new Pojo(id, name, friends);
            byte[] bytes1 = convertToBytes(pojo, MONGO_CODEC);
            byte[] bytes2 = convertToBytes(pojo, CUSTOM_CODEC);
            Pojo pojo12 = readFromRawBytes(bytes1, MONGO_CODEC);
            Pojo pojo22 = readFromRawBytes(bytes2, MONGO_CODEC);
            Pojo pojo11 = readFromRawBytes(bytes1, CUSTOM_CODEC);
            Pojo pojo21 = readFromRawBytes(bytes2, CUSTOM_CODEC);
            if (!(pojo11.equals(pojo12) && pojo12.equals(pojo21) && pojo21.equals(pojo22))) {
                throw new IllegalArgumentException("Could not parse");
            }
            data[i] = bytes1;
        }
    }

    public Pojo readFromRawBytes(byte[] rawBytes, Codec<Pojo> codec) {
        ByteBuf bytes = new ByteBufNIO(ByteBuffer.wrap(rawBytes));
        ByteBufferBsonInput buffer = new ByteBufferBsonInput(bytes);
        BsonBinaryReader reader = new BsonBinaryReader(buffer);
        return codec.decode(reader, DECODER_CONTEXT);
    }

    public byte[] convertToBytes(Pojo pojo, Codec<Pojo> codec) {
        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonBinaryWriter writer = new BsonBinaryWriter(buffer);
        codec.encode(
                writer,
                pojo,
                ENCODER_CONTEXT
        );
        return buffer.toByteArray();
    }

    public ObjectId genId(Random r) {
        return new ObjectId(r.nextInt(), r.nextInt(16777215));
    }

    private static final char[] CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGJKLMNPRSTUVWXYZ0123456789".toCharArray();

    public static String generateString(Random r) {

        int len = r.nextInt(5) + 5;
        StringBuilder builder = new StringBuilder(len);

        for (int i = 0; i < len; ++i) {
            builder.append(CHARS[r.nextInt(CHARS.length)]);
        }

        return builder.toString();
    }

    @Benchmark
    public void customOptimized(Blackhole blackhole) {
        for (int i = 0; i < values; ++i) {
            blackhole.consume(readFromRawBytes(data[i], CUSTOM_FAST_CODEC));
        }
    }

    @Benchmark
    public void custom(Blackhole blackhole) {
        for (int i = 0; i < values; ++i) {
            blackhole.consume(readFromRawBytes(data[i], CUSTOM_CODEC));
        }
    }

    @Benchmark
    public void mongo(Blackhole blackhole) {
        for (int i = 0; i < values; ++i) {
            blackhole.consume(readFromRawBytes(data[i], MONGO_CODEC));
        }
    }

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(".*" + MongoCodecBenchmark.class.getSimpleName() + ".*")
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