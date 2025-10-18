# Multi-stage build for Spring Boot
FROM maven:3.9.4-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml first for better caching
COPY pom.xml .

# Download dependencies with offline mode disabled to avoid blocked mirrors
RUN mvn dependency:resolve -B

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests

# Production stage
FROM eclipse-temurin:17-jre-alpine

# Install curl for health checks
RUN apk add --no-cache curl

# Create app user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -S appuser -u 1001 -G appgroup

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/restaurant-booking-0.0.1-SNAPSHOT.jar app.jar

# Create uploads directory
RUN mkdir -p /tmp/uploads && chown -R appuser:appgroup /tmp/uploads

# Change to app user
USER appuser

# Expose port (Render sẽ tự động map)
EXPOSE $PORT

# Health check sử dụng endpoint đơn giản
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:$PORT/api/health || exit 1

# Run application với port động từ Render
CMD ["sh", "-c", "java -Dspring.profiles.active=prod -Dserver.port=$PORT -jar app.jar"] 