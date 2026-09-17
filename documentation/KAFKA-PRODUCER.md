# Kafka Producer Verification Guide

This runbook verifies the Sprint 7 producer behavior end-to-end on Linux after you submit POST /api/v1/orders from Bruno.

## Scope

What this guide proves:

- Accepted orders are persisted with status NEW and API responds NEW.
- ORDER_PLACED is published to topic orders.
- Kafka key is the accountId.
- Event publish is after commit (recoverable if publish fails).
- Validation failures publish nothing.

## Current Implementation Locations

- Order placement and persistence flow: [TradeService.java](../backend/src/main/java/org/example/backend/service/TradeService.java)
- After-commit publish hook: [OrderPlacedAfterCommitListener.java](../backend/src/main/java/org/example/backend/events/OrderPlacedAfterCommitListener.java)
- Kafka producer send logic: [OrderEventProducer.java](../backend/src/main/java/org/example/backend/events/OrderEventProducer.java)
- Producer configuration: [KafkaProducerConfiguration.java](../backend/src/main/java/org/example/backend/config/KafkaProducerConfiguration.java)
- Runtime Kafka properties: [application.properties](../backend/src/main/resources/application.properties)

## Prerequisites

- Platform is running with kafka, postgres, auth-stub, backend.
- You can call backend API from Bruno.
- This guide includes both Compose v2 (`docker compose`) and Compose v1 (`docker-compose`) forms.

## 1) Confirm Topic Exists

Compose v2:

```bash
docker compose exec kafka kafka-topics \
  --bootstrap-server kafka:29092 --describe --topic orders
```

Compose v1:

```bash
docker-compose exec kafka kafka-topics \
  --bootstrap-server kafka:29092 --describe --topic orders
```

Expected:

- Topic orders exists.
- Partition count is 3 (per team decision).

## 2) Start Live Consumer (before API call)

Open a separate terminal and keep this running:

Compose v2:

```bash
docker compose exec kafka kafka-console-consumer \
  --bootstrap-server kafka:29092 \
  --topic orders \
  --property print.key=true \
  --property key.separator=" | " \
  --property print.partition=true \
  --property print.timestamp=true
```

Compose v1:

```bash
docker-compose exec kafka kafka-console-consumer \
  --bootstrap-server kafka:29092 \
  --topic orders \
  --property print.key=true \
  --property key.separator=" | " \
  --property print.partition=true \
  --property print.timestamp=true
```

Why first: this makes it obvious that your new API request produced a new event.

## 3) Get a Valid JWT

```bash
curl -s -X POST http://localhost:4000/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"mission123"}'
```

Use the returned token as Bearer in Bruno.

## 4) Send Valid POST /api/v1/orders from Bruno

Endpoint:

- http://localhost:8080/api/v1/orders

Headers:

- Authorization: Bearer <token>
- Content-Type: application/json

Body example (MARKET, no price):

```json
{
  "accountId": 1001,
  "symbol": "ACME",
  "side": "BUY",
  "orderPricingType": "MARKET",
  "quantity": 10,
  "idempotencyKey": "idem-linux-001"
}
```

Expected API result:

- HTTP success.
- status is NEW.
- orderPricingType is MARKET.
- price is null.

## 5) Verify DB Commit

Compose v2:

```bash
docker compose exec postgres sh -lc 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "select order_id, account_id, order_status, order_side, pricing_type, price, quantity, idempotency_key, received_at from orders order by order_id desc limit 5;"'
```

Compose v1:

```bash
docker-compose exec postgres sh -lc 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "select order_id, account_id, order_status, order_side, pricing_type, price, quantity, idempotency_key, received_at from orders order by order_id desc limit 5;"'
```

Expected:

- Latest order row exists.
- order_status is NEW.
- account_id matches request.

## 6) Verify Kafka Event

In the live consumer output from step 2, confirm:

- Message appears on topic orders.
- Printed key equals accountId (for example 1001).
- Envelope eventType is ORDER_PLACED.

## 7) Prove Publish Is After Commit (recoverable failure)

1. Stop Kafka only:

Compose v2:

```bash
docker compose stop kafka
```

Compose v1:

```bash
docker-compose stop kafka
```

2. Submit another valid order from Bruno with a new idempotency key.

Expected:

- API still accepts and returns NEW.
- Row exists in orders table.
- Backend logs an error that publish failed after commit and replay is required.

3. Start Kafka again:

Compose v2:

```bash
docker compose start kafka
```

Compose v1:

```bash
docker-compose start kafka
```

This proves commit can succeed even if publish fails, matching recoverable-failure choice.

## 8) Validation Failure Publishes Nothing

Keep consumer running and send an invalid request.

Example invalid body: LIMIT without price.

```json
{
  "accountId": 1001,
  "symbol": "ACME",
  "side": "BUY",
  "orderPricingType": "LIMIT",
  "quantity": 10,
  "idempotencyKey": "idem-invalid-001"
}
```

Expected:

- API returns 4xx validation/domain error.
- No new row committed for that failed request.
- No new message appears on topic orders.

## 9) Optional Unit Test Quick Run

```bash
cd backend
./mvnw -Dtest=TradeServiceTransactionTest,OrderEventProducerTest,OrderPlacedAfterCommitListenerTest test
```

## Producer Reliability Settings Checklist

Current expected producer settings:

- acks=all
- enable.idempotence=true
- retries set to a high value
- max.in.flight.requests.per.connection=5

Check current values:

```bash
grep -n "spring.kafka.producer" backend/src/main/resources/application.properties
```

## Notes

- Idempotent producer removes duplicates caused by producer retries, not duplicates caused by application-level retries.
- Executor/consumer idempotency is still required.
