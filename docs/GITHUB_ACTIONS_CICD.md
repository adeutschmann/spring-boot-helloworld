# GitHub Actions CI/CD Pipeline

This document explains the comprehensive CI/CD pipeline implemented using GitHub Actions for automated building, testing, and deployment.

## Pipeline Overview

The project implements two specialized GitHub Actions workflows:

1. **JAR Build & Deploy** (`build-and-deploy.yml`) - Maven artifact management
2. **Container Build & Push** (`build-container-image.yml`) - Podman-based container builds

## Workflow Architecture

### Conditional Deployment Strategy

**Build Triggers:**
- Push to `main`/`master` branches
- Pull requests to `main`/`master`
- Manual workflow dispatch

**Deployment Logic:**
- **Feature branches/PRs**: Build and test only (validation)
- **Main/master merge**: Build, test, and deploy to Nexus

## JAR Build & Deploy Workflow

### Workflow Structure

```yaml
jobs:
  build:
    - Checkout code
    - Set up Java 21
    - Cache Maven dependencies
    - Run tests
    - Build JAR
    - Upload artifacts

  deploy-to-nexus:
    needs: build
    if: main/master branch push
    - Extract and increment version
    - Deploy to Nexus
    - Create Git tags
    - Update to next SNAPSHOT
```

### Intelligent Version Management

```bash
# Version extraction and manipulation
CURRENT_VERSION=$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)
RELEASE_VERSION=$(echo $CURRENT_VERSION | sed 's/-SNAPSHOT//')
NEW_PATCH=$((PATCH + 1))
NEXT_SNAPSHOT_VERSION="$MAJOR.$MINOR.$((NEW_PATCH + 1))-SNAPSHOT"
```

**Version Flow Example:**
```
Start: 0.0.1-SNAPSHOT
  ↓ (extract and increment)
Deploy: 0.0.2 (clean release)
  ↓ (create tag)
Tag: v0.0.2
  ↓ (bump version)
Next: 0.0.3-SNAPSHOT
```

### Maven Configuration for Nexus

```xml
<distributionManagement>
    <repository>
        <id>nexus-releases</id>
        <url>${nexus.url}/repository/maven-releases/</url>
    </repository>
    <snapshotRepository>
        <id>nexus-snapshots</id>
        <url>${nexus.url}/repository/maven-snapshots/</url>
    </snapshotRepository>
</distributionManagement>
```

### Nexus Authentication

```yaml
- name: Configure Maven settings for Nexus
  run: |
    mkdir -p ~/.m2
    cat > ~/.m2/settings.xml << EOF
    <settings>
      <servers>
        <server>
          <id>nexus-releases</id>
          <username>${env.NEXUS_USERNAME}</username>
          <password>${env.NEXUS_PASSWORD}</password>
        </server>
      </servers>
    </settings>
    EOF
```

## Container Build & Push Workflow

### Podman-Based Build Process

```yaml
- name: Install Podman
  run: |
    sudo apt-get update
    sudo apt-get install -y podman
    sudo sysctl kernel.unprivileged_userns_clone=1

- name: Build container image with Podman
  run: |
    podman build \
      --tag local-build \
      --file ./Containerfile \
      --label "org.opencontainers.image.version=${{ version }}" \
      .
```

### Image Tagging Strategy

**Release Tags (main/master):**
```bash
# Version-specific tag
your-nexus-registry.com:8082/spring-boot-helloworld:0.0.2

# Latest tag
your-nexus-registry.com:8082/spring-boot-helloworld:latest
```

**Development Tags:**
```bash
# Feature branch
your-nexus-registry.com:8082/spring-boot-helloworld:0.0.1-SNAPSHOT-abc12345

# Pull request
your-nexus-registry.com:8082/spring-boot-helloworld:pr-123
```

### Registry Push Logic

```yaml
- name: Push release images to Nexus
  if: github.event_name == 'push' && (github.ref == 'refs/heads/main' || github.ref == 'refs/heads/master')
  run: |
    podman push ${{ steps.version.outputs.primary_tag }}
    podman push ${{ steps.version.outputs.latest_tag }}
```

## Security & Secrets Management

### Required GitHub Secrets

1. **NEXUS_USERNAME** - Nexus repository username
2. **NEXUS_PASSWORD** - Nexus repository password
3. **NEXUS_URL** - Nexus instance URL (https://nexus.company.com)
4. **NEXUS_DOCKER_REGISTRY** - Docker registry URL (nexus.company.com:8082)

### Secret Usage

```yaml
env:
  NEXUS_USERNAME: ${{ secrets.NEXUS_USERNAME }}
  NEXUS_PASSWORD: ${{ secrets.NEXUS_PASSWORD }}
  REGISTRY: ${{ secrets.NEXUS_DOCKER_REGISTRY }}
```

### Secure Authentication

```yaml
- name: Login to Nexus registry
  run: |
    echo "${{ secrets.NEXUS_PASSWORD }}" | podman login \
      --username "${{ secrets.NEXUS_USERNAME }}" \
      --password-stdin \
      ${{ env.REGISTRY }}
```

## Caching Strategies

### Maven Dependency Caching

```yaml
- name: Cache Maven dependencies
  uses: actions/cache@v4
  with:
    path: ~/.m2
    key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
    restore-keys: ${{ runner.os }}-m2
```

**Benefits:**
- Faster build times (dependencies cached between runs)
- Reduced network usage
- More reliable builds (less dependency on external repositories)

### GitHub Actions Artifact Storage

```yaml
- name: Upload JAR artifact
  uses: actions/upload-artifact@v4
  with:
    name: spring-boot-helloworld-jar-${{ github.sha }}
    path: target/*.jar
    retention-days: 30
```

## Build Summaries & Reporting

### Deployment Summary Generation

```yaml
- name: Upload deployment summary
  run: |
    echo "## Deployment Summary 🚀" >> $GITHUB_STEP_SUMMARY
    echo "- **Released Version**: ${{ steps.version.outputs.release_version }}" >> $GITHUB_STEP_SUMMARY
    echo "- **Next Development Version**: ${{ steps.version.outputs.next_snapshot_version }}" >> $GITHUB_STEP_SUMMARY
    echo "- **Git Tag**: v${{ steps.version.outputs.release_version }}" >> $GITHUB_STEP_SUMMARY
```

### Container Build Summary

```yaml
echo "## Container Build Summary 🐳" >> $GITHUB_STEP_SUMMARY
echo "- **Build Tool**: Podman" >> $GITHUB_STEP_SUMMARY
echo "- **Primary Tag**: ${{ steps.version.outputs.primary_tag }}" >> $GITHUB_STEP_SUMMARY
echo "- **Registry**: ${{ env.REGISTRY }}" >> $GITHUB_STEP_SUMMARY
```

## Git Operations & Tagging

### Automatic Git Tagging

```yaml
- name: Create Git tag for release
  run: |
    git config --local user.email "action@github.com"
    git config --local user.name "GitHub Action"
    git tag -a "v${{ version }}" -m "Release version ${{ version }}"
    git push origin "v${{ version }}"
```

### Version Bump Commits

```yaml
- name: Update to next snapshot version
  run: |
    ./mvnw versions:set -DnewVersion=${{ next_version }} -DgenerateBackupPoms=false
    git add pom.xml
    git commit -m "chore: bump version to ${{ next_version }}"
    git push origin ${{ github.ref_name }}
```

## Quality Gates & Testing

### Automated Testing

```yaml
- name: Run tests
  run: ./mvnw test

- name: Build JAR
  run: ./mvnw clean package -DskipTests
```

### Build Validation

- **Unit Tests**: Comprehensive test execution
- **Integration Tests**: Full application context testing
- **Compilation**: Error-free compilation validation
- **Packaging**: Successful JAR creation

## Workflow Optimization

### Parallel Job Execution

```yaml
jobs:
  build-jar:
    runs-on: ubuntu-latest
    # Independent execution
  
  build-container:
    needs: build-jar
    runs-on: ubuntu-latest
    # Depends on JAR build success
```

### Resource Management

- **Runner Selection**: Ubuntu latest for consistent environment
- **Memory Optimization**: Maven opts for efficient builds
- **Timeout Configuration**: Reasonable timeouts for build steps

## Monitoring & Observability

### Build Status Tracking

- GitHub Actions status badges
- Build duration metrics
- Success/failure notifications
- Artifact publication tracking

### Deployment Verification

```yaml
- name: Verify deployment
  run: |
    echo "Artifact deployed to: $NEXUS_URL/repository/maven-releases/"
    echo "Container image: $REGISTRY/spring-boot-helloworld:$VERSION"
```

## Troubleshooting Guide

### Common Issues

**Maven Build Failures:**
```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Verify Maven settings
./mvnw help:effective-settings
```

**Podman Build Issues:**
```bash
# Check Podman installation
podman --version
podman info

# Verify registry connectivity
podman login $REGISTRY
```

**Git Operations Failures:**
```bash
# Verify Git configuration
git config --list

# Check remote access
git remote -v
```

### Best Practices

1. **Conditional Logic**: Use appropriate conditions for deployment steps
2. **Secret Management**: Never expose secrets in logs
3. **Error Handling**: Implement proper error handling and cleanup
4. **Resource Cleanup**: Clean up temporary files and caches
5. **Version Consistency**: Maintain version alignment across artifacts
6. **Documentation**: Keep workflow documentation updated

## Integration Testing

### End-to-End Validation

```yaml
- name: Integration test
  run: |
    # Start application
    java -jar target/*.jar &
    APP_PID=$!
    
    # Wait for startup
    sleep 30
    
    # Test endpoint
    curl -f http://localhost:8080/hello
    
    # Cleanup
    kill $APP_PID
```

This comprehensive CI/CD pipeline ensures reliable, secure, and automated delivery of both JAR artifacts and container images while maintaining high code quality and operational visibility.
