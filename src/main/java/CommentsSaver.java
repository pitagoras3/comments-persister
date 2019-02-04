import io.reactivex.Completable;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;

import java.util.Collection;

class CommentsSaver {

    private static final String COMMENTS_PATH = "comments/";

    private final Vertx vertx;

    CommentsSaver(Vertx vertx) {
        this.vertx = vertx;
    }

    Completable createDirectoryForDomain(String domain) {
        return vertx.fileSystem()
                .rxMkdirs(COMMENTS_PATH + domain);
    }

    Completable writeCommentsToFile(String domain, Collection<JsonObject> comments) {
        return vertx.fileSystem()
                .rxWriteFile(COMMENTS_PATH + domain + "/comments.txt",
                        Buffer.buffer(comments.toString()));
    }
}
