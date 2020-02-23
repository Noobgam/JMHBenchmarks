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

public final class SelfMadeCodec implements Codec<Pojo> {

    @Override
    public Pojo decode(BsonReader reader, DecoderContext decoderContext) {
        ObjectId id = null;
        String name = null;
        Set<ObjectId> friends = null;
        reader.readStartDocument();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String field = reader.readName();
            switch (field) {
                case "_id":
                    id = reader.readObjectId();
                    break;
                case "name":
                    name = reader.readString();
                    break;
                case "friends":
                    reader.readName();
                    reader.readStartArray();
                    friends = new HashSet<>();
                    while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                        friends.add(reader.readObjectId());
                    }
                    reader.readEndArray();
                    break;
                default: reader.skipValue(); break;
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
