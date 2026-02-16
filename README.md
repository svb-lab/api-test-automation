# API Test Automation

Automated REST API tests for [FakeRestAPI](https://fakerestapi.azurewebsites.net) covering Books and Authors endpoints using Java 21, REST Assured, JUnit 5, and Allure reporting.

## Prerequisites

- Java 21
- Maven 3.9+
- Docker (optional)

## Project Structure

```
src/main/java/com/apitest/
├── config/       # REST Assured and API configuration
├── models/       # POJOs (Book, Author, ErrorResponse)
└── services/     # API service layer (BookService, AuthorService)

src/test/java/com/apitest/
├── base/         # BaseTest with shared setup
├── helpers/      # Test data builders and assertion helpers
└── tests/        # BookApiTest, AuthorApiTest
```

## Run Tests

```bash
# all tests
mvn clean test

# specific test class
mvn test -Dtest=BookApiTest

# by tag
mvn test -Dgroups=smoke
mvn test -Dgroups=regression

# with parallel execution
mvn test -Djunit.jupiter.execution.parallel.enabled=true

# generate and open Allure report
mvn allure:serve
```

## Run with Docker

```bash
# using docker compose
docker-compose up

# using plain docker
docker build -t api-tests .
docker run --rm -v $(pwd)/test-results:/output api-tests
```

Test results and Allure reports are saved to `./test-results/`.

## CI/CD

### GitHub Actions

The workflow (`.github/workflows/api-tests.yml`) builds a Docker image and runs tests inside a container. Supports enabling parallel execution. Allure reports are uploaded as artifacts and published to GitHub Pages.

Trigger manually from the **Actions** tab.

## Notes

- The test suite includes intentionally failing and skipped tests to demonstrate Allure's reporting capabilities for different test outcomes (pass, fail, skip).
- Some edge cases (e.g., verifying data persistence after create/update/delete) could not be tested because the demo API does not persist data.
- The demo API accepts negative IDs for books and authors, so the test data builders are configured to generate IDs across the full integer range to reflect this behavior.
