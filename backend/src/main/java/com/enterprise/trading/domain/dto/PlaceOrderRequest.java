package com.enterprise.trading.domain.dto;

import com.enterprise.trading.domain.enums.OrderSide;

import java.math.BigDecimal;

/**
 * Order placement request. Models the {@code PlaceOrderRequest} schema in
 * {@code contracts/trade-api.yaml}, which is binding. These are business
 * constraints, enforced by hand, not transport constraints.
 */
public class PlaceOrderRequest {

    private final Long accountId;
    private final String symbol;
    private final OrderSide side;
    private final Integer quantity;
    private final BigDecimal price;
    private final String idempotencyKey;

    public PlaceOrderRequest(
            Long accountId,
            String symbol,
            OrderSide side,
            Integer quantity,
            BigDecimal price,
            String idempotencyKey) {
        this.accountId = accountId;
        this.symbol = symbol;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.idempotencyKey = idempotencyKey;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getSymbol() {
        return symbol;
    }

    public OrderSide getSide() {
        return side;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}
