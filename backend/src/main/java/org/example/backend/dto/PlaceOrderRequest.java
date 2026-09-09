package org.example.backend.dto;

import org.example.backend.enums.OrderSide;

import java.math.BigDecimal;
import java.util.Objects;

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

        Objects.requireNonNull(
                accountId,
                "Account ID is required");

        if (accountId < 1) {
            throw new IllegalArgumentException(
                    "Account ID must be at least 1");
        }

        Objects.requireNonNull(
                symbol,
                "Symbol is required");

        if (symbol.isBlank() || symbol.length() > 20) {
            throw new IllegalArgumentException(
                    "Symbol must be between 1 and 20 characters");
        }

        Objects.requireNonNull(
                side,
                "Side is required");

        Objects.requireNonNull(
                quantity,
                "Quantity is required");

        if (quantity < 1) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero");
        }

        Objects.requireNonNull(
                price,
                "Price is required");

        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Price must be greater than zero");
        }

        if (price.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException(
                    "Price cannot have more than two decimal places");
        }

        Objects.requireNonNull(
                idempotencyKey,
                "Idempotency key is required");

        if (idempotencyKey.length() < 8 || idempotencyKey.length() > 100) {
            throw new IllegalArgumentException(
                    "Idempotency key must be between 8 and 100 characters");
        }

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
