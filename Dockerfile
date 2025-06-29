# Use OpenJDK 8 as base image
FROM openjdk:8-jdk-slim

# Set working directory
WORKDIR /app

# Install curl
RUN apt-get update && apt-get install -y curl

# Copy the project files
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src ./src

# Make mvnw executable and build the application
RUN chmod +x mvnw && ./mvnw package

# Expose port 8080
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/ecommerce-app-1.0-SNAPSHOT.jar"]
