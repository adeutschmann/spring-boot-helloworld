# Spring Boot Setup & Configuration

This document explains the Spring Boot 3.5.5 setup with Java 21 and modern configuration practices used in this project.

## Spring Boot Version & Dependencies

### Maven Configuration (`pom.xml`)

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.5</version>
    <relativePath/>
</parent>

<properties>
    <java.version>21</java.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Why Spring Boot 3.5.5?

- **Latest LTS Features**: Access to newest Spring Framework capabilities
- **Java 21 Support**: Full compatibility with latest Java LTS version
- **Performance Improvements**: Enhanced startup time and memory usage
- **Security Updates**: Latest security patches and improvements
- **Reactive Stack**: Better integration with WebFlux and reactive programming

## Java 21 Configuration

### Modern Java Features Utilized

1. **Records**: Could be used for immutable data transfer objects
2. **Pattern Matching**: Enhanced switch expressions
3. **Virtual Threads**: Improved concurrency (when using reactive stack)
4. **Text Blocks**: Cleaner multi-line strings in tests

### JVM Configuration

**Container Environment:**
```bash
JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication"
```

**Key JVM Settings:**
- `+UseContainerSupport`: Automatic container memory detection
- `MaxRAMPercentage=75.0`: Use 75% of available container memory
- `+UseG1GC`: Optimized garbage collector for low-latency applications
- `+UseStringDeduplication`: Memory optimization for string-heavy applications

## Application Structure

### Main Application Class

```java
@SpringBootApplication
public class HelloworldApplication {
    public static void main(String[] args) {
        SpringApplication.run(HelloworldApplication.class, args);
    }
}
```

**Key Features:**
- `@SpringBootApplication`: Combines `@Configuration`, `@EnableAutoConfiguration`, and `@ComponentScan`
- **Auto-configuration**: Automatic setup based on classpath dependencies
- **Component Scanning**: Automatic discovery of Spring components

### Package Structure

```
ch.adeutschmanndev.helloworld/
├── HelloworldApplication.java          # Main application entry point
├── resource/                           # Handler classes (business logic)
│   └── HelloWorldHandler.java
└── router/                            # Routing configuration
    └── HelloWorldRouter.java
```

**Design Principles:**
- **Separation of Concerns**: Clear boundaries between routing and business logic
- **Package by Feature**: Logical grouping of related functionality
- **Clean Architecture**: Dependencies point inward toward business logic

## Configuration Management

### Application Properties (`application.properties`)

```properties
# Server configuration
server.port=8080

# Logging configuration
logging.level.org.springframework.web=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n

# Management endpoints
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always
```

### Profile-Specific Configuration

**Development Profile (`application-dev.properties`):**
```properties
logging.level.ch.adeutschmanndev.helloworld=DEBUG
spring.web.resources.cache.period=0
```

**Production Profile (`application-prod.properties`):**
```properties
logging.level.ch.adeutschmanndev.helloworld=WARN
server.compression.enabled=true
server.compression.mime-types=application/json,text/css,text/html
```

## Auto-Configuration Features

### Web Stack Auto-Configuration

Spring Boot automatically configures:
- **Embedded Tomcat**: Default servlet container
- **DispatcherServlet**: Request routing mechanism
- **Error Handling**: Default error pages and exception handling
- **Content Negotiation**: Automatic JSON/XML serialization
- **CORS**: Cross-origin resource sharing support

### Functional Web Support

Enables:
- **RouterFunction**: Functional routing support
- **HandlerFunction**: Functional request handling
- **ServerRequest/ServerResponse**: Reactive web types
- **WebFilter**: Functional middleware support

## Testing Configuration

### Test Slices

**Web Layer Testing:**
```java
@WebMvcTest(HelloWorldRouter.class)
class HelloWorldRouterTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testHelloEndpoint() throws Exception {
        mockMvc.perform(get("/hello"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Hello World"));
    }
}
```

**Integration Testing:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HelloworldApplicationTests {
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void contextLoads() {
        ResponseEntity<String> response = restTemplate.getForEntity("/hello", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
```

## Build Configuration

### Maven Wrapper

```bash
# Ensures consistent Maven version across environments
./mvnw clean package
./mvnw spring-boot:run
```

### Spring Boot Maven Plugin

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
</plugin>
```

**Capabilities:**
- **Executable JAR**: Creates fat JARs with embedded server
- **Development Tools**: Hot reloading and automatic restarts
- **Build Info**: Automatic build information generation
- **Docker Support**: Buildpack integration for containerization

## Health Monitoring

### Actuator Endpoints

**Health Check:**
```bash
curl http://localhost:8080/actuator/health
```

**Application Info:**
```bash
curl http://localhost:8080/actuator/info
```

### Custom Health Indicators

```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        return Health.up()
            .withDetail("service", "HelloWorld")
            .withDetail("status", "operational")
            .build();
    }
}
```

## Security Configuration

### Basic Security Setup

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/hello", "/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .build();
    }
}
```

## Performance Optimization

### Startup Time Optimization

1. **Lazy Initialization**: `spring.main.lazy-initialization=true`
2. **Class Data Sharing**: JVM CDS for faster startup
3. **Native Compilation**: GraalVM native image support
4. **Selective Auto-Configuration**: Exclude unnecessary configurations

### Runtime Optimization

1. **Connection Pooling**: HikariCP for database connections
2. **Caching**: Spring Cache abstraction
3. **Compression**: HTTP response compression
4. **Resource Optimization**: Static resource caching

## Environment-Specific Configuration

### Docker/Container Environment

```properties
# Container-optimized settings
server.tomcat.max-threads=50
server.tomcat.min-spare-threads=10
spring.jpa.hibernate.ddl-auto=validate
```

### Cloud Environment

```properties
# Cloud-native settings
spring.cloud.config.enabled=true
management.metrics.export.prometheus.enabled=true
logging.config=classpath:logback-spring.xml
```

## Best Practices

1. **Externalize Configuration**: Use environment variables for sensitive data
2. **Profile Management**: Separate configuration for different environments
3. **Graceful Shutdown**: Configure proper shutdown behavior
4. **Resource Management**: Optimize thread pools and connection pools
5. **Monitoring**: Enable comprehensive health checks and metrics

This Spring Boot setup provides a solid foundation for building scalable, maintainable applications with modern Java features and cloud-native capabilities.
