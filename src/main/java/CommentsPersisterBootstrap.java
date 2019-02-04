import io.reactivex.Observable;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.client.WebClient;

public class CommentsPersisterBootstrap {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        WebClient webClient = WebClient.create(vertx);

        CommentsReceiver commentsReceiver = new CommentsReceiver(webClient);
        Observable<JsonObject> comments = commentsReceiver.getComments(3);
        commentsReceiver.groupCommentsByDomain(comments)
                .doFinally(vertx::close)
                .subscribe(map -> map.forEach((x, y) -> System.out.println(x + y)));
    }
}
