import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.client.WebClient;

import java.util.Collection;
import java.util.Map;

public class CommentsPersisterBootstrap {

    private static final int AMOUNT_OF_COMMENTS = 3;
    private static final String COMMENTS_PATH = "comments/";

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        WebClient webClient = WebClient.create(vertx);

        CommentsReceiver commentsReceiver = new CommentsReceiver(webClient);
        CommentsSaver commentsSaver = new CommentsSaver(vertx);

        Single<Map<String, Collection<JsonObject>>> extensionToJsonCommentsMap = commentsReceiver.getComments(AMOUNT_OF_COMMENTS)
                .toList()
                .flatMap(commentsReceiver::groupCommentsByDomain);

        extensionToJsonCommentsMap.map(Map::keySet)
                .flattenAsObservable(x -> x)
                .flatMapCompletable(domain -> commentsSaver.createDirectoryForDomain(COMMENTS_PATH, domain))
                .andThen(extensionToJsonCommentsMap)
                .flattenAsObservable(Map::entrySet)
                .flatMapCompletable(entry -> commentsSaver.writeCommentsToFile(COMMENTS_PATH, entry.getKey(), entry.getValue()))
                .doFinally(vertx::close)
                .subscribe();
    }
}
