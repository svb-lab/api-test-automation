# Multi-stage Dockerfile for API Test Automation with Allure Reports

# ========== BUILD STAGE ==========
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean compile -DskipTests

# ========== TEST RUNTIME STAGE ==========
FROM maven:3.9-eclipse-temurin-21-alpine

WORKDIR /app
COPY --from=build /app /app

ENV API_BASE_URL=https://fakerestapi.azurewebsites.net \
    PARALLEL_ENABLED=false

# Create simple entrypoint script
RUN echo '#!/bin/sh' > /app/run-tests.sh && \
    echo 'cd /app' >> /app/run-tests.sh && \
    echo 'echo "Tests started..."' >> /app/run-tests.sh && \
    echo 'mvn clean test -Dapi.base.url=$API_BASE_URL -Djunit.jupiter.execution.parallel.enabled=$PARALLEL_ENABLED' >> /app/run-tests.sh && \
    echo 'TEST_EXIT_CODE=$?' >> /app/run-tests.sh && \
    echo 'echo "Generating report..."' >> /app/run-tests.sh && \
    echo 'mvn allure:report' >> /app/run-tests.sh && \
    echo 'echo "Copying to /output..."' >> /app/run-tests.sh && \
    echo 'mkdir -p /output/allure-results /output/allure-report' >> /app/run-tests.sh && \
    echo 'cp -r /app/target/allure-results/* /output/allure-results/' >> /app/run-tests.sh && \
    echo 'cp -r /app/target/site/allure-maven-plugin/* /output/allure-report/' >> /app/run-tests.sh && \
    echo 'echo "Done."' >> /app/run-tests.sh && \
    echo 'exit $TEST_EXIT_CODE' >> /app/run-tests.sh && \
    chmod +x /app/run-tests.sh

ENTRYPOINT ["/app/run-tests.sh"]