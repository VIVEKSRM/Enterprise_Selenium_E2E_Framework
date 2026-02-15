FROM maven:3.9.6-eclipse-temurin-17

WORKDIR /app

# Copy project files
COPY pom.xml .
COPY src ./src
COPY testng-docker-parallel.xml .
COPY testng-docker-all-parallel.xml .

# Install dependencies
RUN mvn dependency:resolve

# Set environment for Docker execution
ENV execution.mode=DOCKER
ENV docker.hub.url=http://selenium-hub:4444
ENV browser=CHROME

# Run tests with parallel execution
CMD ["mvn", "clean", "test", "-Dsuites=testng-docker-parallel.xml", "-Dheadless=true"]

