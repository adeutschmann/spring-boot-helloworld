package ch.adeutschmanndev.helloworld.router;

import ch.adeutschmanndev.helloworld.resource.HelloWorldHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.server.RequestPath;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HelloWorldRouter Unit Tests")
class HelloWorldRouterTest {

    @Mock
    private HelloWorldHandler helloWorldHandler;

    @Mock
    private ServerRequest serverRequest;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private RequestPath requestPath;

    @Mock
    private ServerResponse serverResponse;

    private HelloWorldRouter helloWorldRouter;

    @BeforeEach
    void setUp() {
        helloWorldRouter = new HelloWorldRouter();

        // Setup common mocks for all tests with lenient stubbing
        lenient().when(serverRequest.servletRequest()).thenReturn(httpServletRequest);
        lenient().when(serverRequest.requestPath()).thenReturn(requestPath);
        lenient().when(requestPath.pathWithinApplication()).thenReturn(org.springframework.http.server.PathContainer.parsePath("/hello"));
    }

    @Test
    @DisplayName("Should create router function bean")
    void shouldCreateRouterFunctionBean() {
        // When
        RouterFunction<ServerResponse> routerFunction = helloWorldRouter.helloWorldRoutes(helloWorldHandler);

        // Then
        assertThat(routerFunction).isNotNull();
    }

    @Test
    @DisplayName("Should route GET /hello requests to handler")
    void shouldRouteGetHelloRequestsToHandler() throws Exception {
        // Given
        when(serverRequest.method()).thenReturn(org.springframework.http.HttpMethod.GET);
        when(serverRequest.headers()).thenReturn(mock(ServerRequest.Headers.class));
        when(serverRequest.headers().accept()).thenReturn(java.util.List.of(MediaType.APPLICATION_JSON));
        when(helloWorldHandler.hello(any(ServerRequest.class))).thenReturn(serverResponse);
        when(httpServletRequest.getMethod()).thenReturn("GET");

        RouterFunction<ServerResponse> routerFunction = helloWorldRouter.helloWorldRoutes(helloWorldHandler);

        // When
        Optional<HandlerFunction<ServerResponse>> handlerFunction = routerFunction.route(serverRequest);

        // Then
        assertThat(handlerFunction).isPresent();

        // Execute the handler function to verify it calls the handler
        ServerResponse response = handlerFunction.get().handle(serverRequest);
        assertThat(response).isEqualTo(serverResponse);
        verify(helloWorldHandler).hello(serverRequest);
    }

    @Test
    @DisplayName("Should not route non-GET requests")
    void shouldNotRouteNonGetRequests() {
        // Given
        when(serverRequest.method()).thenReturn(org.springframework.http.HttpMethod.POST);
        when(httpServletRequest.getMethod()).thenReturn("POST");

        RouterFunction<ServerResponse> routerFunction = helloWorldRouter.helloWorldRoutes(helloWorldHandler);

        // When
        Optional<HandlerFunction<ServerResponse>> handlerFunction = routerFunction.route(serverRequest);

        // Then
        assertThat(handlerFunction).isEmpty();
        verify(helloWorldHandler, never()).hello(any(ServerRequest.class));
    }

    @Test
    @DisplayName("Should not route requests to different paths")
    void shouldNotRouteRequestsToDifferentPaths() {
        // Given
        when(serverRequest.method()).thenReturn(org.springframework.http.HttpMethod.GET);
        when(requestPath.pathWithinApplication()).thenReturn(org.springframework.http.server.PathContainer.parsePath("/different-path"));
        when(httpServletRequest.getMethod()).thenReturn("GET");

        RouterFunction<ServerResponse> routerFunction = helloWorldRouter.helloWorldRoutes(helloWorldHandler);

        // When
        Optional<HandlerFunction<ServerResponse>> handlerFunction = routerFunction.route(serverRequest);

        // Then
        assertThat(handlerFunction).isEmpty();
        verify(helloWorldHandler, never()).hello(any(ServerRequest.class));
    }

    @Test
    @DisplayName("Should not route requests without JSON accept header")
    void shouldNotRouteRequestsWithoutJsonAcceptHeader() {
        // Given
        when(serverRequest.method()).thenReturn(org.springframework.http.HttpMethod.GET);
        when(serverRequest.headers()).thenReturn(mock(ServerRequest.Headers.class));
        when(serverRequest.headers().accept()).thenReturn(java.util.List.of(MediaType.TEXT_PLAIN));
        when(httpServletRequest.getMethod()).thenReturn("GET");

        RouterFunction<ServerResponse> routerFunction = helloWorldRouter.helloWorldRoutes(helloWorldHandler);

        // When
        Optional<HandlerFunction<ServerResponse>> handlerFunction = routerFunction.route(serverRequest);

        // Then
        assertThat(handlerFunction).isEmpty();
        verify(helloWorldHandler, never()).hello(any(ServerRequest.class));
    }

    @Test
    @DisplayName("Should accept wildcard accept headers")
    void shouldAcceptWildcardAcceptHeaders() throws Exception {
        // Given
        when(serverRequest.method()).thenReturn(org.springframework.http.HttpMethod.GET);
        when(serverRequest.headers()).thenReturn(mock(ServerRequest.Headers.class));
        when(serverRequest.headers().accept()).thenReturn(java.util.List.of(MediaType.ALL));
        when(helloWorldHandler.hello(any(ServerRequest.class))).thenReturn(serverResponse);
        when(httpServletRequest.getMethod()).thenReturn("GET");

        RouterFunction<ServerResponse> routerFunction = helloWorldRouter.helloWorldRoutes(helloWorldHandler);

        // When
        Optional<HandlerFunction<ServerResponse>> handlerFunction = routerFunction.route(serverRequest);

        // Then
        assertThat(handlerFunction).isPresent();

        // Execute the handler function to verify it calls the handler
        ServerResponse response = handlerFunction.get().handle(serverRequest);
        assertThat(response).isEqualTo(serverResponse);
        verify(helloWorldHandler).hello(serverRequest);
    }
}
