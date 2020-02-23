package noobgam.me.mongo.codegen;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public class IWC implements Codec<IntWrapper> {
    public IntWrapper decode(BsonReader var1, DecoderContext var2) {
        int var3 = 0;
        var1.readStartDocument();

        while(true) {
            while(var1.readBsonType() != BsonType.END_OF_DOCUMENT) {
                String var4 = var1.readName();
                switch(var4.hashCode()) {
                    case 3355:
                        if (var4.equals("id")) {
                            var3 = var1.readInt32();
                            break;
                        }
                    default:
                        var1.skipValue();
                }
            }

            var1.readEndDocument();
            if (var3 == 0) {
                throw new IllegalArgumentException("Could not parse document");
            }

            return new IntWrapper(var3);
        }
    }

    public void encode(BsonWriter var1, IntWrapper var2, EncoderContext var3) {
        var1.writeStartDocument();
        var1.writeName("id");
        var1.writeInt32(((IntWrapper)var2).getId());
        var1.writeEndDocument();
    }

    public Class<IntWrapper> getEncoderClass() {
        return IntWrapper.class;
    }

    public IWC() {
    }
}
