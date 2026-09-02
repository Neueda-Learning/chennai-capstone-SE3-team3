package com.enterprise.trading.domain.entity;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Holding {

    @Positive
    private final long holdingId;

    @PositiveOrZero
    private long quantity;

    @NotNull
    @DecimalMin(value = "0.00")
    @Digits(integer = 17, fraction = 2)
    private BigDecimal purchasePrice;

    @Positive
    private final int accountId;

    @Positive
    private final int instrumentId;

    public Holding(
            long holdingId,
            long quantity,
            BigDecimal purchasePrice,
            int accountId,
            int instrumentId) {

        this.holdingId = holdingId;
        this.quantity = quantity;

        if (purchasePrice == null) {
            throw new IllegalArgumentException("Purchase price cannot be null");
        }

        if (purchasePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Purchase price cannot be negative");
        }

        if (purchasePrice.scale() > 2) {
            throw new IllegalArgumentException(
                    "Purchase price must have at most 2 decimal places"
            );
        }

        this.purchasePrice = purchasePrice.setScale(2, RoundingMode.HALF_UP);

        this.accountId = accountId;
        this.instrumentId = instrumentId;
    }

    public long getHoldingId() {
        return holdingId;
    }

    public long getQuantity() {
        return quantity;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public int getAccountId() {
        return accountId;
    }

    public int getInstrumentId() {
        return instrumentId;
    }

    public void createHolding(long quantity) {

        if (quantity < 0) {
            throw new IllegalArgumentException(
                    "Quantity cannot be negative"
            );
        }

        this.quantity = quantity;
    }
}

