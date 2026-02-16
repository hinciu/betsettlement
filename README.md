# Sports Betting Settlement Trigger Service - Technical Documentation

## Overview

The **Sports Betting Settlement Trigger Service** is a Spring Boot-based microservice designed to handle sports event outcomes and automatically settle corresponding bets. The service demonstrates event-driven architecture using Apache Kafka for event ingestion and Apache RocketMQ for settlement message distribution.

## Project Structure

```
betsettlement/
├── src/
│   ├── main/
│   │   ├── java/com/sprotygroup/betsettlement/
│   │   │   ├── BetsettlementApplication.java      # Main Spring Boot application
│   │   │   ├── config/
│   │   │   │   ├── Config.java                    # General configuration
│   │   │   │   ├── KafkaConfig.java               # Kafka consumer configuration
│   │   │   │   └── RocketMQProperties.java        # RocketMQ properties binding
│   │   │   ├── controller/
│   │   │   │   └── EventOutcomeController.java    # REST API endpoint
│   │   │   ├── dto/
│   │   │   │   └── EventOutcomeRequest.java       # Request DTO with validation
│   │   │   ├── event/
│   │   │   │   ├── BetSettlement.java             # Settlement message record
│   │   │   │   ├── EventOutcome.java              # Event outcome record
│   │   │   │   └── EventType.java                 # Message type enum
│   │   │   ├── exception/
│   │   │   │   └── SettlementException.java       # Custom exception
│   │   │   ├── listener/
│   │   │   │   └── EventOutcomeListener.java      # Kafka consumer
│   │   │   ├── mapper/
│   │   │   │   ├── BetSettlementMapper.java       # MapStruct mapper
│   │   │   │   └── EventOutcomeMapper.java        # MapStruct mapper
│   │   │   ├── model/
│   │   │   │   ├── Bet.java                       # Bet JPA entity
│   │   │   │   ├── EventOutcomeStatus.java        # Status enum
│   │   │   │   └── FailedEventOutcome.java        # Failed event tracking
│   │   │   ├── producer/
│   │   │   │   ├── EventOutcomeProducer.java      # Kafka producer
│   │   │   │   ├── SettlementProducer.java        # RocketMQ producer
│   │   │   │   └── BetSettlementTransactionListener.java # Transaction handler
│   │   │   ├── repository/
│   │   │   │   ├── BetRepository.java             # JPA repository
│   │   │   │   └── FailedEventOutcomeRepository.java
│   │   │   └── service/
│   │   │       ├── BetSettlementService.java      # Core settlement logic
│   │   │       └── FailedEventOutcomeService.java # Error tracking
│   │   └── resources/
│   │       ├── application.yml                    # Application configuration
│   │       ├── schema.sql                         # Database schema
│   │       └── data.sql                           # Test data
│   └── test/
│       ├── java/com/sprotygroup/betsettlement/
│       │   └── integration/
│       │       ├── BaseIT.java                    # Base integration test
│       │       └── BetSettlementIntegrationTest.java # Full integration tests
│       └── resources/
│           ├── application.yml                    # Test configuration
│           ├── mock-request/                      # Test JSON payloads
│           └── sql/                               # Test SQL scripts
├── docker/
│   └── rocketmq/
│       └── broker.conf                            # RocketMQ broker config
├── docker-compose.yml                             # Multi-container orchestration
├── Dockerfile                                     # Application container image
├── pom.xml                                        # Maven dependencies
└── README.md                                      # User guide
```

## Technology Stack

### Core Framework
- **Spring Boot 4.0.2** - Application framework
- **Java 21** - Programming language with modern features (records, pattern matching)

### Messaging
- **Apache Kafka** - Event streaming platform for event outcomes
- **Apache RocketMQ 5.1.4** - Message queue for settlement distribution
- **Spring Kafka** - Kafka integration with Spring
- **RocketMQ Spring Boot Starter 2.3.0** - RocketMQ integration

### Data Persistence
- **Spring Data JPA** - Data access layer
- **H2 Database** - In-memory database (runtime scope)
- **Hibernate** - JPA implementation

### API & Documentation
- **Spring Web** - REST API framework
- **Spring Validation** - Bean validation with Jakarta Validation
- **Springdoc OpenAPI 2.3.0** - Swagger UI and OpenAPI 3 documentation

### Code Generation & Utilities
- **MapStruct 1.5.5.Final** - Type-safe bean mapping
- **Lombok** - Boilerplate code reduction
- **Jackson** - JSON serialization/deserialization

### Testing
- **Spring Boot Test** - Testing framework
- **JUnit 5** - Testing framework
- **Spring Kafka Test** - Embedded Kafka for integration tests
- **Awaitility 4.2.0** - Asynchronous testing utilities
- **Mockito** - Mocking framework

### DevOps
- **Docker & Docker Compose** - Containerization and orchestration
- **Maven** - Build and dependency management

## Setup and Installation

### Prerequisites

#### Required
- **Docker** (version 20.10+)
- **Docker Compose** (version 2.0+)

#### Optional (for local development)
- **JDK 21** or higher
- **Maven 3.9+** (or use included Maven wrapper `./mvnw`)

### Verify Prerequisites

```bash
# Check Docker
docker --version

# Check Docker Compose
docker-compose --version

# Optional: Check Java (for local development)
java -version

# Optional: Check Maven (for local development)
mvn --version
```

## Running the Application

### Option 1: Full Docker Compose Setup (Recommended)

This is the **easiest and recommended** approach. All services run in containers.

#### Start All Services

```bash
# Clone the repository (if not already done)
cd /path/to/betsettlement

# Start all services (builds application automatically)
docker-compose up -d

# Verify all containers are running
docker-compose ps

# Expected output: 9 services running
# - betsettlement (application)
# - kafka
# - zookeeper
# - kafka-ui
# - rocketmq-namesrv
# - rocketmq-broker
# - rocketmq-console
# - rocketmq-init
```

#### Check Service Status

```bash
# View application logs
docker-compose logs -f betsettlement

# View all logs
docker-compose logs -f

# Check specific service
docker-compose logs kafka
```

#### Access Services

- **Application API**: http://localhost:8084
- **Swagger UI**: http://localhost:8084/swagger-ui.html
- **H2 Console**: http://localhost:8084/h2-console
- **Kafka UI**: http://localhost:8083
- **RocketMQ Console**: http://localhost:8082

### Option 2: Local Application with Docker Dependencies

Run Kafka and RocketMQ in Docker, but run the application locally for development.

#### Step 1: Start Infrastructure

```bash
# Start only Kafka and RocketMQ services
docker-compose up -d zookeeper kafka kafka-ui rocketmq-namesrv rocketmq-broker rocketmq-console rocketmq-init
```

#### Step 2: Build and Run Application Locally

```bash
# Using Maven wrapper (recommended)
./mvnw clean install
./mvnw spring-boot:run

# OR using installed Maven
mvn clean install
mvn spring-boot:run

# Application starts on port 8084
```

#### Step 3: Verify Application

```bash
# Check application health
curl http://localhost:8084/actuator/health

# Access Swagger UI
open http://localhost:8084/swagger-ui.html
```

### Option 3: Manual Build and Run (Without Docker)

**Note**: This requires manual installation of Kafka and RocketMQ.

#### Prerequisites
- Kafka broker running on `localhost:9092`
- RocketMQ NameServer running on `localhost:9876`

#### Build and Run

```bash
# Build the application
./mvnw clean package -DskipTests

# Run the JAR
java -jar target/betsettlement-0.0.1-SNAPSHOT.jar

# With custom properties
java -jar target/betsettlement-0.0.1-SNAPSHOT.jar \
  --spring.kafka.bootstrap-servers=localhost:9092 \
  --rocketmq.name-server=localhost:9876
```

## API Documentation

### Interactive API Documentation

Once the application is running, access the **Swagger UI** at:

```
http://localhost:8084/swagger-ui.html
```

This provides:
- Interactive API exploration
- Request/response examples
- Schema definitions
- Try-it-out functionality

### OpenAPI Specification

Raw OpenAPI 3.0 specification available at:

```
http://localhost:8084/api-docs
```

### Endpoints

#### POST `/api/event-outcomes`

Publishes a sports event outcome to Kafka, triggering the bet settlement process.

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "eventId": 5001,
  "eventName": "Championship Final",
  "eventWinnerId": 9001
}
```

**Field Validation:**
- `eventId`: Required, must be a positive Long
- `eventName`: Required, cannot be blank
- `eventWinnerId`: Required, must be a positive Long

**Response: 202 Accepted**
```json
{
  "eventId": 5001,
  "eventName": "Championship Final",
  "eventWinnerId": 9001
}
```

**Error Responses:**

- **400 Bad Request**: Validation failed
```json
{
  "timestamp": "2026-02-16T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/event-outcomes"
}
```

- **500 Internal Server Error**: Failed to publish to Kafka
```json
{
  "timestamp": "2026-02-16T10:30:00.000+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Failed to publish event outcome",
  "path": "/api/event-outcomes"
}
```
