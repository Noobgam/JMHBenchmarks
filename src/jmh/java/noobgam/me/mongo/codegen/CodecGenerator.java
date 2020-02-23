package noobgam.me.mongo.codegen;

import javassist.*;
import javassist.bytecode.SignatureAttribute;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.ObjectId;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Generates mongo codec, inlines every codec call into switch case
 */
public class CodecGenerator {
    private final ClassPool pool;

    public CodecGenerator(ClassPool pool) {
        this.pool = pool;
    }

    public <T> Class<? extends Codec<T>> generateCodec(Class<T> pojoClazz) {
        try {
            CtClass cc = prepareCtClass(pojoClazz);

            return (Class<? extends Codec<T>>)cc.toClass();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> byte[] generateBytes(Class<T> pojoClazz) {
        try {
            CtClass cc = prepareCtClass(pojoClazz);

            return cc.toBytecode();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> CtClass prepareCtClass(Class<T> pojoClazz) throws NotFoundException, CannotCompileException {
        CtClass cc = pool.makeClass(pojoClazz.getSimpleName() + "Codec");
        CtClass codec = pool.get("org.bson.codecs.Codec");
        SignatureAttribute.ClassSignature cs = new SignatureAttribute.ClassSignature(null, null,
                // Set interface and its generic params
                new SignatureAttribute.ClassType[]{new SignatureAttribute.ClassType("org.bson.codecs.Codec",
                        new SignatureAttribute.TypeArgument[]{
                                new SignatureAttribute.TypeArgument(new SignatureAttribute.ClassType(pojoClazz.getName()))
                })});
        cc.setInterfaces(new CtClass[]{codec});
        cc.setGenericSignature(cs.encode());
        Field[] fields = pojoClazz.getDeclaredFields();
        pool.importPackage(pojoClazz.getName());
        pool.importPackage(DecoderContext.class.getName());
        pool.importPackage(EncoderContext.class.getName());
        pool.importPackage(BsonReader.class.getName());
        pool.importPackage(BsonWriter.class.getName());
        pool.importPackage(BsonType.class.getName());
        for (Field field : fields) {
            pool.importPackage(field.getType().getName());
        }
        {
            CtMethod m = CtNewMethod.make(
                    getDecodeSourceMethod(pojoClazz, fields),
                    cc
            );
            cc.addMethod(m);
        }
        {
            CtMethod m = CtNewMethod.make(
                    getEncodeSourceMethod(pojoClazz, fields),
                    cc
            );
            cc.addMethod(m);
        }
        {
            CtMethod m = CtNewMethod.make(
                    "public Class getEncoderClass() { return " + pojoClazz.getSimpleName() + ".class; }",
                    cc
            );
            cc.addMethod(m);
        }
        return cc;
    }

    public static String getDecodeSourceMethod(Class<?> clazz) {
        return getDecodeSourceMethod(clazz, clazz.getDeclaredFields());
    }

    public static String getEncodeSourceMethod(Class<?> clazz) {
        return getEncodeSourceMethod(clazz, clazz.getDeclaredFields());
    }

    public static String getDecodeSourceMethod(Class<?> clazz, Field[] fields) {
        return "public Object decode(BsonReader reader, DecoderContext decoderContext) { " +
                    Arrays.stream(fields)
                        .map(field -> field.getType().getName() + " " + field.getName() + " = 0;").collect(Collectors.joining()) +
                    "reader.readStartDocument();" +
                    "while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {" +
                        "String fieldName = reader.readName();" +
                        "switch (fieldName) {" +
                            Arrays.stream(fields)
                                    //TODO: proper property name extracted from the constructor
                                .map(field -> generateOneCase(field, field.getName()))
                                .collect(Collectors.joining()) +
                            "default: reader.skipValue(); break;" +
                         "}" +
                    "}" +
                    "reader.readEndDocument();" +
                    "if (" + Arrays.stream(fields).map(field -> field.getName() + " == 0")
                        .collect(Collectors.joining(" || ")) + ") throw new IllegalArgumentException(\"Could not parse document\");" +
                    "return new " + clazz.getSimpleName() + "(" + Arrays.stream(fields).map(Field::getName).collect(Collectors.joining(",")) + ");" +
                "}";
    }

    public static String generateOneCase(Field field, String propertyName) {
        return
                "case \"" + propertyName + "\":" +
                        field.getName() + " = " + readField(field) + ";" +
                        "break;";
    }

    public static String readField(Field field) {
        if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
            return "reader.readInt32()";
        } else if (field.getType().equals(ObjectId.class)) {
            return "reader.readObjectId()";
        } else {
            throw new IllegalArgumentException("Unsupported type");
        }
    }

    public static String getEncodeSourceMethod(Class<?> clazz, Field[] fields) {
        String cast = "(" + clazz.getSimpleName() + ")";
        return "public void encode(BsonWriter writer, Object value, EncoderContext encoderContext) { " +
                "writer.writeStartDocument();" +
                Arrays.stream(fields)
                        .map(field -> "writer.writeName(\"" + field.getName() + "\");" + writeField(cast, field)).collect(Collectors.joining()) +
                "writer.writeEndDocument();" +
                "}";
    }

    public static String writeField(String cast, Field field) {
        String getter = "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);

        if (field.getType().equals(int.class)) {
            return "writer.writeInt32((" + cast + "value)." + getter + "());";
        } else if (field.getType().equals(ObjectId.class)) {
            return "writer.writeObjectId(value." + getter + "());";
        } else {
            throw new IllegalArgumentException("Unsupported type");
        }

    }
}
