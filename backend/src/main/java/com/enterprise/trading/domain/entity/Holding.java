package com.enterprise.trading.domain.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Holding {

    private final long holdingId;

    private long quantity;
    private BigDecimal purchasePrice;

    private final int accountId;
    private final int instrumentId;

    public Holding(
            long holdingId,
            long quantity,
            BigDecimal purchasePrice,
            int accountId,
            int instrumentId) {

        this.holdingId = holdingId;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice.setScale(4);
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


}