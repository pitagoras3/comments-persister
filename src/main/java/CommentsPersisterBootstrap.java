import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.client.WebClient;

import java.util.Collection;
import java.util.Map;

public class CommentsPersisterBootstrap {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        WebClient webClient = WebClient.create(vertx);

        CommentsReceiver commentsReceiver = new CommentsReceiver(vertx, webClient);
        Observable<JsonObject> comments = commentsReceiver.getComments(3);
        Single<Map<String, Collection<JsonObject>>> extensionToJsons = commentsReceiver.groupCommentsByDomain(comments);

        extensionToJsons.map(Map::keySet)
                .flattenAsObservable(x -> x)
                .flatMapCompletable(commentsReceiver::createDirectoryForDomain)
                .andThen(extensionToJsons)
                .flattenAsObservable(Map::entrySet)
                .flatMapCompletable(entry -> commentsReceiver.writeCommentsToFile(entry.getKey(), entry.getValue()))
                .doFinally(vertx::close)
                .subscribe();
    }
}
