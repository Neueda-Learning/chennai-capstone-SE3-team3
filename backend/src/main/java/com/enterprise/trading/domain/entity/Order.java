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