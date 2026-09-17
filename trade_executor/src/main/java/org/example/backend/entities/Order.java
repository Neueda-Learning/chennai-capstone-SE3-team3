package org.example.backend.entities;

import org.example.backend.enums.OrderPricingType;
import org.example.backend.enums.OrderSide;
import org.example.backend.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

public class Order {

    private final long orderId;
    private final String idempotencyKey;
    private OrderStatus orderStatus;
    private final OffsetDateTime createdAt;
    private final OrderSide orderSide;
    private final OrderPricingType pricingType;
    private final BigDecimal price;
    private final long quantity;
    private final BigDecimal executionPrice;
    private final int accountId;
    private final int instrumentId;

    public Order(
            long orderId,
            String idempotencyKey,
            OrderStatus orderStatus,
            OffsetDateTime createdAt,
            OrderSide orderSide,
            OrderPricingType pricingType,
            BigDecimal price,
            long quantity,
            BigDecimal executionPrice,
            int accountId,
            int instrumentId) {

        this.orderId = orderId;
        this.idempotencyKey = Objects.requireNonNull(idempotencyKey, "idempotencyKey is required");
        this.orderStatus = Objects.requireNonNull(orderStatus, "orderStatus is required");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.orderSide = Objects.requireNonNull(orderSide, "orderSide is required");
        this.pricingType = Objects.requireNonNull(pricingType, "pricingType is required");
        this.price = price;
        this.quantity = quantity;
        this.executionPrice = executionPrice;
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

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = Objects.requireNonNull(orderStatus, "orderStatus is required");
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OrderSide getOrderSide() {
        return orderSide;
    }

    public OrderPricingType getPricingType() {
        return pricingType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public long getQuantity() {
        return quantity;
    }

    public BigDecimal getExecutionPrice() {
        return executionPrice;
    }

    public int getAccountId() {
        return accountId;
    }

    public int getInstrumentId() {
        return instrumentId;
    }
}

