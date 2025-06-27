# Dockerfile

# Use a Java 21 base image
FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy the JAR from the Server/target directory into the container
COPY Server/target/movie-web-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your Spring Boot app uses
EXPOSE 8000

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
