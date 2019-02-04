import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CommentsReceiver {

    private static final String COMMENT_API_URL = "https://jsonplaceholder.typicode.com/comments";
    private static final String COMMENTS_PATH = "comments/";

    private final Vertx vertx;
    private final WebClient webClient;

    public CommentsReceiver(Vertx vertx, WebClient webClient) {
        this.vertx = vertx;
        this.webClient = webClient;
    }

    public Observable<JsonObject> getComments(int amountOfComments) {
        return Observable.range(1, amountOfComments)
                .flatMapSingle(id -> webClient.getAbs(COMMENT_API_URL)
                        .addQueryParam("postId", String.valueOf(id))
                        .rxSend())
                .map(HttpResponse::bodyAsJsonArray)
                .map(x -> (List<Map>)x.getList())
                .flatMapIterable(x -> x)
                .map(JsonObject::new);
    }

    public Single<Map<String, Collection<JsonObject>>> groupCommentsByDomain(Observable<JsonObject> comments) {
        return comments.toMultimap(json -> getDomainFromEmail(json.getString("email")));
    }

    public Completable createDirectoryForDomain(String domain) {
        return vertx.fileSystem()
                .rxMkdirs(COMMENTS_PATH + domain);
    }

    public Completable writeCommentsToFile(String domain, Collection<JsonObject> comments) {
        return vertx.fileSystem()
                .rxWriteFile(COMMENTS_PATH + domain + "/comments.txt", Buffer.buffer(comments.toString()));
    }

    private String getDomainFromEmail(String email) {
        String[] splittedMailByDot = email.split("\\.");
        return splittedMailByDot[splittedMailByDot.length - 1];
    }
}
