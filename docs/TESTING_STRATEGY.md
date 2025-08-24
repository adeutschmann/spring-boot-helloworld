# Testing Strategy

This document outlines the comprehensive testing approach for the Spring Boot HelloWorld application, covering unit testing, integration testing, and validation strategies.

## Testing Philosophy

### Testing Pyramid

```
    /\
   /  \     E2E Tests (Few)
  /____\    
 /      \   Integration Tests (Some)
/________\  Unit Tests (Many)
```

**Unit Tests (70%):**
- Fast execution (< 1 second)
- Isolated component testing
- Mock external dependencies
- High code coverage

**Integration Tests (20%):**
- Component interaction testing
- Spring context loading
- Database integration
- HTTP endpoint testing

**End-to-End Tests (10%):**
- Full application workflow
- Container-based testing
- Production-like environment

## Project Testing Structure

```
src/test/java/ch/adeutschmanndev/helloworld/
├── HelloworldApplicationTests.java          # Integration tests
├── resource/
│   └── HelloWorldHandlerTest.java           # Unit tests
├── router/
│   └── HelloWorldRouterTest.java            # Integration tests
└── integration/
    └── HelloWorldIntegrationTest.java       # Full integration tests
```

## Unit Testing

### Handler Unit Tests

**HelloWorldHandlerTest.java:**
```java
package ch.adeutschmanndev.helloworld.resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class HelloWorldHandlerTest {

    private HelloWorldHandler handler;

    @BeforeEach
    void setUp() {
        handler = new HelloWorldHandler();
    }

    @Test
    void hello_shouldReturnHelloWorldMessage() {
        // Given
        ServerRequest request = MockServerRequest.builder()
                .method(org.springframework.http.HttpMethod.GET)
                .uri("/hello")
                .build();

        // When
        ServerResponse response = handler.hello(request);

        // Then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.headers().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        
        // Verify response body (would need additional setup for full body verification)
    }

    @Test
    void hello_shouldHandleEmptyRequest() {
        // Given
        ServerRequest request = MockServerRequest.builder()
                .method(org.springframework.http.HttpMethod.GET)
                .uri("/hello")
                .build();

        // When
        ServerResponse response = handler.hello(request);

        // Then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
    }
}
```

### Testing Best Practices for Unit Tests

**1. AAA Pattern (Arrange, Act, Assert):**
```java
@Test
void testMethod() {
    // Arrange - Set up test data and conditions
    String expectedMessage = "Hello World";
    
    // Act - Execute the method under test
    String actualMessage = handler.getMessage();
    
    // Assert - Verify the results
    assertThat(actualMessage).isEqualTo(expectedMessage);
}
```

**2. Descriptive Test Names:**
```java
// Good
@Test
void hello_shouldReturnJsonResponse_whenValidRequestReceived() { }

// Bad
@Test
void testHello() { }
```

**3. Test Data Builders:**
```java
class TestDataBuilder {
    public static ServerRequest.Builder validRequest() {
        return MockServerRequest.builder()
                .method(HttpMethod.GET)
                .uri("/hello")
                .header("Accept", "application/json");
    }
}
```

## Integration Testing

### Web Layer Integration Tests

**HelloWorldRouterTest.java:**
```java
package ch.adeutschmanndev.helloworld.router;

import ch.adeutschmanndev.helloworld.resource.HelloWorldHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class HelloWorldRouterTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void hello_shouldReturnHelloWorldMessage() {
        // Given
        String url = "http://localhost:" + port + "/hello";

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Hello World");
        assertThat(response.getHeaders().getContentType().toString())
                .contains("application/json");
    }

    @Test
    void hello_shouldHandleConcurrentRequests() throws InterruptedException {
        // Given
        String url = "http://localhost:" + port + "/hello";
        int numberOfRequests = 10;
        Thread[] threads = new Thread[numberOfRequests];

        // When
        for (int i = 0; i < numberOfRequests; i++) {
            threads[i] = new Thread(() -> {
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Then - All requests should succeed (assertions in threads)
    }
}
```

### MockMvc Integration Tests

**Alternative approach using MockMvc:**
```java
@SpringBootTest
@AutoConfigureTestDatabase
@ActiveProfiles("test")
class HelloWorldMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void hello_shouldReturnJsonResponse() throws Exception {
        mockMvc.perform(get("/hello")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Hello World"));
    }

    @Test
    void hello_shouldSupportCorsRequests() throws Exception {
        mockMvc.perform(options("/hello")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk());
    }
}
```

## Test Configuration

### Test Properties

**application-test.properties:**
```properties
# Test-specific configuration
server.port=0
logging.level.ch.adeutschmanndev.helloworld=DEBUG
logging.level.org.springframework.test=DEBUG

# Disable external services in tests
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration

# Test database (if needed)
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
```

### Test Profiles

**Test Configuration Class:**
```java
@TestConfiguration
@Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public Clock testClock() {
        return Clock.fixed(Instant.parse("2025-08-24T10:00:00Z"), ZoneOffset.UTC);
    }

    @Bean
    @Primary
    public Environment testEnvironment() {
        return mock(Environment.class);
    }
}
```

## Container Testing

### Testcontainers Integration

**Containerized Integration Tests:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class HelloWorldContainerTest {

    @Container
    static GenericContainer<?> app = new GenericContainer<>("spring-boot-helloworld:test")
            .withExposedPorts(8080)
            .withEnv("SPRING_PROFILES_ACTIVE", "test");

    @Test
    void containerizedApplication_shouldStartSuccessfully() {
        // Given
        assertTrue(app.isRunning());
        String baseUrl = "http://" + app.getHost() + ":" + app.getMappedPort(8080);

        // When
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/hello", String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Hello World");
    }

    @Test
    void containerHealth_shouldBeHealthy() {
        // Given
        String healthUrl = "http://" + app.getHost() + ":" + app.getMappedPort(8080) + "/actuator/health";

        // When
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("UP");
    }
}
```

## Performance Testing

### Load Testing with JMeter

**JMeter Test Plan (`hello-load-test.jmx`):**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2">
  <hashTree>
    <TestPlan>
      <elementProp name="TestPlan.arguments" elementType="Arguments" guiclass="ArgumentsPanel">
        <collectionProp name="Arguments.arguments">
          <elementProp name="host" elementType="Argument">
            <stringProp name="Argument.name">host</stringProp>
            <stringProp name="Argument.value">localhost</stringProp>
          </elementProp>
          <elementProp name="port" elementType="Argument">
            <stringProp name="Argument.name">port</stringProp>
            <stringProp name="Argument.value">8080</stringProp>
          </elementProp>
        </collectionProp>
      </elementProp>
    </TestPlan>
    <hashTree>
      <ThreadGroup>
        <stringProp name="ThreadGroup.num_threads">100</stringProp>
        <stringProp name="ThreadGroup.ramp_time">10</stringProp>
        <stringProp name="ThreadGroup.duration">60</stringProp>
      </ThreadGroup>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
```

### Stress Testing

**Simple Stress Test:**
```java
@Test
@Timeout(value = 30, unit = TimeUnit.SECONDS)
void stressTest_shouldHandleHighLoad() {
    String url = "http://localhost:" + port + "/hello";
    int numberOfRequests = 1000;
    ExecutorService executor = Executors.newFixedThreadPool(50);
    CountDownLatch latch = new CountDownLatch(numberOfRequests);
    AtomicInteger successCount = new AtomicInteger(0);

    for (int i = 0; i < numberOfRequests; i++) {
        executor.submit(() -> {
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    successCount.incrementAndGet();
                }
            } finally {
                latch.countDown();
            }
        });
    }

    assertDoesNotThrow(() -> latch.await(25, TimeUnit.SECONDS));
    assertThat(successCount.get()).isGreaterThan(950); // 95% success rate
}
```

## Test Data Management

### Test Data Builders

**FluentTestDataBuilder:**
```java
public class ServerRequestBuilder {
    private HttpMethod method = HttpMethod.GET;
    private String uri = "/hello";
    private Map<String, String> headers = new HashMap<>();

    public static ServerRequestBuilder aRequest() {
        return new ServerRequestBuilder();
    }

    public ServerRequestBuilder withMethod(HttpMethod method) {
        this.method = method;
        return this;
    }

    public ServerRequestBuilder withUri(String uri) {
        this.uri = uri;
        return this;
    }

    public ServerRequestBuilder withHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public ServerRequest build() {
        MockServerRequest.Builder builder = MockServerRequest.builder()
                .method(method)
                .uri(uri);
        
        headers.forEach(builder::header);
        return builder.build();
    }
}
```

**Usage:**
```java
@Test
void testWithCustomRequest() {
    ServerRequest request = ServerRequestBuilder.aRequest()
            .withMethod(HttpMethod.GET)
            .withUri("/hello")
            .withHeader("Accept", "application/json")
            .build();

    // Test implementation
}
```

## Code Coverage

### JaCoCo Configuration

**Maven Plugin (`pom.xml`):**
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.8</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>PACKAGE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Running Coverage

**Generate Coverage Report:**
```bash
# Run tests with coverage
./mvnw clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html

# Check coverage thresholds
./mvnw jacoco:check
```

## CI/CD Testing Integration

### GitHub Actions Test Configuration

**Test Job:**
```yaml
test:
  runs-on: ubuntu-latest
  steps:
    - uses: actions/checkout@v4
    
    - name: Set up Java 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'
    
    - name: Cache Maven dependencies
      uses: actions/cache@v4
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
    
    - name: Run unit tests
      run: ./mvnw test
    
    - name: Run integration tests
      run: ./mvnw verify -Dspring.profiles.active=test
    
    - name: Generate coverage report
      run: ./mvnw jacoco:report
    
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
      with:
        file: ./target/site/jacoco/jacoco.xml
```

## Testing Best Practices

### Do's ✅

1. **Write Tests First**: Follow TDD approach when possible
2. **Test Behavior, Not Implementation**: Focus on what the code does
3. **Use Descriptive Names**: Test names should explain the scenario
4. **Keep Tests Independent**: Each test should be able to run in isolation
5. **Use Test Slices**: Use `@WebMvcTest`, `@DataJpaTest` for focused testing
6. **Mock External Dependencies**: Isolate units under test
7. **Maintain High Coverage**: Aim for >80% code coverage

### Don'ts ❌

1. **Don't Test Framework Code**: Don't test Spring Boot's functionality
2. **Don't Write Flaky Tests**: Avoid time-dependent or order-dependent tests
3. **Don't Ignore Failing Tests**: Fix or remove consistently failing tests
4. **Don't Test Private Methods**: Test public interface only
5. **Don't Use Production Data**: Use controlled test data
6. **Don't Skip Integration Tests**: Unit tests alone are insufficient

## Test Maintenance

### Cleaning Up Tests

**Regular Test Maintenance:**
```bash
# Find slow tests
./mvnw test -Djunit.jupiter.execution.timeout.default=5s

# Remove dead test code
./mvnw dependency:analyze-dep-mgt

# Update test dependencies
./mvnw versions:display-dependency-updates
```

### Test Documentation

**Document Complex Test Scenarios:**
```java
/**
 * This test verifies that the hello endpoint correctly handles concurrent requests
 * without any race conditions or thread safety issues.
 * 
 * Scenario: 10 concurrent GET requests to /hello endpoint
 * Expected: All requests return HTTP 200 with correct JSON response
 */
@Test
void concurrentRequests_shouldAllSucceed() {
    // Test implementation
}
```

This comprehensive testing strategy ensures high code quality, reliability, and maintainability of the Spring Boot application through thorough validation at all levels.
