# Use OpenJDK 8 as base image
FROM openjdk:8-jdk-slim

# Set working directory
WORKDIR /app

# Copy the project files
COPY . .

# Build the application
RUN ./mvnw package

# Expose port 8080
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/ecommerce-app-1.0-SNAPSHOT.jar"]
