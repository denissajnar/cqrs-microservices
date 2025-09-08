# CQRS Microservices - Order Management System

[![Kotlin](https://img.shields.io/badge/kotlin-2.2.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Spring Boot](https://img.shields.io/badge/spring--boot-4.0.0--SNAPSHOT-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/java-21-blue.svg?logo=openjdk)](https://openjdk.org/)
[![Gradle](https://img.shields.io/badge/gradle-9.0.0-blue.svg?logo=gradle)](https://gradle.org/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

A modern microservices implementation demonstrating the CQRS (Command Query Responsibility Segregation) pattern using
Spring Boot, Kotlin, and event-driven architecture.

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Technologies](#technologies)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [Development](#development)
- [Testing](#testing)
- [Contributing](#contributing)
- [License](#license)

## 🔍 Overview

This project demonstrates a CQRS architecture pattern implementation with separate microservices for command (write) and
query (read) operations. The system manages order operations with complete separation of concerns, event-driven
communication, and different data storage optimized for each operation type.

### Key Features

- **CQRS Pattern**: Separate command and query responsibilities
- **Event-Driven Architecture**: Asynchronous communication between services
- **Event Sourcing**: Complete event history tracking with aggregate reconstruction
- **Transactional Outbox Pattern**: Reliable event publishing with transactional guarantees
- **Transactional Inbox Pattern**: Idempotent event processing with deduplication
- **Microservices Architecture**: Independent, scalable services
- **Multi-Database Setup**: MongoDB for writes, PostgreSQL for reads
- **Message Queue**: RabbitMQ for reliable event processing
- **API Documentation**: OpenAPI/Swagger integration
- **Comprehensive Testing**: Unit and integration tests with Testcontainers
- **Modern Tech Stack**: Spring Boot 4.0, Kotlin 2.2, Java 21

## 🏗️ Architecture

```
┌─────────────────┐    Events     ┌─────────────────┐
│  Command Side   │──────────────▶│   Query Side    │
│                 │               │                 │
│ ┌─────────────┐ │               │ ┌─────────────┐ │
│ │   MongoDB   │ │               │ │ PostgreSQL  │ │
│ │  (Write DB) │ │               │ │  (Read DB)  │ │
│ └─────────────┘ │               │ └─────────────┘ │
│                 │               │                 │
│ Order Commands  │               │ Order Queries   │
│ - Create        │               │ - Get by ID     │
│ - Update        │               │ - Get by Customer│
│ - Delete        │               │ - Get by Status │
└─────────────────┘               └─────────────────┘
         │                                 ▲
         │                                 │
         └──────────────┐ ┌────────────────┘
                        │ │
                 ┌─────────────┐
                 │  RabbitMQ   │
                 │ (Message    │
                 │  Broker)    │
                 └─────────────┘
```

### Components

1. **Order Command Service** (`order-command-service`)
    - Handles write operations (Create, Update, Delete)
    - Uses MongoDB for data persistence
    - Publishes domain events via RabbitMQ
    - REST API at `/api/v1/orders`

2. **Order Query Service** (`order-query-service`)
    - Handles read operations (Query by ID, Customer, Status)
    - Uses PostgreSQL for optimized read performance
    - Listens to domain events for data synchronization
    - REST API at `/api/v1/orders`

3. **Shared Module** (`shared`)
    - Common domain events
    - Shared data models
    - Error response DTOs

## 🔒 Transactional Messaging Patterns

This system implements both **Transactional Outbox** and **Transactional Inbox** patterns to ensure reliable message
processing and maintain data consistency across services.

### Transactional Outbox Pattern (Command Service)

The outbox pattern ensures that database changes and message publishing happen atomically, preventing data
inconsistency.

**Implementation:**

- Events are stored in MongoDB `outbox_events` collection within the same transaction as business data
- A scheduled processor (`OutboxEventProcessor`) publishes unprocessed events to RabbitMQ
- Failed events are marked with error details for monitoring and debugging
- Automatic cleanup of old processed events

**Benefits:**

- **Atomic Operations**: Database changes and event publishing are transactional
- **Reliability**: Events are never lost due to messaging failures
- **Eventual Consistency**: Messages are eventually delivered even if RabbitMQ is temporarily unavailable
- **Monitoring**: Failed events are tracked and retried automatically

**Database Schema:**

```json
{
  "collection": "outbox_events",
  "fields": {
    "id": "ObjectId",
    "eventId": "Long",
    "eventType": "String",
    "eventPayload": "String (JSON serialized event)",
    "routingKey": "String",
    "exchange": "String",
    "processed": "Boolean",
    "createdAt": "Instant",
    "processedAt": "Instant (nullable)",
    "errorMessage": "String (nullable)"
  }
}
```

### Transactional Inbox Pattern (Query Service)

The inbox pattern ensures idempotent message processing, preventing duplicate event handling.

**Implementation:**

- Each processed event is recorded in PostgreSQL `inbox_events` table
- Event handlers check for existing entries before processing
- Payload hash verification for additional integrity checks
- Automatic cleanup of old processed events

**Benefits:**

- **Idempotency**: Duplicate messages are automatically ignored
- **Data Integrity**: Prevents inconsistent state from duplicate processing
- **Audit Trail**: Complete history of processed events
- **Monitoring**: Event processing metrics and debugging capabilities

**Database Schema:**

```sql
CREATE TABLE IF NOT EXISTS inbox_events
(
    id           BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    event_id     BIGINT       NOT NULL UNIQUE,
    message_id   VARCHAR(255),
    event_type   VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payload_hash VARCHAR(64)
);
```

### Reliability Guarantees

1. **At-Least-Once Delivery**: Events are guaranteed to be delivered at least once
2. **Exactly-Once Processing**: Duplicate events are automatically deduplicated
3. **Transactional Consistency**: Database changes and messaging are atomic
4. **Failure Tracking**: Failed events are logged and marked for manual intervention
5. **Monitoring**: Complete audit trail of all message processing

### Processing Flow

```
Command Service (Outbox Pattern):
1. Business logic executes in transaction
2. Event stored in outbox_events collection
3. Transaction commits (both business data and event)
4. Scheduler picks up unprocessed events
5. Event published to RabbitMQ
6. Event marked as processed

Query Service (Inbox Pattern):
1. Event received from RabbitMQ
2. Check if event_id exists in inbox_events
3. If exists, skip processing (idempotency)
4. If new, execute business logic
5. Store event in inbox_events table
6. Transaction commits (both business data and inbox record)
```

## 🛠️ Technologies

- **Language**: Kotlin 2.2.0
- **Framework**: Spring Boot 4.0.0-SNAPSHOT
- **JVM**: Java 21
- **Build Tool**: Gradle
- **Databases**: MongoDB (Command), PostgreSQL (Query)
- **Message Broker**: RabbitMQ
- **Documentation**: OpenAPI/Swagger
- **Testing**: JUnit 5, Testcontainers
- **Containerization**: Docker, Docker Compose

## 📋 Prerequisites

- Java 21 or higher
- Docker and Docker Compose
- Gradle 9.0+ (or use included wrapper)
- Git

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd cqrs-microservices
```

### 2. Start Infrastructure Services

```bash
docker compose up -d
```

This will start:

- MongoDB (port 27017)
- PostgreSQL (port 5432)
- RabbitMQ (ports 5672, 15672)

### 3. Build the Project

```bash
./gradlew build
```

### 4. Run the Services

#### Option A: Using Gradle (Development)

```bash
# Terminal 1 - Query Service
./gradlew :order-query-service:bootRun

# Terminal 2 - Command Service
./gradlew :order-command-service:bootRun

```

#### Option B: Using JAR files

```bash
# Build JARs
./gradlew bootJar

# Run Command Service
java -jar order-command-service/build/libs/order-command-service-0.0.1-SNAPSHOT.jar

# Run Query Service
java -jar order-query-service/build/libs/order-query-service-0.0.1-SNAPSHOT.jar
```

### 5. Verify the Setup

- Command Service: http://localhost:8080/swagger-ui.html
- Query Service: http://localhost:8081/swagger-ui.html
- RabbitMQ Management: http://localhost:15672 (admin/password)

## 📚 API Documentation

### Command Service (Port 8080)

#### Create Order

```http
POST /api/v1/orders
Content-Type: application/json

{
  "customerId": 1,
  "totalAmount": 99.99
}
```

**Response:**

```json
{
  "id": "507f1f77bcf86cd799439011",
  "customerId": 1,
  "totalAmount": 99.99,
  "status": "PENDING",
  "createdAt": "2025-09-08T14:18:00Z"
}
```

#### Update Order

```http
PUT /api/v1/orders/{id}
Content-Type: application/json

{
  "customerId": 1,
  "totalAmount": 149.99,
  "status": "COMPLETED"
}
```

**Response:**

```json
{
  "id": "507f1f77bcf86cd799439011",
  "customerId": 1,
  "totalAmount": 149.99,
  "status": "COMPLETED",
  "createdAt": "2025-09-08T14:18:00Z"
}
```

#### Delete Order

```http
DELETE /api/v1/orders/{id}
```

#### Event Sourcing Operations

```http
# Reconstruct aggregate from events
GET /api/v1/event-sourcing/aggregates/{id}/reconstruct

# Get event history for aggregate
GET /api/v1/event-sourcing/aggregates/{id}/history

# Get aggregate statistics
GET /api/v1/event-sourcing/aggregates/{id}/stats

# Replay events for aggregate
POST /api/v1/event-sourcing/aggregates/{id}/replay

# Check if aggregate exists
GET /api/v1/event-sourcing/aggregates/{id}/exists

# Get current aggregate state
GET /api/v1/event-sourcing/aggregates/{id}/state

# Get latest version
GET /api/v1/event-sourcing/aggregates/{id}/version
```

### Query Service (Port 8081)

#### Get Order by ID

```http
GET /api/v1/orders/{id}
```

#### Get Orders by Customer

```http
GET /api/v1/orders?customerId=1
```

#### Get Orders by Status

```http
GET /api/v1/orders?status=PENDING
```

#### Get Orders by Customer and Status

```http
GET /api/v1/orders?customerId=1&status=PENDING
```

#### Get Order by Command-Side Order ID

```http
GET /api/v1/orders/order/{orderId}
```

**Example:**

```http
GET /api/v1/orders/order/550e8400-e29b-41d4-a716-446655440000
```

**Response:**

```json
{
  "id": 1,
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "customerId": 1,
  "totalAmount": 99.99,
  "status": "PENDING",
  "createdAt": "2025-09-08T14:18:00Z"
}
```

### Status Values

- `PENDING`
- `CONFIRMED`
- `SHIPPED`
- `DELIVERED`
- `CANCELLED`

### Monitoring and Debugging Endpoints

Both services provide additional endpoints for monitoring and debugging the transactional messaging patterns:

#### Command Service Monitoring (Port 8080)

**Outbox Events Monitoring:**

```http
# Get all unprocessed events
GET /api/v1/outbox/unprocessed

# Get outbox statistics
GET /api/v1/outbox/stats

# Get all outbox events (for debugging)
GET /api/v1/outbox
```

#### Query Service Monitoring (Port 8081)

**Inbox Events Monitoring:**

```http
# Get inbox statistics
GET /api/v1/inbox/stats

# Get all processed inbox events (for debugging)
GET /api/v1/inbox
```

These endpoints are useful for:

- Monitoring event processing health
- Debugging message flow issues
- Understanding system performance metrics
- Troubleshooting failed event deliveries

## 📁 Project Structure

```
cqrs-microservices/
├── order-command-service/          # Write-side microservice
│   ├── src/main/kotlin/
│   │   └── dev/denissajnar/command/
│   │       ├── controller/         # REST controllers
│   │       ├── service/            # Business logic
│   │       ├── repository/         # Data access
│   │       ├── domain/             # Domain models
│   │       ├── dto/                # Data transfer objects
│   │       ├── messaging/          # Event publishing
│   │       └── config/             # Configuration
│   └── src/test/                   # Tests
├── order-query-service/            # Read-side microservice
│   ├── src/main/kotlin/
│   │   └── dev/denissajnar/query/
│   │       ├── controller/         # REST controllers
│   │       ├── service/            # Business logic
│   │       ├── repository/         # Data access
│   │       ├── entity/             # JPA entities
│   │       ├── dto/                # Data transfer objects
│   │       ├── messaging/          # Event handling
│   │       └── config/             # Configuration
│   └── src/test/                   # Tests
├── shared/                         # Shared components
│   └── src/main/kotlin/
│       └── dev/denissajnar/shared/
│           ├── events/             # Domain events
│           ├── model/              # Shared models
│           └── dto/                # Common DTOs
├── compose.yml                     # Docker services
├── build.gradle.kts                # Root build configuration
└── settings.gradle.kts             # Project settings
```

## 💻 Development

### Environment Setup

1. **Database Credentials**:
    - MongoDB: `mongodb://admin:password@localhost:27017/orders_command`
    - PostgreSQL: `postgresql://user:password@localhost:5432/orders_query`
    - RabbitMQ: `amqp://admin:password@localhost:5672`

2. **Default Ports**:
    - Command Service: 8080
    - Query Service: 8081
    - MongoDB: 27017
    - PostgreSQL: 5432
    - RabbitMQ: 5672, 15672 (management)

### Configuration

Services can be configured through `application.yml` files in each service's `src/main/resources` directory.

### Database Migrations

The query service uses Flyway for database migrations. Migration files are located in:

```
order-query-service/src/main/resources/db/migration/
```

## 🧪 Testing

### Run All Tests

```bash
./gradlew test
```

### Run Tests for Specific Service

```bash
./gradlew :order-command-service:test
./gradlew :order-query-service:test
```

### Integration Tests

The project includes comprehensive integration tests using Testcontainers:

```bash
./gradlew integrationTest
```

## 🔧 Configuration

### Environment Variables

| Variable                 | Description                  | Default                                         |
|--------------------------|------------------------------|-------------------------------------------------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile        | `local`                                         |
| `MONGODB_URI`            | MongoDB connection string    | `mongodb://localhost:27017/orders_command`      |
| `POSTGRESQL_URL`         | PostgreSQL connection string | `jdbc:postgresql://localhost:5432/orders_query` |
| `RABBITMQ_HOST`          | RabbitMQ host                | `localhost`                                     |
| `RABBITMQ_PORT`          | RabbitMQ port                | `5672`                                          |

### Profiles

- `local`: Local development (default)
- `test`: Test environment
- `prod`: Production environment

## 📊 Monitoring

### Health Checks

Both services expose actuator endpoints:

- Command Service: http://localhost:8080/actuator/health
- Query Service: http://localhost:8081/actuator/health

### Metrics

Metrics are available at:

- Command Service: http://localhost:8080/actuator/metrics
- Query Service: http://localhost:8081/actuator/metrics

## 🤝 Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull
requests.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

If you encounter any issues or have questions:

1. Check the [Issues](../../issues) page
2. Review the API documentation at `/swagger-ui.html`
3. Check RabbitMQ management console for message queue status
4. Verify database connections and data consistency

## 🚀 What's Next?

- [ ] Introduce gradle composite builds
- [ ] Implement message retry with exponential backoff
- [ ] Implement DLQ (Dead Letter Queue)
- [ ] Add authentication and authorization
- [ ] Implement distributed tracing
- [ ] Add comprehensive monitoring and alerting
- [ ] Add API rate limiting
- [ ] Container orchestration with Kubernetes
