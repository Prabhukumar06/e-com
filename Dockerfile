# Use OpenJDK 8 as base image
FROM openjdk:8-jdk-slim

# Set working directory
WORKDIR /app

# Install Maven and curl
RUN apt-get update && apt-get install -y curl maven

# Copy the project files
COPY pom.xml ./
COPY src ./src

# Build the application
RUN mvn package

# Expose port 8080
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/ecommerce-app-1.0-SNAPSHOT.jar"]
