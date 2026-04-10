# JavaProjects

### 1.3 Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Language | Java | 21 |
| Framework | Spring Boot | 3.2.4 |
| Database (Production) | PostgreSQL | 15+ |
| Database (Testing) | H2 | Latest |
| Build Tool | Gradle | 8.x |
| Testing | JUnit 5 + AssertJ | Latest |


# TestCommands

# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "SellerServiceTest"

# Run tests with coverage report
./gradlew test jacocoTestReport

# View test report
open build/reports/tests/test/index.html
