# Functional Web Framework

This document explains the implementation of Spring's functional web framework using RouterFunction and handler patterns.

## Overview

Instead of using traditional `@Controller` annotations, this application demonstrates Spring's functional web framework approach, which provides:
- More explicit routing configuration
- Better testability
- Functional programming paradigms
- Type-safe route definitions

## Implementation Details

### Handler Pattern (`HelloWorldHandler.java`)

```java
@Component
public class HelloWorldHandler {
    public ServerResponse hello(ServerRequest request) {
        Map<String, String> response = Map.of("message", "Hello World");
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }
}
```

**Key Features:**
- **Separation of Concerns**: Business logic isolated in handler methods
- **Immutable Responses**: Uses `ServerResponse` for type-safe HTTP responses
- **JSON Serialization**: Automatic JSON conversion with proper content types
- **Spring Component**: Managed by Spring's dependency injection container

### Router Configuration (`HelloWorldRouter.java`)

```java
@Configuration
public class HelloWorldRouter {
    @Bean
    public RouterFunction<ServerResponse> helloWorldRoutes(HelloWorldHandler handler) {
        return RouterFunctions
                .route(GET("/hello").and(accept(MediaType.APPLICATION_JSON)), handler::hello);
    }
}
```

**Key Features:**
- **Explicit Routing**: Clear, declarative route definitions
- **Type Safety**: Compile-time route validation
- **Composable**: Routes can be easily combined and nested
- **Testable**: Easy to unit test without web server

## Benefits Over Traditional Controllers

### 1. Explicit Route Definition
- Routes are defined in one place, making them easy to understand
- No hidden magic with annotations scattered across classes
- Clear request/response flow

### 2. Better Testability
```java
// Easy to test without starting web server
RouterFunction<ServerResponse> route = HelloWorldRouter.helloWorldRoutes(handler);
ServerRequest request = MockServerRequest.builder()
    .method(GET)
    .uri("/hello")
    .build();
    
ServerResponse response = route.route(request).block();
```

### 3. Functional Composition
- Routes can be easily composed and combined
- Middleware and filters can be applied functionally
- Better suited for reactive programming

### 4. Performance
- Lower overhead compared to annotation-based controllers
- Better memory usage for high-throughput applications
- More predictable performance characteristics

## Request/Response Flow

1. **Request Reception**: Spring WebFlux receives HTTP request
2. **Route Matching**: RouterFunction matches `/hello` with GET method
3. **Handler Invocation**: `HelloWorldHandler.hello()` method is called
4. **Response Building**: Handler creates JSON response with proper headers
5. **Response Return**: ServerResponse is serialized and returned to client

## Advanced Patterns

### Route Composition
```java
@Bean
public RouterFunction<ServerResponse> allRoutes(HelloWorldHandler handler) {
    return RouterFunctions
        .route(GET("/hello"), handler::hello)
        .andRoute(GET("/health"), handler::health)
        .andRoute(POST("/hello"), handler::createHello);
}
```

### Request Filtering
```java
@Bean
public RouterFunction<ServerResponse> filteredRoutes(HelloWorldHandler handler) {
    return RouterFunctions
        .route(GET("/hello"), handler::hello)
        .filter((request, next) -> {
            // Add logging, security, etc.
            return next.handle(request);
        });
}
```

### Content Negotiation
```java
return RouterFunctions
    .route(GET("/hello").and(accept(MediaType.APPLICATION_JSON)), handler::helloJson)
    .andRoute(GET("/hello").and(accept(MediaType.TEXT_PLAIN)), handler::helloText);
```

## Testing Strategy

### Unit Testing Handlers
```java
@Test
void testHelloHandler() {
    HelloWorldHandler handler = new HelloWorldHandler();
    ServerRequest request = MockServerRequest.builder()
        .method(GET)
        .uri("/hello")
        .build();
        
    ServerResponse response = handler.hello(request);
    
    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
}
```

### Integration Testing Routes
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HelloWorldRouterTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void testHelloEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity("/hello", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Hello World");
    }
}
```

## Best Practices

1. **Keep Handlers Focused**: One responsibility per handler method
2. **Use Dependency Injection**: Let Spring manage handler dependencies
3. **Proper Error Handling**: Return appropriate HTTP status codes
4. **Content Type Management**: Always set proper content types
5. **Route Organization**: Group related routes in the same router class

## Migration from Traditional Controllers

If migrating from `@Controller` approach:

### Before (Controller)
```java
@RestController
public class HelloController {
    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("message", "Hello World");
    }
}
```

### After (Functional)
```java
// Handler
@Component
public class HelloWorldHandler {
    public ServerResponse hello(ServerRequest request) {
        Map<String, String> response = Map.of("message", "Hello World");
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }
}

// Router
@Configuration
public class HelloWorldRouter {
    @Bean
    public RouterFunction<ServerResponse> routes(HelloWorldHandler handler) {
        return RouterFunctions.route(GET("/hello"), handler::hello);
    }
}
```

## Performance Considerations

- **Memory Efficiency**: Lower memory footprint per request
- **CPU Efficiency**: Reduced reflection overhead
- **Reactive Support**: Better integration with reactive streams
- **Scaling**: More predictable performance under load

This functional approach provides a solid foundation for building scalable, testable web applications with Spring Boot.
