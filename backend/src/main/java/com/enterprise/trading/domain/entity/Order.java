package com.enterprise.trading.domain.entity;

import com.enterprise.trading.domain.enums.OrderSide;
import com.enterprise.trading.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

public class Order {

    private final long orderId;
    private final String idempotencyKey;

    private OrderStatus orderStatus;

    private final OffsetDateTime receivedAt;

    private final OrderSide orderSide;
    private final BigDecimal price;
    private final long quantity;

    private OffsetDateTime transactionDate;

    private final int accountId;
    private final int instrumentId;

    public Order(
            long orderId,
            String idempotencyKey,
            OrderStatus orderStatus,
            OffsetDateTime receivedAt,
            OrderSide orderSide,
            BigDecimal price,
            long quantity,
            OffsetDateTime transactionDate,
            int accountId,
            int instrumentId) {

        if (orderId < 1) {
            throw new IllegalArgumentException(
                    "Order ID must be at least 1");
        }

        if (idempotencyKey == null
                || idempotencyKey.isBlank()) {

            throw new IllegalArgumentException(
                    "Idempotency key is required");
        }

        if (idempotencyKey.length() > 100) {
            throw new IllegalArgumentException(
                    "Idempotency key cannot exceed 100 characters");
        }

        Objects.requireNonNull(
                orderStatus,
                "Order status is required");

        Objects.requireNonNull(
                receivedAt,
                "Received timestamp is required");

        Objects.requireNonNull(
                orderSide,
                "Order side is required");

        Objects.requireNonNull(
                price,
                "Price is required");

        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Price must be greater than zero");
        }

        if (price.scale() > 4) {
            throw new IllegalArgumentException(
                    "Price cannot have more than four decimal places");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero");
        }

        if (accountId < 1) {
            throw new IllegalArgumentException(
                    "Account ID must be at least 1");
        }

        if (instrumentId < 1) {
            throw new IllegalArgumentException(
                    "Instrument ID must be at least 1");
        }

        validateTransactionDate(
                orderStatus,
                transactionDate);

        this.orderId = orderId;
        this.idempotencyKey = idempotencyKey;
        this.orderStatus = orderStatus;
        this.receivedAt = receivedAt;
        this.orderSide = orderSide;
        this.price = price.setScale(4);
        this.quantity = quantity;
        this.transactionDate = transactionDate;
        this.accountId = accountId;
        this.instrumentId = instrumentId;
    }

    private void validateTransactionDate(
            OrderStatus status,
            OffsetDateTime transactionDate) {

        boolean terminal =
                status == OrderStatus.FILLED
                        || status == OrderStatus.REJECTED
                        || status == OrderStatus.CANCELLED;

        if (status == OrderStatus.RECEIVED
                && transactionDate != null) {

            throw new IllegalArgumentException(
                    "RECEIVED order cannot have a transaction date");
        }

        if (terminal && transactionDate == null) {
            throw new IllegalArgumentException(
                    "Terminal order must have a transaction date");
        }
    }

    public long getOrderId() {
        return orderId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public OffsetDateTime getReceivedAt() {
        return receivedAt;
    }

    public OrderSide getOrderSide() {
        return orderSide;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public long getQuantity() {
        return quantity;
    }

    public OffsetDateTime getTransactionDate() {
        return transactionDate;
    }

    public int getAccountId() {
        return accountId;
    }

    public int getInstrumentId() {
        return instrumentId;
    }

}