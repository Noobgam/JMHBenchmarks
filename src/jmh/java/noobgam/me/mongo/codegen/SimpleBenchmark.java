package noobgam.me.mongo.codegen;

import com.mongodb.MongoClientSettings;
import javassist.ClassPool;
import org.bson.*;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.io.BasicOutputBuffer;
import org.bson.io.ByteBufferBsonInput;
import org.bson.types.ObjectId;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Random;

@State(Scope.Benchmark)
public class SimpleBenchmark {

    public static final CodecRegistry CODEC_REGISTRY =
            CodecRegistries.fromRegistries(
                    MongoClientSettings.getDefaultCodecRegistry(),
                    CodecRegistries.fromProviders(
                            PojoCodecProvider.builder()
                                    .register(IntWrapper.class).build()
                    )
            );

    public static final Codec<IntWrapper> MONGO_CODEC = CODEC_REGISTRY.get(IntWrapper.class);
    public static final Codec<IntWrapper> CUSTOM_CODEC;
    private static final EncoderContext ENCODER_CONTEXT = EncoderContext.builder().build();
    private static final DecoderContext DECODER_CONTEXT = DecoderContext.builder().build();

    static {
        try {
            CUSTOM_CODEC = new CodecGenerator(ClassPool.getDefault()).generateCodec(IntWrapper.class).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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
            IntWrapper IntWrapper = new IntWrapper(r.nextInt());
            byte[] bytes1 = convertToBytes(IntWrapper, MONGO_CODEC);
            byte[] bytes2 = convertToBytes(IntWrapper, CUSTOM_CODEC);
            IntWrapper IntWrapper12 = readFromRawBytes(bytes1, MONGO_CODEC);
            IntWrapper IntWrapper22 = readFromRawBytes(bytes2, MONGO_CODEC);
            IntWrapper IntWrapper11 = readFromRawBytes(bytes1, CUSTOM_CODEC);
            IntWrapper IntWrapper21 = readFromRawBytes(bytes2, CUSTOM_CODEC);
            if (!(IntWrapper11.equals(IntWrapper12) && IntWrapper12.equals(IntWrapper21) && IntWrapper21.equals(IntWrapper22))) {
                throw new IllegalArgumentException("Could not parse");
            }
            data[i] = bytes1;
        }
    }

    public static IntWrapper readFromRawBytes(byte[] rawBytes, Codec<IntWrapper> codec) {
        ByteBuf bytes = new ByteBufNIO(ByteBuffer.wrap(rawBytes));
        ByteBufferBsonInput buffer = new ByteBufferBsonInput(bytes);
        BsonBinaryReader reader = new BsonBinaryReader(buffer);
        return codec.decode(reader, DECODER_CONTEXT);
    }

    public static byte[] convertToBytes(IntWrapper IntWrapper, Codec<IntWrapper> codec) {
        BasicOutputBuffer buffer = new BasicOutputBuffer();
        BsonBinaryWriter writer = new BsonBinaryWriter(buffer);
        codec.encode(
                writer,
                IntWrapper,
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
                .include(".*" + SimpleBenchmark.class.getSimpleName() + ".*")
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