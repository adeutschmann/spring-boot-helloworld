# Spring Boot HelloWorld

A comprehensive Spring Boot application demonstrating modern DevOps practices, functional web programming, and automated CI/CD pipelines with intelligent version management and Podman-based containerization.

## 🚀 Features Overview

- **Spring Boot 3.5.5** with Java 21 (latest LTS)
- **Functional web framework** with RouterFunction pattern
- **Automated CI/CD pipelines** with GitHub Actions
- **Intelligent version management** with automatic SNAPSHOT handling
- **Podman-based containerization** with security-first approach
- **Nexus integration** for artifact and container registry management
- **Production-ready** with comprehensive monitoring and security

## 📋 Quick Start

### Prerequisites
- Java 21
- Maven 3.6+
- Podman or Docker (for containerization)

### Local Development
```bash
# Clone the repository
git clone https://github.com/adeutschmann/spring-boot-helloworld.git
cd spring-boot-helloworld

# Run the application
./mvnw spring-boot:run

# Test the endpoint
curl http://localhost:8080/hello
```

**Response:**
```json
{
  "message": "Hello World"
}
```

## 🏗️ Architecture

### Project Structure
```
src/main/java/ch/adeutschmanndev/helloworld/
├── HelloworldApplication.java          # Main Spring Boot application
├── resource/
│   └── HelloWorldHandler.java          # Request handler with business logic
└── router/
    └── HelloWorldRouter.java           # RouterFunction configuration
```

### Design Patterns
- **Functional Programming**: Uses Spring's RouterFunction instead of traditional @Controller
- **Clean Architecture**: Separation of concerns with handler and router classes
- **12-Factor App**: Cloud-native design principles
- **Security-First**: Non-root containers and minimal attack surface

## 📚 Comprehensive Feature Documentation

This project demonstrates multiple enterprise-grade features. Each feature has detailed documentation:

### 🎯 Core Application Features
- **[Functional Web Framework](docs/FUNCTIONAL_WEB_FRAMEWORK.md)** - RouterFunction and handler pattern implementation
- **[Spring Boot Configuration](docs/SPRING_BOOT_SETUP.md)** - Modern Spring Boot 3.5.5 with Java 21 setup

### 🐳 Containerization & Deployment
- **[Podman Containerization](docs/PODMAN_CONTAINERIZATION.md)** - Security-first, rootless container builds
- **[Multi-Stage Container Build](docs/CONTAINER_BUILD_STRATEGY.md)** - Optimized Alpine-based production containers
- **[Container Deployment Guide](docs/CONTAINER_DEPLOYMENT_GUIDE.md)** - Comprehensive container deployment instructions

### 🏗️ CI/CD & Automation
- **[GitHub Actions CI/CD](docs/GITHUB_ACTIONS_CICD.md)** - Automated build, test, and deployment pipelines
- **[Intelligent Version Management](docs/VERSION_MANAGEMENT.md)** - Automatic SNAPSHOT removal and version incrementing
- **[Nexus Integration](docs/NEXUS_INTEGRATION.md)** - Artifact and container registry management
- **[Nexus Setup Guide](docs/NEXUS_SETUP_GUIDE.md)** - Step-by-step GitHub secrets and Nexus configuration

### 🔧 Development & Operations
- **[Local Development Setup](docs/LOCAL_DEVELOPMENT.md)** - IDE integration and debugging
- **[Testing Strategy](docs/TESTING_STRATEGY.md)** - Unit testing and validation approaches

### 🔒 Security & Best Practices
- **[Security Implementation](docs/SECURITY_PRACTICES.md)** - Container security, secrets management, and best practices
- **[Production Readiness](docs/PRODUCTION_DEPLOYMENT.md)** - Monitoring, health checks, and operational considerations

## 🚀 Deployment Options

### Container Deployment
```bash
# Using Podman (recommended)
podman pull your-nexus-registry.com:8082/spring-boot-helloworld:latest
podman run -p 8080:8080 your-nexus-registry.com:8082/spring-boot-helloworld:latest

# Using Docker
docker pull your-nexus-registry.com:8082/spring-boot-helloworld:latest
docker run -p 8080:8080 your-nexus-registry.com:8082/spring-boot-helloworld:latest
```

### Local Container Build
```bash
# Build with Podman
podman build -t spring-boot-helloworld:latest -f Containerfile .

# Build with Docker
docker build -t spring-boot-helloworld:latest -f Containerfile .
```

## 🔄 CI/CD Pipeline

### Automated Workflows
- **JAR Deployment**: Automatic Maven artifact deployment to Nexus
- **Container Images**: Podman-based builds for linux/amd64 platform
- **Version Management**: Automatic version incrementing and Git tagging
- **Quality Gates**: Automated testing and build validation

### Container Registry
Container images are automatically built and pushed to Nexus registry:
- **Release images**: `nexus-registry:8082/spring-boot-helloworld:1.0.2`
- **Latest tag**: `nexus-registry:8082/spring-boot-helloworld:latest`
- **Development images**: `nexus-registry:8082/spring-boot-helloworld:1.0.3-SNAPSHOT-abc12345`

## 🎯 Key Innovations

### Zero-Touch Release Management
- Eliminates manual version bumping
- Ensures clean release versions in Nexus (no SNAPSHOT suffixes)
- Maintains development continuity with automatic SNAPSHOT preparation
- Provides complete release traceability through Git tags

### Podman-First Container Strategy
- Implements secure, rootless container builds in CI/CD
- Achieves full Docker compatibility without Docker daemon dependency
- Integrates seamlessly with existing Maven and Nexus workflows
- Provides enhanced security through daemon-free architecture

## 🛠️ Development

### IDE Integration
- **IntelliJ IDEA**: Pre-configured for debug mode
- **Comprehensive .gitignore**: Excludes .idea, target, and build artifacts
- **Maven wrapper**: Ensures consistent build environment

### Container Features
- **Security hardened**: Non-root user (UID 1001), minimal Alpine base
- **Multi-stage build**: Optimized for production deployment
- **Health checks**: Built-in monitoring and liveness probes
- **Resource optimized**: Container-aware JVM settings

## 📊 Monitoring & Operations

- **Health endpoint**: `/hello` serves as health check
- **Container labels**: OCI-compliant metadata for operational insights
- **Logging**: Comprehensive application logging
- **Resource limits**: Kubernetes-ready resource constraints

## 🤝 Contributing

This project demonstrates enterprise-grade patterns and serves as a reference implementation for:
- Modern Spring Boot development practices
- Functional web programming patterns
- DevOps automation with GitHub Actions
- Secure containerization with Podman
- Intelligent release management

## 📖 Additional Resources

- **[Nexus Setup Guide](docs/NEXUS_SETUP_GUIDE.md)** - Step-by-step Nexus and GitHub configuration
- **[Container Deployment Guide](docs/CONTAINER_DEPLOYMENT_GUIDE.md)** - Comprehensive container deployment instructions
- **[API Documentation](docs/API_DOCUMENTATION.md)** - Complete endpoint documentation

---

**Built with ❤️ using Spring Boot, Java 21, Podman, and modern DevOps practices**
