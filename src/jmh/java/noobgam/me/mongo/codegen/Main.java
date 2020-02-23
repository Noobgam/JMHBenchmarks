package noobgam.me.mongo.codegen;

import javassist.ClassPool;

import java.io.FileOutputStream;

public class Main {
    public static void main(String[] args) throws Exception {
        var gen = new CodecGenerator(ClassPool.getDefault());
        if (false) {
            System.out.println(CodecGenerator.getEncodeSourceMethod(IntWrapper.class));
            byte[] bts = gen.generateBytes(IntWrapper.class);
            try (var file = new FileOutputStream("IntWrapperCodec.class")) {
                file.write(bts);
            }
        }
        IntWrapper wrapper = new IntWrapper(1);
        byte[] bytes = SimpleBenchmark.convertToBytes(wrapper, new IWC());
        SimpleBenchmark.readFromRawBytes(bytes, SimpleBenchmark.CUSTOM_CODEC);

    }
}
