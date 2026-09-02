package com.enterprise.trading.domain.entity;


import com.enterprise.trading.domain.enums.AccountStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Account")
class AccountTest {

    @Test
    @DisplayName("stores the initial balance")
    void storesInitialBalance() {
        Account account = new Account(
                1,
                "ETP000000001",
                LocalDate.of(2026, 9, 2),
                new BigDecimal("1000.00"),
                new BigDecimal("1000.00"),
                AccountStatus.ACTIVE,
                "INR",
                1L,
                null,
                null,
                10
        );

        assertEquals(new BigDecimal("1000.00"), account.getBalance());
    }

    @Test
    @DisplayName("increases balance when credited")
    void increasesBalanceWhenCredited() {
        Account account = new Account(
                1,
                "ETP000000001",
                LocalDate.of(2026, 9, 2),
                new BigDecimal("1000.00"),
                new BigDecimal("1000.00"),
                AccountStatus.ACTIVE,
                "INR",
                1L,
                null,
                null,
                10
        );

        account.credit(new BigDecimal("250.00"));

        assertEquals(new BigDecimal("1250.00"), account.getBalance());
    }

    @Test
    @DisplayName("decreases balance when debited")
    void decreasesBalanceWhenDebited() {
        Account account = new Account(
                1,
                "ETP000000001",
                LocalDate.of(2026, 9, 2),
                new BigDecimal("1000.00"),
                new BigDecimal("1000.00"),
                AccountStatus.ACTIVE,
                "INR",
                1L,
                null,
                null,
                10
        );

        account.debit(new BigDecimal("250.00"));

        assertEquals(new BigDecimal("750.00"), account.getBalance());
    }

    @Test
    @DisplayName("rejects a debit that would make the balance negative")
    void rejectsADebitThatWouldMakeTheBalanceNegative() {
        Account account = new Account(
                1,
                "ETP000000001",
                LocalDate.of(2026, 9, 2),
                new BigDecimal("1000.00"),
                new BigDecimal("1000.00"),
                AccountStatus.ACTIVE,
                "INR",
                1L,
                null,
                null,
                10
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> account.debit(new BigDecimal("1001.00"))
        );

        assertEquals(new BigDecimal("1000.00"), account.getBalance());
    }

    @Test
    @DisplayName("can afford an amount less than or equal to the balance")
    void canAffordAnAmountLessThanOrEqualToTheBalance() {
        Account account = new Account(
                1,
                "ETP000000001",
                LocalDate.of(2026, 9, 2),
                new BigDecimal("1000.00"),
                new BigDecimal("1000.00"),
                AccountStatus.ACTIVE,
                "INR",
                1L,
                null,
                null,
                10
        );

        assertTrue(account.canAfford(new BigDecimal("1000.00")));
    }

    @Test
    @DisplayName("cannot afford an amount greater than the balance")
    void cannotAffordAnAmountGreaterThanTheBalance() {
        Account account = new Account(
                1,
                "ETP000000001",
                LocalDate.of(2026, 9, 2),
                new BigDecimal("1000.00"),
                new BigDecimal("1000.00"),
                AccountStatus.ACTIVE,
                "INR",
                1L,
                null,
                null,
                10
        );

        assertFalse(account.canAfford(new BigDecimal("1000.01")));
    }

    @Test
    @DisplayName("doesn't accumulate monetary drift across repeated ops")
    void doesntAccumulateMonetaryDriftingAcrossRepeatedOps() {
        Account account = new Account(
                1,
                "ETP000000001",
                LocalDate.of(2026, 9, 2),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                AccountStatus.ACTIVE,
                "INR",
                1L,
                null,
                null,
                10
        );

        for (int i = 0; i < 1000; i++) {
            account.credit(new BigDecimal("0.10"));
            account.debit(new BigDecimal("0.10"));
        }

        assertEquals(new BigDecimal("0.00"), account.getBalance());
    }

    @Test
    @DisplayName("preserves two decimal places for monetary values")
    void preservesTwoDecimalPlacesForMonetaryValues() {
        Account account = new Account(
                1,
                "ETP000000001",
                LocalDate.of(2026, 9, 2),
                new BigDecimal("1000.00"),
                new BigDecimal("1000.00"),
                AccountStatus.ACTIVE,
                "INR",
                1L,
                null,
                null,
                10
        );

        account.credit(new BigDecimal("100.10"));

        assertEquals(2, account.getBalance().scale());
    }

    @Test
    @DisplayName("rejects a null credit amount")
    void rejectsNullCreditAmount() {
        Account account = new Account(
                1,
                "ETP000000001",
                LocalDate.of(2026, 9, 2),
                new BigDecimal("1000.00"),
                new BigDecimal("1000.00"),
                AccountStatus.ACTIVE,
                "INR",
                1L,
                null,
                null,
                10
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> account.credit(null)
        );
    }

    @Test
    @DisplayName("rejects a non-positive credit amount")
    void rejectsNonPositiveCreditAmount() {
        Account account = new Account(
                1,
                "ETP000000001",
                LocalDate.of(2026, 9, 2),
                new BigDecimal("1000.00"),
                new BigDecimal("1000.00"),
                AccountStatus.ACTIVE,
                "INR",
                1L,
                null,
                null,
                10
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> account.credit(BigDecimal.ZERO)
        );
    }

    @Test
    @DisplayName("rejects a null debit amount")
    void rejectsNullDebitAmount() {
        Account account = new Account(
                1,
                "ETP000000001",
                LocalDate.of(2026, 9, 2),
                new BigDecimal("1000.00"),
                new BigDecimal("1000.00"),
                AccountStatus.ACTIVE,
                "INR",
                1L,
                null,
                null,
                10
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> account.debit(null)
        );
    }

    @Test
    @DisplayName("rejects a non-positive debit amount")
    void rejectsNonPositiveDebitAmount() {
        Account account = new Account(
                1,
                "ETP000000001",
                LocalDate.of(2026, 9, 2),
                new BigDecimal("1000.00"),
                new BigDecimal("1000.00"),
                AccountStatus.ACTIVE,
                "INR",
                1L,
                null,
                null,
                10
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> account.debit(BigDecimal.ZERO)
        );
    }

    @Test
    @DisplayName("starts with the supplied status")
    void startsWithTheSuppliedStatus() {
        Account account = new Account(
                1,
                "ETP000000001",
                LocalDate.of(2026, 9, 2),
                new BigDecimal("1000.00"),
                new BigDecimal("1000.00"),
                AccountStatus.ACTIVE,
                "INR",
                1L,
                null,
                null,
                10
        );

        assertEquals(AccountStatus.ACTIVE, account.getAccountStatus());
    }

    @Test
    @DisplayName("account can be suspended")
    void accountCanBeSuspended() {
        Account account = new Account(
                1,
                "ETP000000001",
                LocalDate.of(2026, 9, 2),
                new BigDecimal("1000.00"),
                new BigDecimal("1000.00"),
                AccountStatus.ACTIVE,
                "INR",
                1L,
                null,
                null,
                10
        );

        account.suspend();

        assertEquals(AccountStatus.SUSPENDED, account.getAccountStatus());
    }

    @Test
    @DisplayName("can be resumed from suspended status")
    void canBeResumedFromSuspendedStatus() {
        Account account = new Account(
                1,
                "ETP000000001",
                LocalDate.of(2026, 9, 2),
                new BigDecimal("1000.00"),
                new BigDecimal("1000.00"),
                AccountStatus.SUSPENDED,
                "INR",
                1L,
                null,
                null,
                10
        );

        account.activate();

        assertEquals(AccountStatus.ACTIVE, account.getAccountStatus());
    }

    @Test
    @DisplayName("can be closed")
    void canBeClosed() {
        Account account = new Account(
                1,
                "ETP000000001",
                LocalDate.of(2026, 9, 2),
                new BigDecimal("1000.00"),
                new BigDecimal("1000.00"),
                AccountStatus.ACTIVE,
                "INR",
                1L,
                null,
                null,
                10
        );

        account.close();

        assertEquals(AccountStatus.CLOSED, account.getAccountStatus());
    }

    @Test
    @DisplayName("can't resume a closed account")
    void canNotResumeAClosedAccount() {
        Account account = new Account(
                1,
                "ETP000000001",
                LocalDate.of(2026, 9, 2),
                new BigDecimal("1000.00"),
                new BigDecimal("1000.00"),
                AccountStatus.CLOSED,
                "INR",
                1L,
                null,
                null,
                10
        );

        assertThrows(
                IllegalStateException.class,
                account::activate
        );
    }
}

