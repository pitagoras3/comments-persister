import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CommentsReceiver {

    private static final String COMMENT_API_URL = "https://jsonplaceholder.typicode.com/comments";

    private final WebClient webClient;

    public CommentsReceiver(WebClient webClient) {
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

    private String getDomainFromEmail(String email) {
        String[] splittedMailByDot = email.split("\\.");
        return splittedMailByDot[splittedMailByDot.length - 1];
    }
}
