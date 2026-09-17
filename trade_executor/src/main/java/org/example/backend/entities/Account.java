package org.example.backend.entities;

import org.example.backend.enums.AccountStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class Account {

    private final int accountId;
    private final String accountNumber;
    private final LocalDate openedDate;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private final AccountStatus accountStatus;
    private final String currency;
    private long version;
    private final Object closedAt;
    private final Object createdBy;
    private final int branchId;

    public Account(
            int accountId,
            String accountNumber,
            LocalDate openedDate,
            BigDecimal balance,
            BigDecimal availableBalance,
            AccountStatus accountStatus,
            String currency,
            long version,
            Object closedAt,
            Object createdBy,
            int branchId) {

        this.accountId = accountId;
        this.accountNumber = Objects.requireNonNull(accountNumber, "accountNumber is required");
        this.openedDate = Objects.requireNonNull(openedDate, "openedDate is required");
        this.balance = Objects.requireNonNull(balance, "balance is required");
        this.availableBalance = Objects.requireNonNull(availableBalance, "availableBalance is required");
        this.accountStatus = Objects.requireNonNull(accountStatus, "accountStatus is required");
        this.currency = Objects.requireNonNull(currency, "currency is required");
        this.version = version;
        this.closedAt = closedAt;
        this.createdBy = createdBy;
        this.branchId = branchId;
    }

    public int getAccountId() {
        return accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public LocalDate getOpenedDate() {
        return openedDate;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
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

    public Object getClosedAt() {
        return closedAt;
    }

    public Object getCreatedBy() {
        return createdBy;
    }

    public int getBranchId() {
        return branchId;
    }

    public boolean canAfford(BigDecimal amount) {
        return amount != null && balance.compareTo(amount) >= 0;
    }

    public void debit(BigDecimal amount) {
        validateAmount(amount);
        balance = balance.subtract(amount);
        availableBalance = availableBalance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        validateAmount(amount);
        balance = balance.add(amount);
        availableBalance = availableBalance.add(amount);
    }

    private static void validateAmount(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount is required");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount cannot be negative");
        }
    }
}

