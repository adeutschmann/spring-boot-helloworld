# Podman Containerization

This document explains the Podman-based containerization strategy implemented in this project, focusing on security-first, rootless container builds.

## Why Podman over Docker?

### Security Advantages
- **Rootless by Design**: No privileged daemon required
- **Process Isolation**: Each container runs as a separate process
- **No Single Point of Failure**: No central daemon that could be compromised
- **Enhanced Security**: Reduced attack surface compared to Docker daemon

### Technical Benefits
- **OCI Compliance**: Full compatibility with Docker registries and Kubernetes
- **Resource Efficiency**: Lower overhead without daemon process
- **Drop-in Replacement**: Same command-line interface as Docker
- **CI/CD Optimized**: Better suited for ephemeral build environments

## Containerfile Implementation

### Multi-Stage Build Strategy

```dockerfile
# Build stage - Use Eclipse Temurin JDK 21 Alpine for compilation
FROM eclipse-temurin:21-jdk-alpine AS builder

# Security: Create non-root user for build process
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

EXPOSE 8080

# Container-optimized JVM settings
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication"

# Health check using application endpoint
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/hello || exit 1

# Use dumb-init for proper signal handling
ENTRYPOINT ["dumb-init", "--"]
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### Key Security Features

1. **Non-Root Execution**
   - User ID 1001 (non-privileged)
   - Dedicated group for application
   - No root privileges in container

2. **Minimal Base Image**
   - Alpine Linux (small attack surface)
   - Only essential packages installed
   - Regular security updates applied

3. **Process Management**
   - `dumb-init` for proper signal handling
   - Graceful shutdown support
   - Zombie process prevention

## GitHub Actions Integration

### Podman Installation and Configuration

```yaml
- name: Install Podman
  run: |
    # Update package list
    sudo apt-get update
    
    # Install Podman
    sudo apt-get install -y podman
    
    # Verify installation
    podman --version
    
    # Configure Podman for rootless operation
    sudo sysctl kernel.unprivileged_userns_clone=1
```

### Container Build Process

```yaml
- name: Build container image with Podman
  run: |
    echo "🔨 Building container image with Podman..."
    
    podman build \
      --tag local-build \
      --file ./Containerfile \
      --label "org.opencontainers.image.title=spring-boot-helloworld" \
      --label "org.opencontainers.image.version=${{ steps.version.outputs.version }}" \
      .
```

### Registry Operations

```yaml
- name: Login to Nexus registry
  run: |
    echo "${{ secrets.NEXUS_PASSWORD }}" | podman login \
      --username "${{ secrets.NEXUS_USERNAME }}" \
      --password-stdin \
      ${{ env.REGISTRY }}

- name: Push release images to Nexus
  run: |
    podman push ${{ steps.version.outputs.primary_tag }}
    podman push ${{ steps.version.outputs.latest_tag }}
```

## Local Development Workflow

### Building Images Locally

```bash
# Build with Podman (recommended)
podman build -t spring-boot-helloworld:latest -f Containerfile .

# Build with Docker (compatibility)
docker build -t spring-boot-helloworld:latest -f Containerfile .
```

### Running Containers

```bash
# Run with Podman
podman run -p 8080:8080 spring-boot-helloworld:latest

# Run with Docker
docker run -p 8080:8080 spring-boot-helloworld:latest

# Run in background
podman run -d -p 8080:8080 --name hello-app spring-boot-helloworld:latest
```

### Container Management

```bash
# List running containers
podman ps

# View logs
podman logs hello-app

# Stop container
podman stop hello-app

# Remove container
podman rm hello-app

# List images
podman images
```

## Image Optimization Strategies

### Layer Caching
- Maven dependencies installed in separate layer
- Source code changes don't invalidate dependency cache
- Faster subsequent builds

### Size Optimization
- Multi-stage build reduces final image size
- Alpine base image (~200MB vs ~500MB with full JDK)
- Only runtime dependencies in final image

### Security Hardening
- Non-root user throughout build and runtime
- Minimal package installation
- Regular security updates applied

## Container Registry Integration

### Nexus Docker Registry

```bash
# Configure registry access
podman login your-nexus-registry.com:8082

# Pull image from Nexus
podman pull your-nexus-registry.com:8082/spring-boot-helloworld:latest

# Tag for local development
podman tag your-nexus-registry.com:8082/spring-boot-helloworld:latest hello-local:latest
```

### Image Tagging Strategy

**Release Images:**
```bash
# Version tag
your-nexus-registry.com:8082/spring-boot-helloworld:1.0.2

# Latest tag
your-nexus-registry.com:8082/spring-boot-helloworld:latest
```

**Development Images:**
```bash
# Snapshot with commit hash
your-nexus-registry.com:8082/spring-boot-helloworld:1.0.3-SNAPSHOT-abc12345

# Pull request tag
your-nexus-registry.com:8082/spring-boot-helloworld:pr-123
```

## OCI Image Labels

### Metadata Labels
```dockerfile
--label "org.opencontainers.image.title=spring-boot-helloworld"
--label "org.opencontainers.image.description=Spring Boot HelloWorld application"
--label "org.opencontainers.image.version=1.0.2"
--label "org.opencontainers.image.source=https://github.com/user/repo"
--label "org.opencontainers.image.revision=abc12345"
--label "org.opencontainers.image.created=2025-08-24T10:30:00Z"
```

### Benefits of Proper Labeling
- Container registry metadata
- Operational insights
- Security scanning integration
- Automated cleanup policies

## Performance Considerations

### Memory Management
```bash
# Container-aware JVM settings
-XX:+UseContainerSupport
-XX:MaxRAMPercentage=75.0

# Optimized garbage collection
-XX:+UseG1GC
-XX:+UseStringDeduplication
```

### Startup Optimization
- JVM warming strategies
- Application-specific optimizations
- Resource pre-allocation

### Runtime Efficiency
- Minimal resource usage
- Efficient process management
- Optimized health checks

## Troubleshooting

### Common Issues

**Permission Errors:**
```bash
# Ensure user namespace support
sudo sysctl kernel.unprivileged_userns_clone=1

# Check Podman configuration
podman info
```

**Registry Authentication:**
```bash
# Verify registry login
podman login --get-login your-nexus-registry.com:8082

# Test connectivity
podman search your-nexus-registry.com:8082/spring-boot-helloworld
```

**Build Failures:**
```bash
# Debug build process
podman build --no-cache -t debug-build .

# Inspect intermediate layers
podman history debug-build
```

### Best Practices

1. **Security First**: Always run as non-root user
2. **Minimal Images**: Use smallest base image possible
3. **Layer Optimization**: Order layers by change frequency
4. **Health Checks**: Include meaningful health endpoints
5. **Signal Handling**: Use proper init system (dumb-init)
6. **Resource Limits**: Set appropriate CPU and memory limits
7. **Regular Updates**: Keep base images and dependencies updated

## Integration with Kubernetes

### Pod Security Standards
```yaml
securityContext:
  runAsNonRoot: true
  runAsUser: 1001
  runAsGroup: 1001
  capabilities:
    drop:
    - ALL
  allowPrivilegeEscalation: false
```

### Resource Management
```yaml
resources:
  requests:
    memory: "256Mi"
    cpu: "100m"
  limits:
    memory: "512Mi"
    cpu: "500m"
```

This Podman-based approach provides enhanced security, better resource efficiency, and seamless integration with modern container orchestration platforms while maintaining full compatibility with Docker-based workflows.
