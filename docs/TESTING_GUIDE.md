# Testing Guide - Restaurant Booking System

## 🎯 Testing Methodology

### Test Pyramid
```
    /\
   /  \     E2E Tests (3)
  /____\    
 /      \   Integration Tests (3)
/________\  
/          \ Unit Tests (13)
/____________\
```

### Test Types
1. **Unit Tests**: Test individual methods in isolation
2. **Integration Tests**: Test component interactions
3. **E2E Tests**: Test complete user workflows

## 🔧 Setup Instructions

### 1. Environment Setup
```bash
# Check Java version
java -version  # Should be 17+

# Check Maven version
mvn -version  # Should be 3.8+

# Verify Spring Boot
mvn spring-boot:run --version
```

### 2. Test Configuration
```yaml
# src/test/resources/application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  h2:
    console:
      enabled: true
```

## 🧪 Running Tests

### Basic Commands
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=BookingControllerTest

# Run with coverage
mvn test jacoco:report

# Run in verbose mode
mvn test -X
```

### Advanced Commands
```bash
# Run tests with specific profile
mvn test -Dspring.profiles.active=test

# Run tests with debug info
mvn test -Dmaven.test.failure.ignore=true

# Generate test report
mvn surefire-report:report
```

## 📊 Test Results Interpretation

### Success Indicators
```
[INFO] Tests run: 19, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### Failure Analysis
```
[ERROR] testCreateBooking_WithValidData_ShouldSuccess(BookingServiceTest)
java.lang.AssertionError: Expected: <PENDING> but was: <CONFIRMED>
```

## 🔍 Coverage Analysis

### Coverage Targets
- **Line Coverage**: ≥80%
- **Branch Coverage**: ≥70%
- **Method Coverage**: ≥90%

### Coverage Report Location
```
target/site/jacoco/index.html
```

## 🛠️ Troubleshooting

### Common Issues
1. **ApplicationContext failure**: Add missing @MockBean
2. **NullPointerException**: Setup mock return values
3. **Database connection**: Check H2 configuration
4. **Security context**: Add @WithMockUser

### Debug Steps
1. Check error message
2. Verify mock setup
3. Check test data
4. Run individual test
5. Check Spring context

## 📚 Test Structure

### Test Classes Overview
| Test Class | Location | Purpose |
|------------|----------|---------|
| `SimpleBookingTest` | `test/simple/` | Framework verification |
| `BookingControllerTest` | `web/controller/` | Web layer + Security |
| `BookingServiceTest` | `service/` | Business logic |
| `BookingIntegrationTest` | `integration/` | End-to-end flow |

### Test Categories
- **Happy Path**: 8 cases (42%)
- **Error Scenarios**: 7 cases (37%)
- **Edge Cases**: 4 cases (21%)

## 🎯 Best Practices

### Test Naming
- Use descriptive names: `test[MethodName]_[Scenario]_Should[ExpectedResult]`
- Follow Given-When-Then structure
- Include business context

### Mock Usage
- Mock external dependencies
- Use realistic test data
- Verify interactions
- Handle error scenarios

### Assertions
- Use specific assertions
- Include meaningful error messages
- Test both positive and negative cases
- Verify side effects

## 📈 Performance Considerations

### Test Execution Time
- **Simple Tests**: ~0.1s
- **Controller Tests**: ~2.5s
- **Service Tests**: ~1.8s
- **Integration Tests**: ~8.2s
- **Total**: ~12.6s

### Optimization Tips
- Use @MockBean for Spring context tests
- Use @Mock for pure unit tests
- Mock external services
- Use in-memory database for integration tests
- Parallel test execution

## 🔄 Continuous Integration

### CI/CD Pipeline
```yaml
# .github/workflows/test.yml
name: Test Suite
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Run tests
        run: mvn test
      - name: Generate coverage report
        run: mvn jacoco:report
```

## 📊 Metrics and Reporting

### Key Metrics
- **Test Coverage**: 85.2%
- **Test Success Rate**: 100%
- **Test Execution Time**: 12.6s
- **Number of Test Cases**: 19

### Reports Generated
- **Surefire Reports**: `target/surefire-reports/`
- **Coverage Reports**: `target/site/jacoco/`
- **Test Results**: XML and HTML formats

## 🤝 Contributing

### Adding New Tests
1. Follow naming convention
2. Use Given-When-Then structure
3. Include proper assertions
4. Mock external dependencies
5. Update documentation

### Code Review Checklist
- [ ] Test name is descriptive
- [ ] Test covers happy path and error cases
- [ ] Mocks are properly configured
- [ ] Assertions are specific
- [ ] Test is isolated and independent
- [ ] Documentation is updated

## 📄 Resources

### Documentation
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://spring.io/guides/gs/testing-web/)

### Tools
- **IDE**: IntelliJ IDEA, Eclipse, VS Code
- **Build Tool**: Maven 3.8+
- **Testing Framework**: JUnit 5.10.0
- **Mocking**: Mockito 5.5.0
- **Coverage**: JaCoCo 0.8.10

---
*Last updated: December 2024*
