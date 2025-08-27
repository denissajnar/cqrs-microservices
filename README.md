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
docker-compose up -d
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

#### Update Order

```http
PUT /api/v1/orders/update/{id}
Content-Type: application/json

{
  "customerId": 1,
  "totalAmount": 149.99,
  "status": "COMPLETED"
}
```

#### Delete Order

```http
DELETE /api/v1/orders/{id}
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
GET /api/v1/orders/by-status?status=PENDING
```

### Status Values

- `PENDING`
- `CONFIRMED`
- `SHIPPED`
- `DELIVERED`
- `CANCELLED`

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
- [ ] Implement message retry
- [ ] Implement DLQ
- [ ] Add authentication and authorization
- [ ] Implement distributed tracing
- [ ] Add comprehensive monitoring and alerting
- [ ] Implement event sourcing
- [ ] Add API rate limiting
- [ ] Container orchestration with Kubernetes
