# Sports Betting Settlement Trigger Service

## Overview
Spring Boot service that publishes sports event outcomes to Kafka, consumes them to settle in-memory bets stored in H2, and mocks RocketMQ by logging settlement payloads.

## Prerequisites
- JDK 21+
- Maven 3.9+
- Docker & Docker Compose (for Kafka)

## Quick Start

### 1. Start Kafka Environment
```bash
docker-compose up -d
```

Verify services are running:
```bash
docker-compose ps
```

This starts:
- **Kafka Broker** at `localhost:9092`
- **Zookeeper** at `localhost:2181`
- **Kafka UI** at `http://localhost:8083` (for monitoring topics and messages)
- **RocketMQ NameServer** at `localhost:9876`
- **RocketMQ Broker** at `localhost:10911`
- **RocketMQ Console** at `http://localhost:8082` (for monitoring RocketMQ messages)

### 2. Run the Application
```bash
./mvnw spring-boot:run
```

The API is available at `http://localhost:8084`.

### 3. Test the API

Publish an event outcome:
```bash
curl -X POST http://localhost:8084/api/event-outcomes \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": 5001,
    "eventName": "Match A",
    "eventWinnerId": 9001
  }'
```

The service will:
1. Publish the outcome to Kafka topic `event-outcomes`
2. Consumer listens and retrieves unsettled bets for event 5001
3. Settles the bets (marks won/lost based on winner match)
4. Sends settlement messages to RocketMQ topic `bet-settlements` using **transactional messages**
4. Sends settlement messages to RocketMQ topic `bet-settlements`

**Note**: RocketMQ transactional messages guarantee that settlements are only sent if database updates succeed. See `TRANSACTIONAL_MESSAGES.md` for details.

## Database


### Tables
- Event ID `5001`: 3 bets (winner IDs: 9001, 9002, 9001)
- Event ID `6001`: 1 bet (winner ID: 9100)
- Event ID `5002`: 1 bet (winner ID: 9200)

### H2 Console
Access H2 console at `http://localhost:8084/h2-console`:
- JDBC URL: `jdbc:h2:mem:betsettlement`
- Username: `sa`
- Password: _(empty)_

## API Documentation

### Endpoints

#### POST /api/event-outcomes
Publishes sports event outcome to Kafka.

**Request:**
```json
{
  "eventId": 5001,
  "eventName": "Match A",
  "eventWinnerId": 9001
}
```

**Response:** `202 Accepted`
```json
{
  "eventId": 5001,
  "eventName": "Match A",
  "eventWinnerId": 9001
}
```

## Monitoring

### Kafka UI
Browse to `http://localhost:8083` to:
- View topics and messages
- Monitor consumer groups
- Check broker health

### RocketMQ Console
Browse to `http://localhost:8082` to:
- View topics and messages in `bet-settlements` topic
- Monitor message status and delivery
- Check broker and nameserver status
- View consumer groups and subscriptions

### Application Logs
Settlement results are logged as:
```
INFO  c.s.b.p.SettlementProducer - Sent bet settlement to RocketMQ - Topic: bet-settlements, BetId: 1, MsgId: xxx, Status: SEND_OK
```

## Stop Services

### Stop Application
Press `Ctrl+C` in the terminal running the application.

### Stop Kafka
```bash
docker-compose down
```

To remove volumes (clean state):
```bash
docker-compose down -v
```

## Technologies
- Spring Boot 3.4.2
- Spring Kafka
- Spring Data JPA
- H2 Database
- Liquibase
- Apache RocketMQ 5.1.4
- MapStruct
- Lombok
- Docker Compose
