# Multi-Stage Container Build Strategy

This document explains the multi-stage container build approach used in this project, focusing on optimization, security, and production readiness.

## Overview

The multi-stage build strategy separates the build environment from the runtime environment, resulting in:
- **Smaller production images** (reduced attack surface)
- **Enhanced security** (no build tools in production)
- **Faster deployments** (smaller image size)
- **Cost efficiency** (reduced storage and transfer costs)

## Build Stage Architecture

### Stage 1: Builder Stage (JDK Environment)

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
RUN ./mvnw dependency:go-offline -B

# Copy source and build
COPY --chown=appuser:appgroup src src
RUN ./mvnw clean package -DskipTests -B
```

**Key Features:**
- **JDK Environment**: Full development kit for compilation
- **Dependency Caching**: Maven dependencies installed in separate layer
- **Security**: Non-root user throughout build process
- **Optimization**: Source code copied after dependencies for better caching

### Stage 2: Runtime Stage (JRE Environment)

```dockerfile
# Runtime stage - Use Eclipse Temurin JRE 21 Alpine for smaller footprint
FROM eclipse-temurin:21-jre-alpine

# Install security updates and essential tools
RUN apk update && \
    apk upgrade && \
    apk add --no-cache dumb-init curl && \
    rm -rf /var/cache/apk/*

# Create non-root user for runtime
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

# Copy JAR from build stage
COPY --from=builder --chown=appuser:appgroup /app/target/*.jar app.jar

# Switch to non-root user
USER appuser

# Runtime configuration
EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:8080/hello || exit 1
ENTRYPOINT ["dumb-init", "--"]
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

**Key Features:**
- **JRE Only**: Minimal runtime environment (no compilation tools)
- **Security Hardening**: Regular updates, non-root execution
- **Process Management**: Proper signal handling with dumb-init
- **Health Monitoring**: Built-in health checks

## Layer Optimization Strategy

### Layer Caching Hierarchy

```dockerfile
# Layer 1: Base image (rarely changes)
FROM eclipse-temurin:21-jdk-alpine AS builder

# Layer 2: System dependencies (rarely changes)
RUN addgroup -g 1001 -S appgroup && adduser -u 1001 -S appuser -G appgroup

# Layer 3: Maven wrapper and POM (changes less frequently)
COPY --chown=appuser:appgroup mvnw pom.xml ./
COPY --chown=appuser:appgroup .mvn .mvn

# Layer 4: Maven dependencies (cached until POM changes)
RUN ./mvnw dependency:go-offline -B

# Layer 5: Application source (changes most frequently)
COPY --chown=appuser:appgroup src src
RUN ./mvnw clean package -DskipTests -B
```

**Caching Benefits:**
- Dependencies are cached until `pom.xml` changes
- Source code changes don't invalidate dependency cache
- Faster subsequent builds (minutes → seconds)
- Reduced network usage and build server load

## Size Optimization Techniques

### Image Size Comparison

| Build Type | Size | Components |
|------------|------|------------|
| Single-stage (JDK) | ~500MB | JDK + Source + Dependencies + Tools |
| Multi-stage (JRE) | ~200MB | JRE + JAR + Runtime essentials |
| **Savings** | **~300MB** | **60% reduction** |

### Optimization Strategies

**1. Alpine Linux Base**
```dockerfile
FROM eclipse-temurin:21-jre-alpine  # ~200MB vs ~400MB for Ubuntu
```

**2. Minimal Package Installation**
```dockerfile
RUN apk add --no-cache dumb-init curl  # Only essential packages
```

**3. Cache Cleanup**
```dockerfile
RUN apk update && apk upgrade && rm -rf /var/cache/apk/*
```

**4. Single Layer Optimization**
```dockerfile
# Combine commands to reduce layers
RUN apk update && \
    apk upgrade && \
    apk add --no-cache dumb-init curl && \
    rm -rf /var/cache/apk/*
```

## Security Hardening

### Multi-Stage Security Benefits

**Build Stage Security:**
- Isolated build environment
- Build tools not exposed in production
- Dependency verification during build
- Secure artifact creation

**Runtime Stage Security:**
- Minimal attack surface (JRE only)
- No development tools in production
- Regular security updates applied
- Non-root execution enforced

### Security Implementation

**1. Non-Root User Creation**
```dockerfile
# Consistent UID/GID across stages
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup
```

**2. File Ownership Management**
```dockerfile
# Secure file ownership from build stage
COPY --from=builder --chown=appuser:appgroup /app/target/*.jar app.jar
```

**3. Security Updates**
```dockerfile
# Latest security patches
RUN apk update && apk upgrade
```

## Build Performance Optimization

### Parallel Build Strategy

**Docker BuildKit (if using Docker):**
```bash
DOCKER_BUILDKIT=1 docker build --progress=plain .
```

**Podman Build Optimization:**
```bash
podman build \
  --layers \
  --cache-from=type=local,src=/tmp/cache \
  --cache-to=type=local,dest=/tmp/cache \
  .
```

### CI/CD Integration

**GitHub Actions Optimization:**
```yaml
- name: Build container image with caching
  run: |
    podman build \
      --tag local-build \
      --file ./Containerfile \
      --cache-from=ghcr.io/user/repo:cache \
      --cache-to=ghcr.io/user/repo:cache \
      .
```

## Development vs Production Builds

### Development Build (Single-Stage)

```dockerfile
# Development Containerfile.dev
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app
COPY . .

# Development tools and hot-reload
RUN ./mvnw dependency:go-offline
EXPOSE 8080 8000

# Debug mode with hot-reload
CMD ["./mvnw", "spring-boot:run", "-Dspring-boot.run.jvmArguments=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000"]
```

### Production Build (Multi-Stage)

```dockerfile
# Production Containerfile (current implementation)
FROM eclipse-temurin:21-jdk-alpine AS builder
# ... build stage ...

FROM eclipse-temurin:21-jre-alpine
# ... optimized runtime stage ...
```

## Advanced Optimization Techniques

### Distroless Images (Alternative)

```dockerfile
# Alternative: Google Distroless
FROM eclipse-temurin:21-jdk-alpine AS builder
# ... build stage ...

FROM gcr.io/distroless/java21-debian12
COPY --from=builder /app/target/*.jar /app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

**Benefits:**
- Even smaller size (~50MB)
- Minimal attack surface
- No shell access

**Trade-offs:**
- Harder to debug
- No package manager
- Limited tooling

### Build Argument Optimization

```dockerfile
ARG MAVEN_OPTS="-Xmx1024m"
ARG SKIP_TESTS="true"

RUN ./mvnw clean package -DskipTests=${SKIP_TESTS} ${MAVEN_OPTS}
```

**Usage:**
```bash
podman build \
  --build-arg MAVEN_OPTS="-Xmx2048m" \
  --build-arg SKIP_TESTS="false" \
  .
```

## Monitoring and Metrics

### Build Time Monitoring

```yaml
# GitHub Actions build metrics
- name: Monitor build time
  run: |
    START_TIME=$(date +%s)
    podman build -t app .
    END_TIME=$(date +%s)
    BUILD_DURATION=$((END_TIME - START_TIME))
    echo "Build completed in ${BUILD_DURATION} seconds"
```

### Image Analysis

```bash
# Analyze image layers
podman history spring-boot-helloworld:latest

# Image size analysis
podman images spring-boot-helloworld:latest

# Security scanning
podman run --rm -v /var/run/docker.sock:/var/run/docker.sock \
  aquasec/trivy image spring-boot-helloworld:latest
```

## Best Practices Summary

### Do's ✅
1. **Use multi-stage builds** for production images
2. **Order layers by change frequency** (dependencies before source)
3. **Use specific base image tags** (not `latest`)
4. **Run as non-root user** in production
5. **Include health checks** for monitoring
6. **Clean up package caches** to reduce size
7. **Use .containerignore** to exclude unnecessary files

### Don'ts ❌
1. **Don't include build tools** in production stage
2. **Don't run as root** in containers
3. **Don't copy unnecessary files** to final stage
4. **Don't use development tools** in production
5. **Don't ignore security updates**
6. **Don't create excessive layers**

## Troubleshooting

### Common Issues

**Large Image Size:**
```bash
# Check layer sizes
podman history --human spring-boot-helloworld:latest

# Find large files
podman run --rm spring-boot-helloworld:latest du -sh /*
```

**Build Cache Issues:**
```bash
# Clear build cache
podman system prune -a

# Force rebuild without cache
podman build --no-cache -t app .
```

**Permission Problems:**
```bash
# Check file permissions in container
podman run --rm spring-boot-helloworld:latest ls -la /app/
```

This multi-stage build strategy provides an optimal balance between build efficiency, security, and production performance while maintaining developer productivity and operational simplicity.
