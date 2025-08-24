# GitHub Actions Nexus Deployment Setup Guide

This guide walks you through setting up GitHub Actions to build your Spring Boot JAR and deploy it to your Nexus repository **only when merging to main/master branch**.

## 📋 Prerequisites

- Access to your GitHub repository settings
- Nexus repository manager with user credentials
- Your Nexus instance URL

## 🔄 Workflow Behavior

### When the workflow runs:
- **Build Job**: Always runs on:
  - Push to main/master branches
  - Pull requests to main/master branches
  - Manual workflow dispatch

- **Deploy to Nexus Job**: Only runs on:
  - **Push to main/master branches** (merge from feature branch)
  - **NOT on pull requests** (ensures only reviewed code gets deployed)

### What happens:
1. **Feature branch work**: Build and test only (no Nexus deployment)
2. **Pull request**: Build and test only (validates the code)
3. **Merge to main/master**: Build, test, AND deploy to Nexus with automatic versioning

### 🚀 Automatic Version Management:
When deploying to Nexus, the workflow automatically:
1. **Extracts current version** from pom.xml (e.g., `0.0.1-SNAPSHOT`)
2. **Creates release version** by removing SNAPSHOT and incrementing patch (e.g., `0.0.2`)
3. **Deploys release version** to Nexus releases repository
4. **Creates Git tag** (e.g., `v0.0.2`) for the release
5. **Updates pom.xml** to next snapshot version (e.g., `0.0.3-SNAPSHOT`)
6. **Commits and pushes** the version bump back to main branch

**Example flow:**
- Start: `0.0.1-SNAPSHOT` → Deploy: `0.0.2` → Next: `0.0.3-SNAPSHOT`

## 🔐 Step 1: Set up GitHub Secrets

### 1.1 Navigate to Repository Settings
1. Go to your GitHub repository: `https://github.com/adeutschmann/spring-boot-helloworld`
2. Click on **Settings** tab
3. In the left sidebar, click **Secrets and variables** → **Actions**

### 1.2 Add Required Secrets
Click **New repository secret** for each of the following:

#### Secret 1: NEXUS_USERNAME
- **Name**: `NEXUS_USERNAME`
- **Secret**: `your-nexus-username`
- Click **Add secret**

#### Secret 2: NEXUS_PASSWORD
- **Name**: `NEXUS_PASSWORD`  
- **Secret**: `your-nexus-password`
- Click **Add secret**

#### Secret 3: NEXUS_URL
- **Name**: `NEXUS_URL`
- **Secret**: `https://your-nexus-instance.com` (without trailing slash)
- Click **Add secret**

### 1.3 Verify Secrets
After adding all secrets, you should see:
- ✅ NEXUS_USERNAME
- ✅ NEXUS_PASSWORD  
- ✅ NEXUS_URL

## 🏗️ Step 2: Understanding the Workflow

The GitHub Actions workflow will:

1. **Trigger on**:
   - Push to main/master/develop branches
   - Pull requests to main/master
   - Manual workflow dispatch

2. **Build Process**:
   - Checkout code
   - Set up Java 21 (Temurin)
   - Cache Maven dependencies
   - Configure Maven settings with Nexus credentials
   - Run tests
   - Build JAR file
   - Deploy to Nexus

3. **Artifacts**:
   - Uploads JAR as GitHub artifact (30-day retention)
   - Deploys to appropriate Nexus repository (releases/snapshots)

## 🚀 Step 3: Repository Configuration

### 3.1 Nexus Repository Types
Based on your version in `pom.xml`:
- **SNAPSHOT versions** (e.g., `0.0.1-SNAPSHOT`) → `maven-snapshots` repository
- **Release versions** (e.g., `1.0.0`) → `maven-releases` repository

### 3.2 Common Repository Names
If your Nexus uses different repository names, update the `pom.xml`:
```xml
<repository>
    <id>nexus-releases</id>
    <url>${nexus.url}/repository/your-release-repo-name/</url>
</repository>
<snapshotRepository>
    <id>nexus-snapshots</id>
    <url>${nexus.url}/repository/your-snapshot-repo-name/</url>
</snapshotRepository>
```

## 🔧 Step 4: Testing the Setup

### 4.1 Local Testing (Optional)
Test Maven deployment locally:
```bash
# Set environment variables
export NEXUS_USERNAME="your-username"
export NEXUS_PASSWORD="your-password"
export NEXUS_URL="https://your-nexus-instance.com"

# Test deployment
./mvnw deploy -Dnexus.url=$NEXUS_URL
```

### 4.2 GitHub Actions Testing
1. Push code to trigger the workflow
2. Go to **Actions** tab in your repository
3. Watch the workflow execution
4. Check the deployment summary

## 🛠️ Step 5: Customization Options

### 5.1 Version Management
To create releases instead of snapshots:
```bash
# Change version in pom.xml from SNAPSHOT to release
<version>1.0.0</version>
```

### 5.2 Additional Maven Goals
Add to the workflow if needed:
```yaml
- name: Generate sources JAR
  run: ./mvnw source:jar
  
- name: Generate javadoc JAR  
  run: ./mvnw javadoc:jar
```

## 🔍 Step 6: Troubleshooting

### Common Issues:
1. **401 Unauthorized**: Check NEXUS_USERNAME and NEXUS_PASSWORD
2. **404 Not Found**: Verify NEXUS_URL and repository names
3. **Build Failure**: Check Java version compatibility

### Debugging:
- Check workflow logs in GitHub Actions tab
- Verify secrets are properly set
- Test Nexus connection manually

## 📊 Expected Results

After successful execution:
- ✅ JAR built and tested
- ✅ Artifact uploaded to GitHub
- ✅ JAR deployed to Nexus repository
- ✅ Deployment summary in workflow

The artifact will be available in your Nexus repository at:
`https://your-nexus-instance.com/repository/maven-snapshots/ch/adeutschmanndev/helloworld/0.0.1-SNAPSHOT/`
