# Contributing to CQRS Microservices

Thank you for your interest in contributing to our CQRS Microservices project! This document provides guidelines and
information for contributors.

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Coding Standards](#coding-standards)
- [Architecture Guidelines](#architecture-guidelines)
- [Testing Requirements](#testing-requirements)
- [Pull Request Process](#pull-request-process)
- [Issue Reporting](#issue-reporting)
- [Documentation](#documentation)
- [Performance Considerations](#performance-considerations)

## 🤝 Code of Conduct

This project adheres to a code of conduct that we expect all contributors to follow:

- Be respectful and inclusive
- Focus on constructive feedback
- Help maintain a welcoming environment
- Report any unacceptable behavior to the maintainers

## 🚀 Getting Started

### Prerequisites

Before contributing, ensure you have:

- Java 21 or higher
- Docker and Docker Compose
- Git
- IDE with Kotlin support (IntelliJ IDEA recommended)
- Basic understanding of CQRS pattern and event-driven architecture

### Environment Setup

1. **Fork and clone the repository**
   ```bash
   git clone <repository-url>
   cd cqrs-microservices
   ```

2. **Start infrastructure services**
   ```bash
   docker compose up -d
   ```

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Run tests to verify setup**
   ```bash
   ./gradlew test
   ```

## 💻 Development Workflow

### Branch Strategy

We follow a Git Flow approach:

- **main**: Production-ready code
- **develop**: Integration branch for features
- **feature/**: New features (`feature/add-order-validation`)
- **bugfix/**: Bug fixes (`bugfix/fix-event-ordering`)
- **hotfix/**: Critical production fixes (`hotfix/security-patch`)

### Feature Development Process

1. **Create a feature branch**
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/your-feature-name
   ```

2. **Make changes following coding standards**

3. **Write/update tests**
    - Unit tests for business logic
    - Integration tests for API endpoints
    - Update existing tests if needed

4. **Run the full test suite**
   ```bash
   ./gradlew test
   ```

5. **Update documentation if needed**

6. **Commit with descriptive messages**
   ```bash
   git commit -m "feat: add order validation with business rules
   
   - Add OrderValidator class with comprehensive validation
   - Include customer ID and amount validation
   - Add unit tests for all validation scenarios
   - Update API documentation
   
   Closes #123"
   ```

7. **Push and create Pull Request**

### Commit Message Convention

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

**Types:**

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

**Examples:**

```bash
feat(command): add order validation logic
fix(query): resolve customer lookup issue
docs: update API documentation
test(integration): add end-to-end order flow test
```

## 📝 Coding Standards

### Kotlin Style Guide

We follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html) with these additional
rules:

#### Code Formatting

- Use 4 spaces for indentation
- Maximum line length: 120 characters
- Use trailing commas in multi-line structures
- Prefer explicit types for public APIs

```kotlin
// Good
class OrderCommandService(
    private val repository: OrderCommandRepository,
    private val eventPublisher: EventPublisher,
    private val validator: OrderValidator,
) {
    fun createOrder(request: CreateOrderCommandRequest): OrderResponse {
        validator.validate(request)

        val command = OrderCommand(
            customerId = request.customerId,
            totalAmount = request.totalAmount,
            status = Status.PENDING,
        )

        return repository.save(command)
            .let { mapper.toResponse(it) }
            .also { eventPublisher.publish(OrderCreatedEvent(it.id, it.customerId)) }
    }
}
```

#### Naming Conventions

- **Classes**: PascalCase (`OrderCommandService`)
- **Functions**: camelCase (`createOrder`)
- **Variables**: camelCase (`totalAmount`)
- **Constants**: UPPER_SNAKE_CASE (`MAX_ORDER_AMOUNT`)
- **Packages**: lowercase (`dev.denissajnar.command`)

#### Function Design

- Keep functions small and focused (max 20 lines)
- Use meaningful parameter names
- Prefer immutable data structures
- Use nullable types judiciously

```kotlin
// Good - Single responsibility, clear parameters
fun validateOrderAmount(amount: BigDecimal): ValidationResult =
    when {
        amount <= BigDecimal.ZERO -> ValidationResult.invalid("Amount must be positive")
        amount > MAX_ORDER_AMOUNT -> ValidationResult.invalid("Amount exceeds maximum")
        else -> ValidationResult.valid()
    }

// Avoid - Too many responsibilities
fun processOrderAndSendEmailAndUpdateInventory(order: Order) { /* ... */
}
```

### Spring Boot Conventions

#### Configuration

- Use `application.yml` over `application.properties`
- Group related properties together
- Use environment-specific profiles

```yaml
# Good
spring:
  datasource:
    mongodb:
      uri: ${MONGODB_URI:mongodb://localhost:27017/orders_command}

  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER:admin}
```

#### Dependency Injection

- Use constructor injection
- Prefer `val` for injected dependencies
- Use `@Component` family annotations appropriately

```kotlin
@Service
class OrderCommandService(
    private val repository: OrderCommandRepository,
    private val eventPublisher: EventPublisher,
) {
    // Implementation
}
```

## 🏗️ Architecture Guidelines

### CQRS Pattern Adherence

#### Command Side (Write)

- Handle only write operations (Create, Update, Delete)
- Use MongoDB for event sourcing and persistence
- Publish domain events for all state changes
- Validate business rules before persistence
- No query operations allowed

#### Query Side (Read)

- Handle only read operations
- Use PostgreSQL for optimized queries
- Listen to domain events for data synchronization
- No direct database modifications
- Optimize for read performance

#### Shared Module

- Only common models, events, and DTOs
- No business logic
- Maintain backward compatibility
- Keep dependencies minimal

### Event-Driven Architecture

#### Domain Events

```kotlin
// Follow this pattern for all domain events
data class OrderCreatedEvent(
    override val eventId: Long = Random().nextLong(),
    val historyId: String,
    val customerId: Long,
    val totalAmount: BigDecimal,
    override val timestamp: Instant = Instant.now(),
) : DomainEvent
```

#### Event Publishing

- Publish events after successful persistence
- Use transactional outbox pattern for reliability
- Include all necessary data in events
- Make events immutable

#### Event Handling

- Handle events idempotently
- Implement proper error handling and retry logic
- Log event processing for debugging
- Maintain event ordering when necessary

### API Design

#### RESTful Conventions

- Use HTTP verbs correctly (GET, POST, PUT, DELETE)
- Return appropriate HTTP status codes
- Use consistent URL patterns
- Version APIs (`/api/v1/`)

#### Request/Response

```kotlin
// Request DTOs - validation annotations
data class CreateOrderCommandRequest(
    @field:NotNull
    @field:Min(value = 1, message = "Customer ID must be positive")
    val customerId: Long,

    @field:NotNull
    @field:DecimalMin(value = "0.01", message = "Total amount must be positive")
    val totalAmount: BigDecimal,
)

// Response DTOs - complete data
data class OrderResponse(
    val id: String,
    val customerId: Long,
    val totalAmount: BigDecimal,
    val status: Status,
    val createdAt: Instant,
)
```

#### Error Handling

- Use `@ControllerAdvice` for global exception handling
- Return consistent error response format
- Include appropriate error codes and messages
- Log errors with context

## 🧪 Testing Requirements

### Test Coverage

Maintain minimum 80% code coverage for:

- Service classes (business logic)
- Controllers (API endpoints)
- Event handlers
- Validators

### Testing Strategy

#### Unit Tests

- Test individual components in isolation
- Mock external dependencies
- Focus on business logic validation
- Use descriptive test names

```kotlin
@ExtendWith(MockitoExtension::class)
class OrderCommandServiceTest {

    @Test
    fun `should create order with valid data`() {
        // Given
        val request = CreateOrderCommandRequest(
            customerId = 1L,
            totalAmount = BigDecimal("99.99")
        )

        // When
        val result = service.createOrder(request)

        // Then
        assertThat(result.customerId).isEqualTo(1L)
        assertThat(result.status).isEqualTo(Status.PENDING)
        verify(eventPublisher).publish(any<OrderCreatedEvent>())
    }

    @Test
    fun `should throw exception for invalid customer ID`() {
        // Given
        val request = CreateOrderCommandRequest(
            customerId = -1L,
            totalAmount = BigDecimal("99.99")
        )

        // When & Then
        assertThrows<BusinessValidationException> {
            service.createOrder(request)
        }
    }
}
```

#### Integration Tests

- Test complete API workflows
- Use Testcontainers for database testing
- Test event publishing and handling
- Verify data consistency

```kotlin
@SpringBootTest
@Testcontainers
class OrderCommandControllerIntegrationTest : SpringBootTestParent() {

    @Test
    fun `should create order and publish event`() {
        // Given
        val request = CreateOrderCommandRequest(
            customerId = 1L,
            totalAmount = BigDecimal("99.99")
        )

        // When
        val response = restTemplate.postForEntity(
            "/api/v1/orders",
            request,
            OrderResponse::class.java
        )

        // Then
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.customerId).isEqualTo(1L)

        // Verify event was published
        await().untilAsserted {
            assertThat(rabbitTemplate.receiveAndConvert("order.events"))
                .isNotNull()
        }
    }
}
```

#### End-to-End Tests

- Test complete CQRS flows
- Verify command → event → query synchronization
- Test error scenarios and recovery
- Use realistic data volumes

### Test Data Management

- Use test data builders for complex objects
- Clean up data after tests
- Use meaningful test data
- Avoid hardcoded values

```kotlin
object OrderTestDataBuilder {
    fun createOrderCommand(
        customerId: Long = 1L,
        totalAmount: BigDecimal = BigDecimal("99.99")
    ) = CreateOrderCommandRequest(
        customerId = customerId,
        totalAmount = totalAmount
    )
}
```

## 🔄 Pull Request Process

### Before Submitting

1. **Ensure all tests pass**
   ```bash
   ./gradlew test
   ```

2. **Verify code builds successfully**
   ```bash
   ./gradlew build
   ```

3. **Update documentation** if needed

4. **Verify no breaking changes** unless intentional

### Pull Request Template

Use this template for all PRs:

```markdown
## Description

Brief description of changes and motivation.

## Type of Change

- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing

- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing performed

## Checklist

- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] Tests pass locally
```

### Review Process

1. **Automated checks** must pass
2. **At least one review** from maintainers
3. **All conversations resolved**
4. **Up-to-date with target branch**

### Review Criteria

Reviewers will check for:

- CQRS pattern adherence
- Code quality and maintainability
- Test coverage and quality
- Documentation completeness
- Performance implications
- Security considerations

## 🐛 Issue Reporting

### Bug Reports

Include the following information:

- **Environment**: OS, Java version, service version
- **Steps to reproduce**: Detailed step-by-step instructions
- **Expected behavior**: What should happen
- **Actual behavior**: What actually happens
- **Logs**: Relevant error messages and stack traces
- **Additional context**: Screenshots, configuration, etc.

### Feature Requests

Include:

- **Problem statement**: What problem does this solve?
- **Proposed solution**: How should it work?
- **Alternatives considered**: Other approaches evaluated
- **Implementation notes**: Technical considerations

## 📖 Documentation

### Code Documentation

- Document public APIs with KDoc
- Include examples for complex functions
- Explain business logic and domain concepts
- Keep documentation up-to-date with code changes

```kotlin
/**
 * Creates a new order in the command side of the CQRS system.
 *
 * This function validates the order data, persists it to MongoDB,
 * and publishes an OrderCreatedEvent for query side synchronization.
 *
 * @param dto The order creation request containing customer ID and amount
 * @return OrderResponseDTO with the created order details
 * @throws BusinessValidationException if the order data is invalid
 *
 * @sample
 * ```kotlin
 * val dto = CreateOrderCommandDTO(customerId = 1L, totalAmount = BigDecimal("99.99"))
 * val order = service.createOrder(dto)
 * ```

*/
fun createOrder(dto: CreateOrderCommandDTO): OrderResponseDTO

```

### Architecture Documentation

- Update architecture diagrams when adding new services
- Document event schemas and contracts
- Maintain API documentation in OpenAPI format
- Document deployment and operational procedures

## ⚡ Performance Considerations

### Database Optimization

#### Command Side (MongoDB)
- Use appropriate indexes for queries
- Consider document structure for write performance
- Monitor collection sizes and implement archiving

#### Query Side (PostgreSQL)
- Optimize indexes for read patterns
- Use database-specific features (views, materialized views)
- Monitor query performance

### Event Processing

- Design events for efficient serialization
- Consider event size and frequency
- Implement proper error handling and dead letter queues
- Monitor message queue performance

### API Performance

- Implement pagination for large result sets
- Use appropriate HTTP caching headers
- Consider async processing for long operations
- Monitor response times and implement timeouts

## 🚨 Security Guidelines

### Data Validation

- Validate all input data
- Use parameterized queries
- Implement rate limiting
- Sanitize output data

### Authentication & Authorization

- Never commit credentials to code
- Use environment variables for secrets
- Implement proper RBAC when adding auth
- Validate JWT tokens properly

### Database Security

- Use least privilege database accounts
- Enable database audit logging
- Encrypt sensitive data
- Regular security updates

## 🎯 Getting Help

### Communication Channels

- **GitHub Issues**: Bug reports and feature requests
- **Pull Requests**: Code discussions
- **Email**: Direct contact with maintainers

### Learning Resources

- [CQRS Pattern](https://martinfowler.com/bliki/CQRS.html)
- [Event Sourcing](https://martinfowler.com/eaaDev/EventSourcing.html)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Kotlin Documentation](https://kotlinlang.org/docs/)

### Common Issues

1. **Tests failing locally**
   - Ensure Docker services are running
   - Check database connections
   - Verify test data cleanup

2. **Build failures**
   - Clean build directory: `./gradlew clean`
   - Check Java version compatibility
   - Update dependencies if needed

3. **IDE setup issues**
   - Import as Gradle project
   - Set Project SDK to Java 21
   - Enable Kotlin plugin

Thank you for contributing to our CQRS Microservices project! Your contributions help make this project better for everyone.
