# enterprise-trading-platform
A single platform built progressively from the data layer to cloud deployment.

## Quick Start

### Prerequisites
- Docker and Docker Compose
- Java 21 (for local Backend development)
- PostgreSQL client (optional, for direct database access)

### First Run
```bash
# Copy environment template
cp .env.example .env

# Edit .env and add your Fauxnance API key
# FAUXNANCE_API_KEY=<your-key-here>

# Start the platform
docker compose up -d

# Verify all services are healthy
docker compose ps
```

### Accessing Services
- **Backend API**: http://localhost:8080
- **Auth Stub**: http://localhost:4000
- **PostgreSQL**: localhost:5432 (postgres/postgres)
- **Kafka**: localhost:9092 (external), kafka:29092 (internal)

## Architecture Overview

### Sprint 7: Kafka Event Bus

The platform uses Kafka for asynchronous order processing and data distribution:

- **orders** topic (3 partitions, keyed by accountId): Orders awaiting execution
- **trade-events** topic (3 partitions, keyed by accountId): Order fills, rejections, cancellations
- **market-data** topic (6 partitions, keyed by symbol): Quotes from Fauxnance

Topics are created automatically when the platform starts via `kafka-init` service.

### Key Concepts

**Message Envelope** (all topics):
```json
{
  "eventId": "uuid",          // Unique identifier, idempotency key
  "eventType": "ORDER_PLACED", // Discriminates payload
  "eventTime": "2025-01-01T...", // UTC timestamp
  "source": "trade-api",      // Producing component
  "schemaVersion": 1,         // For schema versioning
  "payload": {}               // Topic-specific data
}
```

**Delivery Semantics**: At-least-once
- Producers retry, so duplicates can occur
- Consumers must be idempotent
- Unknown fields in messages are ignored (forward-compatible)

## Documentation

### Infrastructure (Sprint 7)
- [infra/README.md](infra/README.md) — Local stack setup and management
- [KAFKA-DECISIONS.md](KAFKA-DECISIONS.md) — Design justifications for partition counts and keys

### Contracts
- [contracts/kafka-topics.md](contracts/kafka-topics.md) — Official Kafka topic specifications
- [contracts/trade-api.yaml](contracts/trade-api.yaml) — Trade REST API contract

### Backend
- [Backend/SPRINT-6-README.md](Backend/SPRINT-6-README.md) — Backend overview

## Common Tasks

### Start/Stop Platform
```bash
# Start all services
docker compose up -d

# View logs
docker compose logs -f kafka       # Kafka logs
docker compose logs -f backend     # Backend logs

# Stop services (keep data)
docker compose stop

# Stop and remove containers (keep volumes)
docker compose down

# Reset everything (wipe all data)
docker compose down -v
docker compose up -d
```

### Verify Kafka Topics
```bash
# List all topics
docker compose exec kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 --list

# Check topic partitions
docker compose exec kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 --describe --topic orders

# Read messages from a topic
docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 --topic trade-events --from-beginning
```

### Database Access
```bash
# Via psql from your machine
psql -h localhost -p 5432 -U postgres -d trading

# Via Docker
docker compose exec postgres psql -U postgres -d trading
```

## Development

### Building the Backend
```bash
cd Backend
mvn clean compile -DskipTests

# Run tests
mvn test

# Start Spring Boot
mvn spring-boot:run
```

Kafka consumer and producer implementation is deferred to upcoming stories.

## Sprint 7 Implementation

**Acceptance Criteria Status**: ✓ Complete

1. ✓ Topics and dead-letter topics exist with correct partitions and keys
2. ✓ Five-field message envelope implemented
3. ✓ Written justification of partition counts and key choices
4. ✓ Defensible by any team member

Consumer and producer implementations are deferred to upcoming stories.
