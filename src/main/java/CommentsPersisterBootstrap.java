import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.client.WebClient;

public class CommentsPersisterBootstrap {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        WebClient webClient = WebClient.create(vertx);

        CommentsReceiver commentsReceiver = new CommentsReceiver(webClient);
        commentsReceiver.getComments(3)
                .doFinally(vertx::close)
                .subscribe(json -> System.out.println(json.encodePrettily()));
    }
}
