# Container Image Build and Deployment Guide

This guide explains how to build and deploy container images using GitHub Actions with Nexus container registry.

## 🐳 Container Image Workflow

### Workflow Triggers
- **Always builds** container images on:
  - Push to main/master branches
  - Pull requests to main/master branches
  - Manual workflow dispatch

- **Only pushes to registry** on:
  - Push to main/master branches (merge from feature branch)
  - NOT on pull requests (build verification only)

## 🏷️ Image Tagging Strategy

### Release Tags (main/master branch)
When you merge to main/master, the workflow creates:
- `your-nexus-registry.com:8082/spring-boot-helloworld:0.0.2` (clean version)
- `your-nexus-registry.com:8082/spring-boot-helloworld:latest`
- Git tag: `container-v0.0.2`

### Development Tags (feature branches)
For feature branches and PRs:
- `your-nexus-registry.com:8082/spring-boot-helloworld:0.0.1-SNAPSHOT-abc12345`
- `your-nexus-registry.com:8082/spring-boot-helloworld:feature-branch-latest`
- `your-nexus-registry.com:8082/spring-boot-helloworld:pr-123` (for PRs)

## 🔧 Setup Requirements

### GitHub Secrets Needed
1. `NEXUS_USERNAME` - Your Nexus username
2. `NEXUS_PASSWORD` - Your Nexus password
3. `NEXUS_DOCKER_REGISTRY` - Registry URL (e.g., `nexus.company.com:8082`)

### Nexus Configuration
Ensure your Nexus instance has:
- Docker registry configured (usually on port 8082)
- Docker repository created (hosted or group)
- User permissions for pushing images

## 🚀 Container Features

### Multi-Architecture Support
- Builds for both `linux/amd64` and `linux/arm64`
- Uses Docker Buildx for cross-platform builds

### Optimization Features
- **Multi-stage build** with JDK for compilation, JRE for runtime
- **Security hardened** with non-root user (UID 1001)
- **Layer caching** using GitHub Actions cache
- **Minimal base image** using Alpine Linux

### Container Labels
Each image includes OCI-compliant labels:
- `org.opencontainers.image.title`
- `org.opencontainers.image.description`
- `org.opencontainers.image.version`
- `org.opencontainers.image.source`
- `org.opencontainers.image.revision`

## 🧪 Testing Container Images

### Pull and Run Locally
```bash
# Pull the latest image
docker pull your-nexus-registry.com:8082/spring-boot-helloworld:latest

# Run the container
docker run -p 8080:8080 your-nexus-registry.com:8082/spring-boot-helloworld:latest

# Test the endpoint
curl http://localhost:8080/hello
```

### Using with Docker Compose
```yaml
version: '3.8'
services:
  spring-app:
    image: your-nexus-registry.com:8082/spring-boot-helloworld:latest
    ports:
      - "8080:8080"
    environment:
      - JAVA_OPTS=-Xmx512m
```

## 🔍 Workflow Details

### Build Process
1. **JAR Build Job**: Compiles and tests the Spring Boot application
2. **Container Build Job**: Creates multi-platform container image
3. **Version Extraction**: Automatically determines image tags from pom.xml
4. **Registry Push**: Pushes to Nexus (only on main/master)
5. **Git Tagging**: Creates container release tags

### Build Summary
Each workflow run provides a detailed summary including:
- Generated image tags
- Target registry
- Platform architectures
- Push status
- Container run commands

## 🔒 Security Considerations

### Image Scanning
Consider adding container scanning to the workflow:
```yaml
- name: Run Trivy vulnerability scanner
  uses: aquasecurity/trivy-action@master
  with:
    image-ref: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:latest
```

### Registry Authentication
- Uses secure GitHub secrets for Nexus authentication
- Only authenticates when pushing (not for builds)
- Supports standard Docker registry authentication

## 📊 Monitoring and Maintenance

### Image Cleanup
Consider implementing retention policies in Nexus:
- Keep last 10 release versions
- Clean up development tags older than 30 days
- Remove untagged images weekly

### Size Optimization
Current optimizations:
- Multi-stage build reduces final image size
- Alpine-based runtime (~200MB vs ~500MB with full JDK)
- Layer caching improves build times
