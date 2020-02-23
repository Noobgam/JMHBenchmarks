package noobgam.me.mongo.codegen;

import org.bson.codecs.pojo.annotations.BsonId;


public class IntWrapper {
    @BsonId
    private final int id;

    public IntWrapper(
            @BsonId int id
    ) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
