package noobgam.me.mongo.codec;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.util.Objects;
import java.util.Set;

public final class Pojo {
    @BsonId
    final ObjectId id;

    final String name;
    final Set<ObjectId> friends;

    @BsonCreator
    public Pojo(
            @BsonId ObjectId id,
            @BsonProperty("name") String name,
            @BsonProperty("friends") Set<ObjectId> friends
    ) {
        this.id = id;
        this.name = name;
        this.friends = friends;
    }

    public ObjectId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<ObjectId> getFriends() {
        return friends;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pojo pojo = (Pojo) o;
        return Objects.equals(id, pojo.id) &&
                Objects.equals(name, pojo.name) &&
                Objects.equals(friends, pojo.friends);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, friends);
    }
}
