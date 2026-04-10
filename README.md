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









# Clean build
./gradlew clean

# Compile and build JAR
./gradlew build

# Build without running tests
./gradlew build -x test

# Create executable JAR only
./gradlew bootJar




#Api usage examples

1.Create Seller
curl -X POST http://localhost:8080/sellers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ivan Petrov",
    "contactInfo": "ivan@mail.ru"
  }'


Get All sellers

curl http://localhost:8080/sellers

{
    "id": 1,
    "name": "Ivan Petrov",
    "contactInfo": "ivan@mail.ru",
    "registrationDate": "2024-01-15T10:30:00",
    "current": true,
    "version": 1
}


UpdateSeller (Creates New Version)

{
    "id": 3,
    "name": "Ivan Petrovich",
    "version": 2,
    "current": true,
    "originalId": 1
}


Get Best Seller

{
    "id": 2,
    "name": "Maria Sidorova",
    "contactInfo": "maria@mail.ru"
}

