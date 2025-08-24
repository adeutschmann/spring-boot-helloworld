# Local Development Setup

This document provides comprehensive guidance for setting up and optimizing the local development environment for this Spring Boot project.

## Prerequisites

### Required Software

**Java Development Kit:**
```bash
# Verify Java 21 installation
java -version
# Should output: openjdk version "21.0.x"

# Install Java 21 if needed (macOS with Homebrew)
brew install openjdk@21

# Install Java 21 (Ubuntu/Debian)
sudo apt update
sudo apt install openjdk-21-jdk

# Install Java 21 (Windows with Chocolatey)
choco install openjdk21
```

**Maven:**
```bash
# Verify Maven installation
mvn -version
# Should output: Apache Maven 3.6.x or higher

# Install Maven (macOS with Homebrew)
brew install maven

# Install Maven (Ubuntu/Debian)
sudo apt install maven

# Install Maven (Windows with Chocolatey)
choco install maven
```

**Container Runtime (Optional):**
```bash
# Install Podman (recommended)
# macOS
brew install podman

# Ubuntu/Debian
sudo apt update
sudo apt install podman

# Alternative: Docker Desktop
# Download from https://www.docker.com/products/docker-desktop
```

## IDE Configuration

### IntelliJ IDEA Setup

**1. Import Project:**
```
File → Open → Select project root directory
Choose "Maven" as project type
```

**2. Java SDK Configuration:**
```
File → Project Structure → Project Settings → Project
Project SDK: Choose Java 21
Project Language Level: 21 - Pattern matching for switch
```

**3. Maven Configuration:**
```
File → Settings → Build, Execution, Deployment → Build Tools → Maven
Maven home path: /usr/local/Cellar/maven/x.x.x/libexec (or system path)
User settings file: ~/.m2/settings.xml
Local repository: ~/.m2/repository
```

**4. Spring Boot Configuration:**
```
File → Settings → Plugins
Install: Spring Boot, Spring MVC, Spring Data
Enable annotation processing for Spring configurations
```

### VS Code Setup

**Required Extensions:**
```json
{
  "recommendations": [
    "vscjava.vscode-java-pack",
    "vmware.vscode-spring-boot",
    "redhat.java",
    "vscjava.vscode-maven",
    "ms-vscode.vscode-json"
  ]
}
```

**VS Code Settings (`.vscode/settings.json`):**
```json
{
  "java.home": "/path/to/java-21",
  "java.configuration.runtimes": [
    {
      "name": "JavaSE-21",
      "path": "/path/to/java-21"
    }
  ],
  "spring-boot.ls.java.home": "/path/to/java-21",
  "maven.executable.path": "/usr/local/bin/mvn"
}
```

## Project Setup

### Clone and Initial Setup

```bash
# Clone the repository
git clone https://github.com/adeutschmann/spring-boot-helloworld.git
cd spring-boot-helloworld

# Verify Maven wrapper
./mvnw --version

# Download dependencies
./mvnw dependency:go-offline

# Compile the project
./mvnw compile

# Run tests
./mvnw test
```

### Environment Configuration

**1. Application Properties (`src/main/resources/application-dev.properties`):**
```properties
# Development-specific settings
server.port=8080
logging.level.ch.adeutschmanndev.helloworld=DEBUG
logging.level.org.springframework.web=DEBUG

# Development tools
spring.devtools.restart.enabled=true
spring.devtools.livereload.enabled=true

# Actuator endpoints (development only)
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
```

**2. IDE Run Configuration:**
```
Main Class: ch.adeutschmanndev.helloworld.HelloworldApplication
VM Options: -Dspring.profiles.active=dev
Program Arguments: --spring.profiles.active=dev
Working Directory: /path/to/project/root
```

## Development Workflow

### Running the Application

**Command Line:**
```bash
# Run with Maven wrapper
./mvnw spring-boot:run

# Run with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Run with JVM debug mode
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
```

**IntelliJ IDEA:**
```
1. Open HelloworldApplication.java
2. Click the green arrow next to main method
3. Or use Run Configuration with profile settings
```

**VS Code:**
```
1. Open Command Palette (Cmd+Shift+P)
2. Type "Spring Boot Dashboard"
3. Click run button next to the application
```

### Hot Reload and Development Tools

**Spring Boot DevTools Configuration:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

**IntelliJ IDEA Auto-Reload:**
```
File → Settings → Build, Execution, Deployment → Compiler
✓ Build project automatically

Help → Find Action → Registry
✓ compiler.automake.allow.when.app.running
```

**Manual Restart:**
```bash
# Trigger restart (create/modify this file)
touch src/main/resources/.reloadtrigger
```

## Debugging Setup

### IntelliJ IDEA Debugging

**1. Debug Configuration:**
```
Run → Edit Configurations → Spring Boot
Main class: ch.adeutschmanndev.helloworld.HelloworldApplication
VM options: -Dspring.profiles.active=dev
Debug port: 5005 (default)
```

**2. Setting Breakpoints:**
```java
// Set breakpoints in HelloWorldHandler.java
@Component
public class HelloWorldHandler {
    public ServerResponse hello(ServerRequest request) {
        // Breakpoint here
        Map<String, String> response = Map.of("message", "Hello World");
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }
}
```

### Remote Debugging

**1. Start Application with Debug Mode:**
```bash
./mvnw spring-boot:run \
  -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
```

**2. Connect IDE to Debug Port:**
```
IntelliJ: Run → Attach to Process → localhost:5005
VS Code: Debug → Attach → localhost:5005
```

## Testing Setup

### Unit Testing Configuration

**Run All Tests:**
```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=HelloworldApplicationTests

# Run tests with coverage
./mvnw test jacoco:report
```

**IntelliJ IDEA Testing:**
```
Right-click on test class → Run 'TestClass'
Right-click on project → Run 'All Tests'
View → Tool Windows → Test Results
```

### Integration Testing

**Test Configuration (`src/test/resources/application-test.properties`):**
```properties
# Test-specific settings
server.port=0
logging.level.ch.adeutschmanndev.helloworld=INFO
spring.test.mockmvc.print=true
```

**Example Integration Test:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HelloWorldIntegrationTest {
    
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

## Container Development

### Local Container Build

**Build with Podman:**
```bash
# Build container image
podman build -t spring-boot-helloworld:dev -f Containerfile .

# Run container locally
podman run -p 8080:8080 spring-boot-helloworld:dev

# Run with volume mounting for development
podman run -p 8080:8080 \
  -v $(pwd)/src:/app/src \
  spring-boot-helloworld:dev
```

**Development Containerfile (`Containerfile.dev`):**
```dockerfile
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app
COPY . .

# Install dependencies
RUN ./mvnw dependency:go-offline

# Expose debug port
EXPOSE 8080 5005

# Run with debug and hot-reload
CMD ["./mvnw", "spring-boot:run", \
     "-Dspring-boot.run.profiles=dev", \
     "-Dspring-boot.run.jvmArguments=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"]
```

## Code Quality Tools

### Code Formatting

**IntelliJ IDEA Code Style:**
```
File → Settings → Editor → Code Style → Java
Import scheme: Google Java Style Guide
Configure: Tabs and indents, Spaces, etc.
```

**Automatic Formatting:**
```
Code → Reformat Code (Ctrl+Alt+L)
Code → Optimize Imports (Ctrl+Alt+O)
```

### Static Analysis

**Checkstyle Configuration (`checkstyle.xml`):**
```xml
<?xml version="1.0"?>
<!DOCTYPE module PUBLIC "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
        "https://checkstyle.org/dtds/configuration_1_3.dtd">
<module name="Checker">
    <module name="TreeWalker">
        <module name="UnusedImports"/>
        <module name="IllegalImport"/>
        <module name="LineLength">
            <property name="max" value="120"/>
        </module>
    </module>
</module>
```

**Maven Integration:**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.3.0</version>
    <configuration>
        <configLocation>checkstyle.xml</configLocation>
    </configuration>
</plugin>
```

## Database Development (Optional)

### H2 Database Setup

**Dependencies (`pom.xml`):**
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

**Configuration (`application-dev.properties`):**
```properties
# H2 Database configuration
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

## Performance Profiling

### JVM Profiling

**Enable JFR (Java Flight Recorder):**
```bash
./mvnw spring-boot:run \
  -Dspring-boot.run.jvmArguments="-XX:+FlightRecorder -XX:StartFlightRecording=duration=60s,filename=app-profile.jfr"
```

**Memory Analysis:**
```bash
# Generate heap dump
./mvnw spring-boot:run \
  -Dspring-boot.run.jvmArguments="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./heapdump.hprof"
```

### Application Metrics

**Micrometer Integration:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

**Access Metrics:**
```bash
# Application health
curl http://localhost:8080/actuator/health

# Application metrics
curl http://localhost:8080/actuator/metrics

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus
```

## Git Workflow

### Pre-commit Hooks

**Setup Git Hooks:**
```bash
# Create pre-commit hook
cat > .git/hooks/pre-commit << 'EOF'
#!/bin/bash
./mvnw test
if [ $? -ne 0 ]; then
    echo "Tests failed. Commit aborted."
    exit 1
fi
EOF

chmod +x .git/hooks/pre-commit
```

### Branch Management

**Feature Development:**
```bash
# Create feature branch
git checkout -b feature/your-feature-name

# Regular commits
git add .
git commit -m "feat: add new functionality"

# Push feature branch
git push origin feature/your-feature-name
```

## Troubleshooting

### Common Issues

**Port Already in Use:**
```bash
# Find process using port 8080
lsof -ti:8080

# Kill process
kill -9 $(lsof -ti:8080)

# Use different port
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

**Maven Dependency Issues:**
```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Reimport dependencies
./mvnw clean install
```

**IDE Issues:**
```bash
# IntelliJ IDEA: Invalidate caches
File → Invalidate Caches and Restart

# VS Code: Reload Java projects
Cmd+Shift+P → Java: Clean Workspace
```

This comprehensive local development setup ensures optimal productivity, debugging capabilities, and code quality for Spring Boot development.
