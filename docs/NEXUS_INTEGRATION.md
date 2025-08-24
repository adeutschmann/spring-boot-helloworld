# Nexus Integration

This document explains the comprehensive integration with Nexus Repository Manager for both Maven artifacts and Docker container registry management.

## Overview

Nexus Repository Manager serves as the central hub for:
- **Maven Artifacts**: JAR files, POMs, and metadata
- **Container Images**: Docker/OCI-compliant container registry
- **Dependency Management**: Proxy for external repositories
- **Security Scanning**: Vulnerability assessment and policy enforcement

## Nexus Repository Structure

### Maven Repositories

**Release Repository (`maven-releases`):**
```
ch/adeutschmanndev/helloworld/
├── 0.0.2/
│   ├── helloworld-0.0.2.jar
│   ├── helloworld-0.0.2.pom
│   └── helloworld-0.0.2.jar.sha1
├── 0.0.3/
│   ├── helloworld-0.0.3.jar
│   ├── helloworld-0.0.3.pom
│   └── helloworld-0.0.3.jar.sha1
└── maven-metadata.xml
```

**Snapshot Repository (`maven-snapshots`):**
```
ch/adeutschmanndev/helloworld/
└── 0.0.4-SNAPSHOT/
    ├── helloworld-0.0.4-20250824.103000-1.jar
    ├── helloworld-0.0.4-20250824.103000-1.pom
    ├── maven-metadata.xml
    └── resolver-status.properties
```

### Container Registry

**Repository Structure:**
```
spring-boot-helloworld/
├── latest
├── 0.0.2
├── 0.0.3
├── 0.0.4-SNAPSHOT-abc12345
└── pr-123
```

**Registry URL Format:**
```
your-nexus-instance.com:8082/spring-boot-helloworld:tag
```

## Maven Integration

### Distribution Management Configuration

**POM Configuration (`pom.xml`):**
```xml
<distributionManagement>
    <repository>
        <id>nexus-releases</id>
        <name>Nexus Release Repository</name>
        <url>${nexus.url}/repository/maven-releases/</url>
    </repository>
    <snapshotRepository>
        <id>nexus-snapshots</id>
        <name>Nexus Snapshot Repository</name>
        <url>${nexus.url}/repository/maven-snapshots/</url>
    </snapshotRepository>
</distributionManagement>
```

### Maven Settings Configuration

**Local Development (`~/.m2/settings.xml`):**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0">
    <servers>
        <server>
            <id>nexus-releases</id>
            <username>your-nexus-username</username>
            <password>your-nexus-password</password>
        </server>
        <server>
            <id>nexus-snapshots</id>
            <username>your-nexus-username</username>
            <password>your-nexus-password</password>
        </server>
    </servers>
    
    <profiles>
        <profile>
            <id>nexus</id>
            <repositories>
                <repository>
                    <id>nexus-public</id>
                    <url>https://your-nexus-instance.com/repository/maven-public/</url>
                    <releases><enabled>true</enabled></releases>
                    <snapshots><enabled>true</enabled></snapshots>
                </repository>
            </repositories>
            <pluginRepositories>
                <pluginRepository>
                    <id>nexus-public</id>
                    <url>https://your-nexus-instance.com/repository/maven-public/</url>
                    <releases><enabled>true</enabled></releases>
                    <snapshots><enabled>true</enabled></snapshots>
                </pluginRepository>
            </pluginRepositories>
        </profile>
    </profiles>
    
    <activeProfiles>
        <activeProfile>nexus</activeProfile>
    </activeProfiles>
</settings>
```

### CI/CD Maven Settings

**GitHub Actions Configuration:**
```yaml
- name: Configure Maven settings for Nexus
  run: |
    mkdir -p ~/.m2
    cat > ~/.m2/settings.xml << EOF
    <?xml version="1.0" encoding="UTF-8"?>
    <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0">
      <servers>
        <server>
          <id>nexus-releases</id>
          <username>\${env.NEXUS_USERNAME}</username>
          <password>\${env.NEXUS_PASSWORD}</password>
        </server>
        <server>
          <id>nexus-snapshots</id>
          <username>\${env.NEXUS_USERNAME}</username>
          <password>\${env.NEXUS_PASSWORD}</password>
        </server>
      </servers>
    </settings>
    EOF
  env:
    NEXUS_USERNAME: ${{ secrets.NEXUS_USERNAME }}
    NEXUS_PASSWORD: ${{ secrets.NEXUS_PASSWORD }}
```

## Container Registry Integration

### Registry Authentication

**Podman Login:**
```bash
# Login to Nexus Docker registry
echo "$NEXUS_PASSWORD" | podman login \
  --username "$NEXUS_USERNAME" \
  --password-stdin \
  your-nexus-instance.com:8082
```

**Docker Login:**
```bash
# Login to Nexus Docker registry
echo "$NEXUS_PASSWORD" | docker login \
  --username "$NEXUS_USERNAME" \
  --password-stdin \
  your-nexus-instance.com:8082
```

### Image Tagging and Push

**Release Images:**
```bash
# Tag for release
podman tag local-build your-nexus-instance.com:8082/spring-boot-helloworld:0.0.2
podman tag local-build your-nexus-instance.com:8082/spring-boot-helloworld:latest

# Push to registry
podman push your-nexus-instance.com:8082/spring-boot-helloworld:0.0.2
podman push your-nexus-instance.com:8082/spring-boot-helloworld:latest
```

**Development Images:**
```bash
# Tag for development
podman tag local-build your-nexus-instance.com:8082/spring-boot-helloworld:0.0.3-SNAPSHOT-abc12345

# Push development image
podman push your-nexus-instance.com:8082/spring-boot-helloworld:0.0.3-SNAPSHOT-abc12345
```

## Nexus Repository Configuration

### Creating Maven Repositories

**1. Maven Hosted Repository (Releases):**
```
Name: maven-releases
Version Policy: Release
Layout Policy: Strict
Deployment Policy: Allow redeploy
```

**2. Maven Hosted Repository (Snapshots):**
```
Name: maven-snapshots
Version Policy: Snapshot
Layout Policy: Strict
Deployment Policy: Allow redeploy
```

**3. Maven Proxy Repository (Central):**
```
Name: maven-central
Remote Storage: https://repo1.maven.org/maven2/
Version Policy: Release
Layout Policy: Strict
```

**4. Maven Group Repository (Public):**
```
Name: maven-public
Group Members:
  - maven-releases
  - maven-snapshots
  - maven-central
```

### Creating Docker Repository

**Docker Hosted Repository:**
```
Name: docker-hosted
HTTP Port: 8082
Enable Docker V1 API: No
Blob Store: default
Force Basic Authentication: Yes
```

**Docker Proxy Repository (Docker Hub):**
```
Name: docker-hub
Remote Storage: https://registry-1.docker.io
Docker Index: Use Docker Hub
```

**Docker Group Repository:**
```
Name: docker-group
HTTP Port: 8083
Group Members:
  - docker-hosted
  - docker-hub
```

## Security Configuration

### User Management

**Developer Role Permissions:**
```
- nx-repository-view-maven2-maven-public-read
- nx-repository-view-maven2-maven-releases-*
- nx-repository-view-maven2-maven-snapshots-*
- nx-repository-view-docker-docker-hosted-*
- nx-repository-view-docker-docker-group-read
```

**CI/CD Service Account:**
```
Username: ci-service-account
Roles: nx-deployment
Permissions:
  - Maven: deploy to releases and snapshots
  - Docker: push to hosted registry
  - Read: all public repositories
```

### SSL/TLS Configuration

**Enable HTTPS:**
```
SSL Certificate: Let's Encrypt or Corporate CA
HTTP Redirect: Enable (redirect HTTP to HTTPS)
HSTS: Enable for enhanced security
```

**Docker Registry SSL:**
```
Registry URL: https://your-nexus-instance.com:8082
Certificate: Must be trusted by Docker/Podman clients
```

## Repository Cleanup Policies

### Maven Cleanup Policy

**Release Cleanup:**
```yaml
Name: maven-releases-cleanup
Format: maven2
Criteria:
  - Asset age: Keep assets used in last 180 days
  - Component count: Keep last 10 versions
  - Component age: Remove components older than 365 days
```

**Snapshot Cleanup:**
```yaml
Name: maven-snapshots-cleanup
Format: maven2
Criteria:
  - Asset age: Keep assets used in last 30 days
  - Component count: Keep last 5 snapshots per version
  - Component age: Remove components older than 90 days
```

### Docker Cleanup Policy

**Container Image Cleanup:**
```yaml
Name: docker-cleanup
Format: docker
Criteria:
  - Asset age: Keep images used in last 90 days
  - Component count: Keep last 5 tags per repository
  - Tag pattern: Keep tags matching 'latest', 'v*.*.*'
```

## Artifact Deployment

### Manual Deployment

**Deploy JAR to Nexus:**
```bash
# Deploy release version
./mvnw deploy -DskipTests -Dnexus.url=https://your-nexus-instance.com

# Deploy specific version
./mvnw deploy:deploy-file \
  -DgroupId=ch.adeutschmanndev \
  -DartifactId=helloworld \
  -Dversion=0.0.2 \
  -Dpackaging=jar \
  -Dfile=target/helloworld-0.0.2.jar \
  -DrepositoryId=nexus-releases \
  -Durl=https://your-nexus-instance.com/repository/maven-releases/
```

### Automated Deployment

**GitHub Actions Integration:**
```yaml
- name: Deploy to Nexus
  run: ./mvnw deploy -DskipTests -Dnexus.url=${{ secrets.NEXUS_URL }}
  env:
    NEXUS_USERNAME: ${{ secrets.NEXUS_USERNAME }}
    NEXUS_PASSWORD: ${{ secrets.NEXUS_PASSWORD }}
```

## Dependency Resolution

### Using Nexus as Proxy

**Repository Priority:**
1. **Local Repository** (`~/.m2/repository`)
2. **Nexus Group Repository** (maven-public)
   - Hosted repositories (releases, snapshots)
   - Proxy repositories (Maven Central, etc.)
3. **Fallback**: Direct to Maven Central (if configured)

**Benefits:**
- **Faster Builds**: Cached dependencies
- **Reliability**: Offline access to dependencies
- **Security**: Controlled dependency sources
- **Compliance**: Audit trail of all dependencies

### Dependency Configuration

**Force Nexus Usage:**
```xml
<repositories>
    <repository>
        <id>nexus-public</id>
        <url>https://your-nexus-instance.com/repository/maven-public/</url>
        <releases><enabled>true</enabled></releases>
        <snapshots><enabled>true</enabled></snapshots>
    </repository>
</repositories>
```

## Monitoring and Analytics

### Repository Health Monitoring

**Key Metrics:**
- Repository size and growth
- Download/upload statistics
- Error rates and availability
- Security vulnerabilities found

**Nexus IQ Integration:**
```yaml
# Component vulnerability scanning
nexus-iq:
  enabled: true
  server-url: https://your-nexus-iq-instance.com
  application-id: spring-boot-helloworld
  scan-targets:
    - target/*.jar
```

### Usage Analytics

**Maven Repository Analytics:**
```
- Most downloaded artifacts
- Version distribution
- Unique downloads per day/week/month
- Geographic distribution of downloads
```

**Docker Registry Analytics:**
```
- Image pull statistics
- Tag usage patterns
- Image size trends
- Vulnerability scan results
```

## Troubleshooting

### Common Maven Issues

**Authentication Failed:**
```bash
# Verify credentials
curl -u username:password \
  https://your-nexus-instance.com/repository/maven-public/

# Check settings.xml server ID matches POM distributionManagement
```

**Deploy Failed:**
```bash
# Check repository permissions
# Verify repository accepts the artifact type (release vs snapshot)
# Ensure deployment policy allows uploads
```

### Common Docker Issues

**Push Failed:**
```bash
# Verify registry login
podman login your-nexus-instance.com:8082

# Check push permissions
# Ensure repository exists and is configured correctly
```

**Pull Failed:**
```bash
# Verify image exists
curl -u username:password \
  https://your-nexus-instance.com:8082/v2/spring-boot-helloworld/tags/list

# Check pull permissions
```

### Network Connectivity

**Test Nexus Connectivity:**
```bash
# Test HTTP connectivity
curl -I https://your-nexus-instance.com

# Test Maven repository
curl -u username:password \
  https://your-nexus-instance.com/repository/maven-public/

# Test Docker registry
curl -u username:password \
  https://your-nexus-instance.com:8082/v2/
```

## Best Practices

### Repository Management

1. **Separate Release and Snapshot** repositories
2. **Use repository groups** for simplified client configuration
3. **Implement cleanup policies** to manage storage
4. **Enable security scanning** for vulnerability detection
5. **Monitor repository health** and usage patterns

### Security Best Practices

1. **Use dedicated service accounts** for CI/CD
2. **Implement least privilege** access controls
3. **Enable SSL/TLS** for all communications
4. **Regular security audits** of stored artifacts
5. **Backup repository data** regularly

### Performance Optimization

1. **Configure appropriate blob stores** for different content types
2. **Use SSD storage** for frequently accessed repositories
3. **Implement CDN** for global artifact distribution
4. **Monitor and tune** JVM settings for Nexus
5. **Regular maintenance** and cleanup operations

This comprehensive Nexus integration provides enterprise-grade artifact and container registry management with security, scalability, and operational efficiency.
