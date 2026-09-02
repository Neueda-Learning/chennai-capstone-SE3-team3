package com.enterprise.trading.domain.exception;

import com.enterprise.trading.domain.enums.AccountStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DomainExceptionHierarchyTest {

    @Test
    void accountNotFoundCarriesCatalogueCodeAndMessage() {

        AccountNotFoundException exception = new AccountNotFoundException(1L);

        assertInstanceOf(DomainException.class, exception);
        assertEquals("ACC-404", exception.getErrorCode());
        assertEquals("Account not found", exception.getMessage());
        assertEquals(1L, exception.getAccountId());
    }

    @Test
    void accountNotActiveCarriesCatalogueCodeAndMessage() {

        AccountNotActiveException exception =
                new AccountNotActiveException(1L, AccountStatus.SUSPENDED);

        assertInstanceOf(DomainException.class, exception);
        assertEquals("ACC-403", exception.getErrorCode());
        assertEquals("Account not active", exception.getMessage());
        assertEquals(1L, exception.getAccountId());
        assertEquals(AccountStatus.SUSPENDED, exception.getStatus());
    }

    @Test
    void instrumentNotFoundCarriesCatalogueCodeAndMessage() {

        InstrumentNotFoundException exception = new InstrumentNotFoundException("ACME");

        assertInstanceOf(DomainException.class, exception);
        assertEquals("INS-404", exception.getErrorCode());
        assertEquals("Instrument not found", exception.getMessage());
        assertEquals("ACME", exception.getSymbol());
    }

    @Test
    void insufficientFundsCarriesCatalogueCodeAndMessage() {

        InsufficientFundsException exception = new InsufficientFundsException(
                1L,
                new BigDecimal("2550.00"),
                new BigDecimal("1000.00"));

        assertInstanceOf(DomainException.class, exception);
        assertEquals("ORD-400", exception.getErrorCode());
        assertEquals("Insufficient funds", exception.getMessage());
        assertEquals(1L, exception.getAccountId());
        assertEquals(new BigDecimal("2550.00"), exception.getRequired());
        assertEquals(new BigDecimal("1000.00"), exception.getAvailable());
    }

    @Test
    void insufficientHoldingsCarriesCatalogueCodeAndMessage() {

        InsufficientHoldingsException exception =
                new InsufficientHoldingsException(1L, "ACME", 100L, 40L);

        assertInstanceOf(DomainException.class, exception);
        assertEquals("ORD-409", exception.getErrorCode());
        assertEquals("Insufficient holdings", exception.getMessage());
        assertEquals(1L, exception.getAccountId());
        assertEquals("ACME", exception.getSymbol());
        assertEquals(100L, exception.getRequestedQuantity());
        assertEquals(40L, exception.getAvailableQuantity());
    }

    @Test
    void duplicateOrderCarriesCatalogueCodeAndMessage() {

        DuplicateOrderException exception =
                new DuplicateOrderException("6f2b1c2a-6a1e-4a4f-9c0d-2f7a1b3c4d5e");

        assertInstanceOf(DomainException.class, exception);
        assertEquals("ORD-409", exception.getErrorCode());
        assertEquals("Duplicate order", exception.getMessage());
        assertEquals("6f2b1c2a-6a1e-4a4f-9c0d-2f7a1b3c4d5e", exception.getIdempotencyKey());
    }

    @Test
    void insufficientHoldingsAndDuplicateOrderShareTheSameCodeButAreDistinctTypes() {

        InsufficientHoldingsException holdings =
                new InsufficientHoldingsException(1L, "ACME", 100L, 40L);
        DuplicateOrderException duplicate = new DuplicateOrderException("key-12345678");

        assertEquals(holdings.getErrorCode(), duplicate.getErrorCode());
        assertEquals("ORD-409", holdings.getErrorCode());
    }
}
