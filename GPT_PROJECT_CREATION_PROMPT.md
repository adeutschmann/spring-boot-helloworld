# GPT Prompt: Create Comprehensive Spring Boot HelloWorld Project

## Project Overview
Create a production-ready Spring Boot HelloWorld application with modern architecture, comprehensive documentation, CI/CD pipeline, containerization, and enterprise integration. This project should demonstrate best practices for Spring Boot 3.5.5 with Java 21, functional web framework, multi-stage container builds, and complete DevOps workflow.

## Project Requirements

### 1. Core Application Structure
Create a Spring Boot 3.5.5 application with the following package structure:
```
src/main/java/ch/adeutschmanndev/helloworld/
├── HelloworldApplication.java           # Main Spring Boot application
├── resource/HelloWorldHandler.java     # Functional handler implementation
└── router/HelloWorldRouter.java        # Route configuration
```

### 2. Maven Configuration (pom.xml)
Create a comprehensive Maven configuration with:
- **Parent**: spring-boot-starter-parent 3.5.5
- **Java Version**: 21
- **Group ID**: ch.adeutschmanndev
- **Artifact ID**: helloworld
- **Version**: 0.0.1-SNAPSHOT
- **Dependencies**:
  - spring-boot-starter-web
  - spring-boot-starter-test
  - spring-boot-maven-plugin

**Sample pom.xml structure:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.5</version>
        <relativePath/>
    </parent>
    <groupId>ch.adeutschmanndev</groupId>
    <artifactId>helloworld</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>helloworld</name>
    <description>Production-ready Spring Boot HelloWorld application</description>
    <properties>
        <java.version>21</java.version>
    </properties>
    <!-- Add complete dependencies and build configuration -->
</project>
```

### 3. Spring Boot Application Implementation

**HelloworldApplication.java:**
```java
package ch.adeutschmanndev.helloworld;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HelloworldApplication {
    public static void main(String[] args) {
        SpringApplication.run(HelloworldApplication.class, args);
    }
}
```

**HelloWorldHandler.java (Functional Web Framework):**
```java
package ch.adeutschmanndev.helloworld.resource;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;
import java.util.Map;

@Component
public class HelloWorldHandler {
    public Mono<ServerResponse> hello(ServerRequest request) {
        Map<String, String> response = Map.of("message", "Hello World");
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(response);
    }
}
```

**HelloWorldRouter.java:**
```java
package ch.adeutschmanndev.helloworld.router;

import ch.adeutschmanndev.helloworld.resource.HelloWorldHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.http.MediaType;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class HelloWorldRouter {
    @Bean
    public RouterFunction<ServerResponse> helloWorldRoutes(HelloWorldHandler handler) {
        return RouterFunctions
                .route(GET("/hello").and(accept(MediaType.APPLICATION_JSON)), handler::hello);
    }
}
```

### 4. Application Configuration

**application.yml:**
```yaml
server:
  port: 8080
  servlet:
    context-path: /

spring:
  application:
    name: helloworld
  profiles:
    active: default

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized

logging:
  level:
    ch.adeutschmanndev.helloworld: INFO
    org.springframework: INFO
```

### 5. Comprehensive Testing Suite

Create tests in `src/test/java/ch/adeutschmanndev/helloworld/`:

**HelloworldApplicationTests.java:**
```java
package ch.adeutschmanndev.helloworld;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class HelloworldApplicationTests {
    @Test
    void contextLoads() {
        // Validates Spring context loads successfully
    }
}
```

**HelloWorldHandlerTest.java:**
```java
package ch.adeutschmanndev.helloworld.resource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.mock;

class HelloWorldHandlerTest {
    private HelloWorldHandler handler;

    @BeforeEach
    void setUp() {
        handler = new HelloWorldHandler();
    }

    @Test
    void testHelloEndpoint() {
        ServerRequest request = mock(ServerRequest.class);
        
        Mono<ServerResponse> response = handler.hello(request);
        
        StepVerifier.create(response)
                .expectNextMatches(serverResponse -> 
                    serverResponse.statusCode().is2xxSuccessful())
                .verifyComplete();
    }
}
```

### 6. Multi-Stage Containerization (Containerfile)

Create a production-ready multi-stage container build:

```dockerfile
# Build stage - Use Eclipse Temurin JDK 21 Alpine for compilation
FROM eclipse-temurin:21-jdk-alpine AS builder

# Create non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

# Copy build files with proper ownership
COPY --chown=appuser:appgroup mvnw pom.xml ./
COPY --chown=appuser:appgroup .mvn .mvn

# Install dependencies (cached layer)
USER appuser
RUN ./mvnw dependency:go-offline -B

# Copy source and build
COPY --chown=appuser:appgroup src src
RUN ./mvnw clean package -DskipTests -B

# Runtime stage - Use Eclipse Temurin JRE 21 Alpine for smaller footprint
FROM eclipse-temurin:21-jre-alpine

# Install security updates and essential tools
RUN apk update && apk upgrade && \
    apk add --no-cache curl && \
    rm -rf /var/cache/apk/*

# Create non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Create app directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder --chown=appuser:appgroup /app/target/*.jar app.jar

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 7. GitHub Actions CI/CD Pipeline

Create `.github/workflows/` directory with two workflows:

**build-and-deploy.yml (JAR Build & Deploy):**
```yaml
name: Build and Deploy JAR

on:
  push:
    branches: [ main, master ]
  pull_request:
    branches: [ main, master ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up Java 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'eclipse-temurin'
        
    - name: Cache Maven dependencies
      uses: actions/cache@v4
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        
    - name: Run tests
      run: mvn clean test
      
    - name: Build JAR
      run: mvn clean package -DskipTests
      
    - name: Upload artifacts
      uses: actions/upload-artifact@v4
      with:
        name: jar-artifact
        path: target/*.jar

  deploy-to-nexus:
    needs: build
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main' || github.ref == 'refs/heads/master'
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up Java 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'eclipse-temurin'
        
    - name: Deploy to Nexus
      run: |
        # Extract version, deploy, and create tags
        mvn deploy -DskipTests
        # Version management logic here
```

**build-container-image.yml (Container Build & Push):**
```yaml
name: Build and Push Container Image

on:
  push:
    branches: [ main, master ]
  pull_request:
    branches: [ main, master ]
  workflow_dispatch:

jobs:
  build-container:
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up Podman
      run: |
        sudo apt-get update
        sudo apt-get install -y podman
        
    - name: Build container image
      run: |
        podman build -t helloworld:latest .
        
    - name: Push to registry
      if: github.ref == 'refs/heads/main' || github.ref == 'refs/heads/master'
      run: |
        # Push logic for container registry
        echo "Pushing container image..."
```

### 8. Comprehensive Documentation

Create all documentation files in `docs/` directory:

**API_DOCUMENTATION.md** - Complete API endpoint documentation
**SPRING_BOOT_SETUP.md** - Spring Boot 3.5.5 and Java 21 setup guide
**FUNCTIONAL_WEB_FRAMEWORK.md** - Functional web framework implementation
**CONTAINER_BUILD_STRATEGY.md** - Multi-stage container build explanation
**GITHUB_ACTIONS_CICD.md** - CI/CD pipeline documentation
**TESTING_STRATEGY.md** - Comprehensive testing approach
**NEXUS_INTEGRATION.md** - Nexus Repository integration
**NEXUS_SETUP_GUIDE.md** - Nexus setup and configuration
**SECURITY_PRACTICES.md** - Security implementation guidelines
**LOCAL_DEVELOPMENT.md** - Local development environment setup
**PRODUCTION_DEPLOYMENT.md** - Production deployment strategies
**PODMAN_CONTAINERIZATION.md** - Podman containerization guide
**CONTAINER_DEPLOYMENT_GUIDE.md** - Container deployment guide
**VERSION_MANAGEMENT.md** - Version management and release strategy

Each documentation file should be comprehensive, include code examples, configuration snippets, and detailed explanations.

### 9. Project Files

Create the following additional files:
- **README.md** - Project overview and quick start guide
- **HELP.md** - Getting started help
- **mvnw** and **mvnw.cmd** - Maven wrapper scripts
- **.mvn/wrapper/** - Maven wrapper configuration

### 10. Security Features

Implement comprehensive security practices:
- Non-root user in containers
- Security scanning integration
- Dependency vulnerability management
- Secure configuration practices
- HTTPS configuration options
- Security headers implementation

### 11. Nexus Integration

Configure Maven for Nexus Repository integration:
- Distribution management configuration
- Repository proxy settings
- Artifact deployment configuration
- Container registry integration

## Git Workflow Instructions

### Step 1: Initialize Repository
```bash
git init
git add .
git commit -m "Initial project setup"
```

### Step 2: Create Feature Branch
```bash
git checkout -b feature/initialize
git add .
git commit -m "Complete Spring Boot HelloWorld implementation with comprehensive documentation and CI/CD pipeline"
```

### Step 3: Push to Remote
```bash
git remote add origin https://github.com/YOUR_USERNAME/helloworld.git
git push -u origin feature/initialize
```

### Step 4: Create Pull Request

Create a pull request from `feature/initialize` to `main` with the following comprehensive summary:

**Pull Request Title:** Complete Spring Boot HelloWorld Implementation with Production-Ready Features

**Pull Request Description:**
```markdown
# 🚀 Complete Spring Boot HelloWorld Implementation

## Summary
This pull request introduces a comprehensive, production-ready Spring Boot HelloWorld application demonstrating modern development practices, enterprise integration, and complete DevOps workflow.

## ✨ Features Implemented

### 🏗️ Core Application Architecture
- **Spring Boot 3.5.5** with Java 21 support
- **Functional Web Framework** using RouterFunction and Handler patterns
- **RESTful API** with JSON response handling
- **Modern Spring Configuration** with component-based architecture

### 🔧 Development Infrastructure
- **Maven Build System** with comprehensive dependency management
- **Spring Boot Starters** for web and testing capabilities
- **Java 21 Features** utilization (Records, Pattern Matching, Virtual Threads)
- **YAML Configuration** with environment-specific profiles

### 🐳 Containerization & Deployment
- **Multi-Stage Docker Build** for optimized production images
- **Podman Integration** for container management
- **OpenShift Compatibility** for enterprise deployment
- **Security-Hardened Containers** with non-root user execution
- **Alpine Linux Base** for minimal attack surface

### 🔄 CI/CD Pipeline
- **Dual GitHub Actions Workflows**:
  - JAR Build & Deploy to Nexus Repository
  - Container Build & Push to Registry
- **Intelligent Version Management** with automatic tagging
- **Conditional Deployment** based on branch strategy
- **Automated Testing** integration

### 🏢 Enterprise Integration
- **Nexus Repository Management** for artifacts and containers
- **Maven Release Pipeline** with snapshot and release repositories
- **Container Registry** integration
- **Dependency Proxy** configuration

### 🧪 Testing Strategy
- **Comprehensive Testing Pyramid** (Unit, Integration, E2E)
- **Spring Boot Test Framework** integration
- **Handler Unit Tests** for business logic validation
- **Router Integration Tests** for endpoint verification
- **Application Context Tests** for configuration validation

### 📚 Documentation (14 Comprehensive Guides)
- Complete API documentation with endpoint specifications
- Spring Boot setup and configuration guide
- Functional web framework implementation details
- Container build strategy and optimization
- GitHub Actions CI/CD pipeline documentation
- Testing strategy and best practices
- Nexus integration and artifact management
- Security practices and guidelines
- Local development environment setup
- Production deployment strategies
- Podman containerization guide
- Container deployment instructions
- Version management and release strategy

### 🔒 Security Features
- **Container Security** with non-root user execution
- **Dependency Scanning** through Nexus integration
- **Security Best Practices** documentation
- **Production Configuration** with security considerations
- **Vulnerability Management** workflow

## 🎯 Technical Highlights

### Modern Spring Boot Patterns
- Functional Web Framework instead of traditional @Controller
- RouterFunction for explicit, type-safe routing
- ServerRequest/ServerResponse for functional request handling
- Component-based architecture with dependency injection

### Production-Ready Features
- Multi-stage container builds for optimal image size
- Automated CI/CD pipelines with comprehensive testing
- Enterprise artifact management with Nexus
- Security-hardened deployment configuration

### Developer Experience
- Extensive documentation covering all project aspects
- Local development setup guides
- Container-based development workflow
- Automated version management and deployment

## 📊 Project Structure
```
├── src/main/java/ch/adeutschmanndev/helloworld/
│   ├── HelloworldApplication.java           # Spring Boot main class
│   ├── resource/HelloWorldHandler.java     # Functional handler
│   └── router/HelloWorldRouter.java        # Route configuration
├── src/main/resources/application.yml      # Application configuration
├── src/test/java/                          # Comprehensive test suite
├── docs/ (14 documentation files)          # Extensive documentation
├── .github/workflows/                      # CI/CD pipelines
├── Containerfile                           # Multi-stage container build
└── pom.xml                                # Maven configuration
```

## 🔍 Code Quality & Testing
- Clean architecture with separation of concerns
- Functional programming patterns
- Type safety throughout the application
- Comprehensive test coverage across all layers
- Automated quality gates in CI/CD pipeline

## 🚢 Deployment Strategy
- Feature branch development with PR validation
- Automated testing on all branches
- Conditional deployment to production from main branch
- Version tagging and artifact management
- Container registry integration

## 📋 Validation Checklist
- ✅ Unit tests for all handler components
- ✅ Integration tests for router configuration
- ✅ Application context loading verification
- ✅ Container build and security validation
- ✅ CI/CD pipeline functionality
- ✅ Documentation completeness
- ✅ Security practices implementation

## 🎉 Production Readiness
This implementation is production-ready featuring:
- Enterprise-grade CI/CD pipeline
- Secure container deployment strategy
- Comprehensive monitoring and documentation
- Scalable architecture patterns
- Security best practices implementation

## 📋 Testing Instructions
1. Clone the repository
2. Run `mvn clean test` for unit tests
3. Run `mvn spring-boot:run` for local development
4. Build container with `podman build -t helloworld .`
5. Test API endpoint: `curl http://localhost:8080/hello`

## 🔗 Documentation Links
- [API Documentation](docs/API_DOCUMENTATION.md)
- [Spring Boot Setup](docs/SPRING_BOOT_SETUP.md)
- [Container Strategy](docs/CONTAINER_BUILD_STRATEGY.md)
- [CI/CD Pipeline](docs/GITHUB_ACTIONS_CICD.md)
- [Testing Strategy](docs/TESTING_STRATEGY.md)
- [Security Practices](docs/SECURITY_PRACTICES.md)

This implementation demonstrates enterprise-grade Spring Boot development with modern patterns, comprehensive testing, security best practices, and complete DevOps integration.
```

## Additional Instructions for GPT

1. **Create ALL files mentioned** - Don't skip any documentation or configuration files
2. **Include complete code examples** in documentation files with proper syntax highlighting
3. **Ensure consistency** across all configuration files and documentation
4. **Add detailed comments** in code files explaining functionality
5. **Create comprehensive test coverage** for all components
6. **Include proper error handling** and logging throughout the application
7. **Follow Spring Boot 3.5.5 best practices** and modern Java 21 patterns
8. **Implement security best practices** in all aspects of the project
9. **Create production-ready configuration** for all environments
10. **Generate the pull request exactly as specified** with the comprehensive summary

## Success Criteria
- Complete Spring Boot 3.5.5 application with Java 21
- All 14 documentation files created with comprehensive content
- Functional web framework implementation
- Multi-stage container build
- Complete CI/CD pipeline with GitHub Actions
- Comprehensive testing suite
- Nexus integration configuration
- Security best practices implementation
- Feature branch created and pushed
- Pull request created with detailed summary

This prompt should result in a complete, production-ready Spring Boot project that matches the comprehensive structure and features of the original project.
