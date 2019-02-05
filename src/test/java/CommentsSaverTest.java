import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.file.FileSystem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(MockitoJUnitRunner.class)
public class CommentsSaverTest {

    private Vertx vertx;
    private FileSystem fileSystem;
    private CommentsSaver commentsSaver;

    @Before
    public void init() {
        vertx = Vertx.vertx();
        fileSystem = vertx.fileSystem();
        commentsSaver = new CommentsSaver(vertx);
    }

    @Test
    public void shouldCreateDirectoryWithDomainName_whenCreatingDirectoryForDomain() {
        // given
        String domain = "pl";

        // when
        // then
        fileSystem
                .rxCreateTempDirectory("testComments")
                .flatMap(testPath -> commentsSaver
                        .createDirectoryForDomain(testPath + "/", domain)
                        .andThen(fileSystem.rxReadDir(testPath + "/" + domain)))
                .test()
                .awaitDone(5, TimeUnit.SECONDS)
                .assertComplete();
    }

    @Test
    public void shouldWriteCommentsToFile_whenWritingCommentsToFile() {
        // given
        String domain = "com";

        JsonObject json1 = new JsonObject()
                .put("id", 1)
                .put("mail", "abc@abc.com");
        JsonObject json2 = new JsonObject()
                .put("id", 2)
                .put("mail", "cde@cde.com");

        Collection<JsonObject> jsons = Arrays.asList(json1, json2);
        List<String> expectedFileContent = Arrays.asList(json1.encodePrettily(), json2.encodePrettily());

        // when
        // then
        fileSystem
                .rxCreateTempDirectory("testComments_")
                .flatMap(tempPath -> fileSystem.rxMkdir(tempPath + "/" + domain)
                        .andThen(commentsSaver
                                .writeCommentsToFile(tempPath + "/", domain, jsons)
                                .andThen(fileSystem.rxReadFile(tempPath + "/" + domain + "/comments.txt")
                                        .map(Buffer::toString))))
                .test()
                .awaitDone(5, TimeUnit.SECONDS)
                .assertValue(expectedFileContent.toString())
                .assertComplete();
    }
}
