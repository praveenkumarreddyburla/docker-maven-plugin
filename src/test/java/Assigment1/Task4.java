package Assigment1;

import io.fabric8.maven.docker.access.chunked.PullOrPushResponseJsonHandler;
import io.fabric8.maven.docker.util.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import com.google.gson.JsonObject;
import io.fabric8.maven.docker.access.DockerAccessException;
import io.fabric8.maven.docker.access.chunked.PullOrPushResponseJsonHandler;
import io.fabric8.maven.docker.util.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;


public class Task4 {

    @Mock
    private Logger log;

    private PullOrPushResponseJsonHandler handler;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new PullOrPushResponseJsonHandler(log);
    }


    @Test
    public void testErrorHandling() {
        Logger mockLogger = mock(Logger.class);
        PullOrPushResponseJsonHandler handler = new PullOrPushResponseJsonHandler(mockLogger);

        JsonObject json = new JsonObject();
        json.addProperty("error", "Failed to pull image");
        JsonObject errorDetail = new JsonObject();
        errorDetail.addProperty("message", "Image not found");
        json.add("errorDetail", errorDetail);

        DockerAccessException exception = assertThrows(DockerAccessException.class,
                () -> handler.process(json));
        assertEquals("Failed to pull image (Image not found)", exception.getMessage());

        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void testLogInfoMessageWithStreamField() throws DockerAccessException {
        JsonObject json = new JsonObject();
        json.addProperty("stream", "This is a log message\n");

        handler.logInfoMessage(json);

        Mockito.verify(log).info("%s", "This is a log message");
    }

    @Test
    public void testProcessWithProgressDetail() throws DockerAccessException {
        JsonObject json = new JsonObject();
        json.add("progressDetail", new JsonObject());
        json.addProperty("id", "123");
        json.addProperty("status", "Downloading");
        json.addProperty("progress", "[==================>] 100%");

        handler.process(json);

        Mockito.verify(log).progressUpdate("123", "Downloading",
                "[==================>] 100%");
    }

    @Test
    public void testLogInfoMessageWithStatusField() throws DockerAccessException {
        JsonObject json = new JsonObject();
        json.addProperty("status", "Downloading");

        handler.logInfoMessage(json);

        Mockito.verify(log).info("%s", "Downloading");
    }

    @Test
    public void testLogInfoMessageWithNoStreamOrStatusField() throws DockerAccessException {
        JsonObject json = new JsonObject();
        json.addProperty("otherField", "Some value");

        handler.logInfoMessage(json);

        Mockito.verify(log).info("%s", json.toString());
    }


    @Test
    public void testStart() {
        handler.start();

        Mockito.verify(log).progressStart();
    }

    @Test
    public void testStop() {
        handler.stop();

        Mockito.verify(log).progressFinished();
    }
}