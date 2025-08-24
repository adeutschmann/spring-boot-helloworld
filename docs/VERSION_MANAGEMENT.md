# Intelligent Version Management

This document explains the automated version management system that eliminates manual version bumping and ensures clean release artifacts.

## Overview

The intelligent version management system automatically handles:
- SNAPSHOT removal for clean releases
- Semantic version incrementing
- Git tag creation for releases
- Automatic preparation of next development version

## Version Flow Architecture

### Automated Version Lifecycle

```
Development: 0.0.1-SNAPSHOT
     ↓ (merge to main/master)
Extract: 0.0.1-SNAPSHOT → base version 0.0.1
     ↓ (increment patch)
Release: 0.0.2 (clean version deployed to Nexus)
     ↓ (create Git tag)
Tag: v0.0.2 (for tracking and rollback)
     ↓ (prepare next development)
Next: 0.0.3-SNAPSHOT (committed back to main)
```

## Implementation Details

### Version Extraction Logic

```bash
# Extract current version from Maven POM
CURRENT_VERSION=$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)
echo "Current version: $CURRENT_VERSION"  # Output: 0.0.1-SNAPSHOT

# Remove SNAPSHOT suffix to get base version
BASE_VERSION=$(echo $CURRENT_VERSION | sed 's/-SNAPSHOT//')
echo "Base version: $BASE_VERSION"  # Output: 0.0.1

# Parse semantic version components
MAJOR=$(echo $BASE_VERSION | cut -d. -f1)  # 0
MINOR=$(echo $BASE_VERSION | cut -d. -f2)  # 0
PATCH=$(echo $BASE_VERSION | cut -d. -f3)  # 1
```

### Version Incrementing Strategy

```bash
# Increment patch version for release
NEW_PATCH=$((PATCH + 1))
RELEASE_VERSION="$MAJOR.$MINOR.$NEW_PATCH"
echo "Release version: $RELEASE_VERSION"  # Output: 0.0.2

# Prepare next development version
NEXT_PATCH=$((NEW_PATCH + 1))
NEXT_SNAPSHOT_VERSION="$MAJOR.$MINOR.$NEXT_PATCH-SNAPSHOT"
echo "Next snapshot: $NEXT_SNAPSHOT_VERSION"  # Output: 0.0.3-SNAPSHOT
```

## Maven Integration

### POM Version Updates

```bash
# Set release version for deployment
./mvnw versions:set -DnewVersion=$RELEASE_VERSION -DgenerateBackupPoms=false

# Deploy clean release version
./mvnw deploy -DskipTests -Dnexus.url=$NEXUS_URL

# Update to next development version
./mvnw versions:set -DnewVersion=$NEXT_SNAPSHOT_VERSION -DgenerateBackupPoms=false
```

### Distribution Management

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

**Repository Selection Logic:**
- Versions ending with `-SNAPSHOT` → `maven-snapshots` repository
- Clean versions (no suffix) → `maven-releases` repository

## Git Operations

### Release Tagging

```bash
# Configure Git for automation
git config --local user.email "action@github.com"
git config --local user.name "GitHub Action"

# Create annotated tag for release
git tag -a "v$RELEASE_VERSION" -m "Release version $RELEASE_VERSION"

# Push tag to remote repository
git push origin "v$RELEASE_VERSION"
```

### Development Version Commit

```bash
# Stage POM changes
git add pom.xml

# Commit version bump
git commit -m "chore: bump version to $NEXT_SNAPSHOT_VERSION"

# Push to main branch
git push origin $GITHUB_REF_NAME
```

## Conditional Logic

### Release Detection

```yaml
# Only run version management on main/master merges
if: github.event_name == 'push' && (github.ref == 'refs/heads/main' || github.ref == 'refs/heads/master')
```

### Version Type Identification

```bash
if [[ "$VERSION" == *"-SNAPSHOT" ]]; then
    echo "Development version detected"
    ARTIFACT_TYPE="snapshot"
else
    echo "Release version detected"
    ARTIFACT_TYPE="release"
fi
```

## Container Image Versioning

### Synchronized Versioning

The container images use the same version scheme as Maven artifacts:

```bash
# Extract version for container tagging
VERSION=$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)

# Create container tags aligned with Maven version
if [[ "$VERSION" == *"-SNAPSHOT" ]]; then
    # Development container tag
    CONTAINER_TAG="$REGISTRY/$IMAGE_NAME:$VERSION-$SHORT_SHA"
else
    # Release container tags
    RELEASE_TAG="$REGISTRY/$IMAGE_NAME:$RELEASE_VERSION"
    LATEST_TAG="$REGISTRY/$IMAGE_NAME:latest"
fi
```

### Container Release Tagging

```bash
# Create container-specific Git tag
CONTAINER_TAG="container-v$RELEASE_VERSION"
git tag -a "$CONTAINER_TAG" -m "Container release version $RELEASE_VERSION"
git push origin "$CONTAINER_TAG"
```

## Benefits

### Zero Manual Intervention
- No human involvement in version management
- Eliminates human error in version bumping
- Consistent versioning across all environments

### Clean Release Artifacts
- No SNAPSHOT suffixes in production releases
- Clear distinction between development and release artifacts
- Proper semantic versioning

### Complete Traceability
- Git tags for every release (JAR and container)
- Audit trail of all version changes
- Easy rollback to any previous version

### Development Continuity
- Automatic preparation of next development version
- No interruption to development workflow
- Ready for immediate feature development

## Versioning Strategies

### Semantic Versioning Support

**Current Implementation (Patch Increment):**
```
0.0.1 → 0.0.2 → 0.0.3
```

**Minor Version Strategy:**
```bash
# For feature releases
MINOR_VERSION=$((MINOR + 1))
RELEASE_VERSION="$MAJOR.$MINOR_VERSION.0"
```

**Major Version Strategy:**
```bash
# For breaking changes
MAJOR_VERSION=$((MAJOR + 1))
RELEASE_VERSION="$MAJOR_VERSION.0.0"
```

### Custom Version Patterns

**Date-Based Versioning:**
```bash
DATE_VERSION=$(date +%Y.%m.%d)
BUILD_NUMBER=${GITHUB_RUN_NUMBER}
RELEASE_VERSION="$DATE_VERSION.$BUILD_NUMBER"
```

**Git Hash Integration:**
```bash
SHORT_SHA=${GITHUB_SHA::8}
RELEASE_VERSION="$BASE_VERSION-$SHORT_SHA"
```

## Rollback Strategy

### Version Rollback

```bash
# Rollback to previous version
PREVIOUS_TAG=$(git describe --tags --abbrev=0 HEAD~1)
PREVIOUS_VERSION=${PREVIOUS_TAG#v}

# Update POM to previous version
./mvnw versions:set -DnewVersion=$PREVIOUS_VERSION -DgenerateBackupPoms=false

# Commit rollback
git add pom.xml
git commit -m "rollback: revert to version $PREVIOUS_VERSION"
git push origin main
```

### Emergency Hotfix Process

```bash
# Create hotfix branch from release tag
git checkout -b hotfix/$RELEASE_VERSION v$RELEASE_VERSION

# Apply fixes and increment patch
HOTFIX_VERSION="$MAJOR.$MINOR.$((PATCH + 1))"

# Follow normal release process for hotfix
```

## Monitoring & Verification

### Version Consistency Checks

```bash
# Verify Maven version matches Git tag
MAVEN_VERSION=$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)
LATEST_TAG=$(git describe --tags --abbrev=0)
LATEST_VERSION=${LATEST_TAG#v}

if [[ "$MAVEN_VERSION" != "$LATEST_VERSION-SNAPSHOT" ]]; then
    echo "Version mismatch detected!"
    exit 1
fi
```

### Artifact Verification

```bash
# Verify artifact exists in Nexus
ARTIFACT_URL="$NEXUS_URL/repository/maven-releases/ch/adeutschmanndev/helloworld/$RELEASE_VERSION/helloworld-$RELEASE_VERSION.jar"
curl -f -I "$ARTIFACT_URL" || exit 1
```

## Best Practices

### Version Management Guidelines

1. **Consistent Incrementing**: Always increment patch version for releases
2. **Clean Releases**: Never deploy SNAPSHOT versions to production
3. **Tag Everything**: Create Git tags for all releases
4. **Automate Everything**: No manual version management
5. **Verify Deployment**: Confirm artifacts are properly deployed

### Error Handling

```bash
# Rollback on deployment failure
trap 'rollback_version' ERR

rollback_version() {
    echo "Deployment failed, rolling back version changes"
    git reset --hard HEAD~1
    git push --force origin main
}
```

### Integration Testing

```yaml
- name: Verify version consistency
  run: |
    # Check that release version doesn't conflict
    if git tag | grep -q "v$RELEASE_VERSION"; then
      echo "Version $RELEASE_VERSION already exists!"
      exit 1
    fi
```

This intelligent version management system ensures reliable, automated releases while maintaining complete traceability and development workflow continuity.
