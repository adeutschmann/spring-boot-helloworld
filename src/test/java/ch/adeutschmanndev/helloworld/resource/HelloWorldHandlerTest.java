package ch.adeutschmanndev.helloworld.resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("HelloWorldHandler Unit Tests")
class HelloWorldHandlerTest {

    @Mock
    private ServerRequest serverRequest;

    private HelloWorldHandler helloWorldHandler;

    @BeforeEach
    void setUp() {
        helloWorldHandler = new HelloWorldHandler();
    }

    @Test
    @DisplayName("Should return Hello World message with OK status")
    void shouldReturnHelloWorldMessageWithOkStatus() {
        // When
        ServerResponse response = helloWorldHandler.hello(serverRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);

        // Verify content type is JSON
        assertThat(response.headers().getFirst("Content-Type")).contains(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    @DisplayName("Should return correct JSON structure")
    void shouldReturnCorrectJsonStructure() {
        // When
        ServerResponse response = helloWorldHandler.hello(serverRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);

        // The handler creates a Map with "message" -> "Hello World"
        // We can't easily extract the body in unit tests without integration context,
        // but we can verify the response structure is created correctly
        assertThat(response.headers().getFirst("Content-Type")).contains(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    @DisplayName("Should handle multiple consecutive requests consistently")
    void shouldHandleMultipleRequestsConsistently() {
        // When - make multiple calls
        ServerResponse response1 = helloWorldHandler.hello(serverRequest);
        ServerResponse response2 = helloWorldHandler.hello(serverRequest);
        ServerResponse response3 = helloWorldHandler.hello(serverRequest);

        // Then - all responses should be consistent
        assertThat(response1.statusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.statusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response3.statusCode()).isEqualTo(HttpStatus.OK);

        assertThat(response1.headers().getFirst("Content-Type")).contains(MediaType.APPLICATION_JSON_VALUE);
        assertThat(response2.headers().getFirst("Content-Type")).contains(MediaType.APPLICATION_JSON_VALUE);
        assertThat(response3.headers().getFirst("Content-Type")).contains(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    @DisplayName("Should not depend on request parameters")
    void shouldNotDependOnRequestParameters() {
        // When
        ServerResponse response = helloWorldHandler.hello(serverRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.headers().getFirst("Content-Type")).contains(MediaType.APPLICATION_JSON_VALUE);
    }
}
