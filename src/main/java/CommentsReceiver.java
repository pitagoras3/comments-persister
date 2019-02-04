import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

class CommentsReceiver {

    private static final String COMMENT_API_URL = "https://jsonplaceholder.typicode.com/comments";
    private static final int STARTING_POST_ID = 1;

    private final WebClient webClient;

    CommentsReceiver(WebClient webClient) {
        this.webClient = webClient;
    }

    Observable<JsonObject> getComments(int amountOfComments) {
        return Observable.range(STARTING_POST_ID, amountOfComments)
                .flatMapSingle(id -> webClient.getAbs(COMMENT_API_URL)
                        .addQueryParam("postId", String.valueOf(id))
                        .rxSend())
                .map(HttpResponse::bodyAsJsonArray)
                .map(jsonArray -> jsonArray.stream().map(json -> (JsonObject) json)
                        .collect(toList()))
                .flatMapIterable(x -> x);
    }

    Single<Map<String, Collection<JsonObject>>> groupCommentsByDomain(List<JsonObject> comments) {
        return Observable.fromIterable(comments)
                .toMultimap(json -> getDomainFromEmail(json.getString("email")));
    }

    private String getDomainFromEmail(String email) {
        String[] splittedMailByDot = email.split("\\.");
        return splittedMailByDot[splittedMailByDot.length - 1];
    }
}
