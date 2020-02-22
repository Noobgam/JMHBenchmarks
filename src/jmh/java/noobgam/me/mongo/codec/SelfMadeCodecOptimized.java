package noobgam.me.mongo.codec;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.ObjectId;

import java.util.HashSet;
import java.util.Set;

public final class SelfMadeCodecOptimized implements Codec<Pojo> {

    @Override
    public Pojo decode(BsonReader reader, DecoderContext decoderContext) {
        ObjectId id = null;
        String name = null;
        Set<ObjectId> friends = null;
        reader.readStartDocument();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            // hashcode is ok there, mostly because it is barely prone to errors and is detectable at compile time
            int field = reader.readName().hashCode();
            switch (field) {
                case 94650:
                    id = reader.readObjectId();
                    break;
                case 3373707:
                    name = reader.readString();
                    break;
                case -600094315:
                    reader.readStartArray();
                    friends = new HashSet<>();
                    while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                        friends.add(reader.readObjectId());
                    }
                    reader.readEndArray();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.readEndDocument();
        if (id == null || name == null || friends == null) {
            throw new IllegalArgumentException("Could not parse document");
        }
        return new Pojo(id, name, friends);
    }

    @Override
    public void encode(BsonWriter writer, Pojo value, EncoderContext encoderContext) {
        writer.writeStartDocument();
        writer.writeObjectId("_id", value.id);
        writer.writeStartArray("friends");
        for (ObjectId friend : value.friends) {
            writer.writeObjectId(friend);
        }
        writer.writeEndArray();
        writer.writeString("name", value.name);
        writer.writeEndDocument();
    }

    @Override
    public Class<Pojo> getEncoderClass() {
        return Pojo.class;
    }
}
