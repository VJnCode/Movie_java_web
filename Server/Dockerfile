# Stage 1: Build the app
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY Server/pom.xml Server/
COPY Server/src Server/src
WORKDIR /app/Server
RUN mvn clean package -DskipTests

# Stage 2: Run the app
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /app/Server/target/movie-web-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8000
ENTRYPOINT ["java", "-jar", "app.jar"]
