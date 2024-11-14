# Use an official OpenJDK image as a base
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the jar file to the container
COPY build/libs/quartz-management-console.jar app.jar

# Expose the port your Spring Boot app runs on
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
