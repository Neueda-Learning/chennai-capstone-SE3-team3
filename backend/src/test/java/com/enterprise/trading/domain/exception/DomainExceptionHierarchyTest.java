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


}
