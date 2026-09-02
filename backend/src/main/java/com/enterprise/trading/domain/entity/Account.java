package com.enterprise.trading.domain.entity;

import com.enterprise.trading.domain.enums.AccountStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;

public class Account {

    private final int accountId;
    private final String accountNumber;
    private final LocalDate openingDate;

    private BigDecimal balance;
    private BigDecimal purchasingPower;

    private AccountStatus accountStatus;
    private final String currency;

    private final long version;

    private OffsetDateTime suspendedAt;
    private OffsetDateTime closedAt;

    private final int clientId;

    public Account(
            int accountId,
            String accountNumber,
            LocalDate openingDate,
            BigDecimal balance,
            BigDecimal purchasingPower,
            AccountStatus accountStatus,
            String currency,
            long version,
            OffsetDateTime suspendedAt,
            OffsetDateTime closedAt,
            int clientId) {

        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.openingDate = openingDate;
        this.balance = balance;
        this.purchasingPower = purchasingPower;
        this.accountStatus = accountStatus;
        this.currency = currency;
        this.version = version;
        this.suspendedAt = suspendedAt;
        this.closedAt = closedAt;
        this.clientId = clientId;
    }

    public int getAccountId() {
        return accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public LocalDate getOpeningDate() {
        return openingDate;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getPurchasingPower() {
        return purchasingPower;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public String getCurrency() {
        return currency;
    }

    public long getVersion() {
        return version;
    }

    public OffsetDateTime getSuspendedAt() {
        return suspendedAt;
    }

    public OffsetDateTime getClosedAt() {
        return closedAt;
    }

    public int getClientId() {
        return clientId;
    }


}