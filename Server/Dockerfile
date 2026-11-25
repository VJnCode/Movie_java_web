# syntax=docker/dockerfile:1
# Build context: repository root (docker build -f Server/Dockerfile .)

# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app/Server
COPY Server/pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY Server/src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Runtime stage: JRE only, non-root, read-only friendly ----
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S -G app -H -s /sbin/nologin app
WORKDIR /app
COPY --from=build --chown=app:app /app/Server/target/movie-web.jar app.jar
USER app
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"
EXPOSE 8000
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget -qO- http://127.0.0.1:${PORT:-8000}/actuator/health/liveness || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
