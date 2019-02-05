import io.reactivex.Completable;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;

import java.util.Collection;
import java.util.stream.Collectors;

class CommentsSaver {

    private final Vertx vertx;

    CommentsSaver(Vertx vertx) {
        this.vertx = vertx;
    }

    Completable createDirectoryForDomain(String path, String domain) {
        return vertx.fileSystem()
                .rxMkdirs(path + domain);
    }

    Completable writeCommentsToFile(String path, String domain, Collection<JsonObject> comments) {
        return vertx.fileSystem()
                .rxWriteFile(path + domain + "/comments.txt",
                        Buffer.buffer(comments.stream()
                                .map(JsonObject::encodePrettily)
                                .collect(Collectors.toList())
                                .toString()));
    }
}
