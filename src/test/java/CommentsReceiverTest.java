import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.HttpRequest;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;

@RunWith(MockitoJUnitRunner.class)
public class CommentsReceiverTest {

    private static final String COMMENT_API_URL = "https://jsonplaceholder.typicode.com/comments";

    @Mock
    private WebClient webClient;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private HttpRequest<Buffer> httpRequest;

    @Mock
    private HttpResponse<Buffer> httpResponse;

    private CommentsReceiver commentsReceiver;

    @Before
    public void init() {
        commentsReceiver = new CommentsReceiver(webClient);
    }

    @Test
    public void shouldReturnObservableWithComments_whenGettingCommentsFromApi() {
        // given
        JsonObject json1 = new JsonObject()
                .put("id", 1)
                .put("mail", "abc@abc.com");
        JsonObject json2 = new JsonObject()
                .put("id", 2)
                .put("mail", "cde@cde.com");
        JsonArray exampleArrayFromApi = new JsonArray()
                .add(json1)
                .add(json2);

        given(webClient.getAbs(COMMENT_API_URL)).willReturn(httpRequest);
        given(httpRequest
                .addQueryParam(anyString(), anyString())
                .rxSend())
                .willReturn(Single.just(httpResponse));
        given(httpResponse.bodyAsJsonArray()).willReturn(exampleArrayFromApi);

        // when
        Observable<JsonObject> result = commentsReceiver.getComments(1);

        // then
        result.test()
                .assertValues(json1, json2)
                .assertComplete();
    }

    @Test
    public void shouldHandleError_whenErrorOccurDuringGettingCommentsFromApi() {
        // given
        given(webClient.getAbs(COMMENT_API_URL)).willReturn(httpRequest);
        given(httpRequest
                .addQueryParam(anyString(), anyString())
                .rxSend())
                .willReturn(Single.error(new Exception()));

        // when
        Observable<JsonObject> result = commentsReceiver.getComments(1);

        // then
        result.test()
                .assertError(Exception.class)
                .assertNotComplete();
    }

    @Test
    public void shouldGroupCommentsByDomain_whenGroupingComments() {
        // given
        JsonObject json1 = new JsonObject()
                .put("id", 1)
                .put("email", "abc@abc.com");
        JsonObject json2 = new JsonObject()
                .put("id", 2)
                .put("email", "cde@cde.com");
        JsonObject json3 = new JsonObject()
                .put("id", 3)
                .put("email", "fgh@fgh.pl");
        Map<String, Collection<JsonObject>> expected = new HashMap<>();
        expected.put("com", asList(json1, json2));
        expected.put("pl", singletonList(json3));

        List<JsonObject> comments = asList(json1, json2, json3);

        // when
        Single<Map<String, Collection<JsonObject>>> result = commentsReceiver.groupCommentsByDomain(comments);

        // then
        result.test()
                .assertValue(expected)
                .assertComplete();
    }
}
