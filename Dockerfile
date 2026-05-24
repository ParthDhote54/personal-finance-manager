# Stage 1: Build Stage
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy only the pom.xml first to cache dependencies
COPY pom.xml .

# Download all required dependencies (improves caching)
RUN mvn dependency:go-offline

# Copy the source code
COPY src ./src

# Build the application, skipping tests to speed up the process
RUN mvn clean package -DskipTests

# Stage 2: Runtime Stage
FROM eclipse-temurin:21-jre-alpine

# Set the working directory
WORKDIR /app

# Copy the compiled JAR file from the builder stage
COPY --from=builder /app/target/manager-1.0.0.jar app.jar

# Expose the application port
EXPOSE 8080

# Define the entry point to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
