# Build stage - Use Eclipse Temurin JDK 21 Alpine for compilation
FROM eclipse-temurin:21-jdk-alpine AS builder

# Set metadata labels
LABEL maintainer="Alexander Deutschmann"
LABEL description="Spring Boot HelloWorld application with RouterFunction"
LABEL version="1.0"

# Create application directory and non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first for better layer caching
COPY --chown=appuser:appgroup mvnw pom.xml ./
COPY --chown=appuser:appgroup .mvn .mvn

# Install dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY --chown=appuser:appgroup src src

# Build the application
RUN ./mvnw clean package -DskipTests -B

# Runtime stage - Use Eclipse Temurin JRE 21 Alpine for smaller footprint
FROM eclipse-temurin:21-jre-alpine

# Install security updates and remove package manager cache
RUN apk update && \
    apk upgrade && \
    apk add --no-cache dumb-init curl && \
    rm -rf /var/cache/apk/*

# Create non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=builder --chown=appuser:appgroup /app/target/*.jar app.jar

# Switch to non-root user
USER appuser

# Expose port 8080
EXPOSE 8080

# Set JVM options for container environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/hello || exit 1

# Use dumb-init for proper signal handling
ENTRYPOINT ["dumb-init", "--"]

# Run the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
