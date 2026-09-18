# Wrong Order ID – DLT Test

### 1. Send an invalid order ID

Run:

```bash
echo '1001:{"orderId":999999,"accountId":1001,"symbol":"AAPL","side":"BUY","quantity":10,"receivedAt":"2026-09-18T10:00:00Z"}' | docker-compose exec -T kafka kafka-console-producer --bootstrap-server kafka:29092 --topic orders --property parse.key=true --property key.separator=":"
```

### 2. Check the DLT

Run:

```bash
docker-compose exec kafka kafka-console-consumer \
  --bootstrap-server kafka:29092 \
  --topic orders-dlt \
  --from-beginning \
  --property print.key=true \
  --property print.headers=true
```

### 3. Expected result

Verify that:

* `orderId: 999999` appears in `orders-dlt`.
* The original message is preserved.
* The failure reason is present in the message headers.
* The message is not retried indefinitely.
