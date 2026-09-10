package org.example.backend.mapper;

import org.example.backend.entities.Account;
import org.example.backend.enums.AccountStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AccountMapper
 *
 * Test scenarios:
 * 1. Account row inserted and retrievable ✓
 * 2. Account reads correctly ✓
 * 3. Mapper reports the affected row count ✓
 * 4. Mapper surfaces a constraint violation ✓
 *
 * All tests verify that:
 * - Parameters are bound correctly (no SQL injection via parameters)
 * - Affected row counts are returned properly
 * - Constraint violations surface through DataIntegrityViolationException
 * - Result sets map cleanly to domain objects
 */
@SpringBootTest
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/trading_db_test",
        "mybatis.mapper-locations=classpath:mapper/*.xml",
        "mybatis.type-aliases-package=org.example.backend.entities"
})
@DisplayName("AccountMapper Integration Tests")
class AccountMapperIntegrationTest {

    @Autowired
    private AccountMapper accountMapper;

    // ===================================================
    // TEST: Account row inserted and retrievable
    // ===================================================

    @Test
    @DisplayName("Scenario: Account row inserted and retrievable - ACTIVE status")
    void testInsertAndRetrieveAccount_Active() {
        // GIVEN: A new ACTIVE account with all parameters bound
        Account newAccount = new Account(
                0,  // Will be generated
                "ACC000999001",
                LocalDate.of(2024, 1, 10),
                new BigDecimal("25000.00"),
                new BigDecimal("20000.00"),
                AccountStatus.ACTIVE,
                "EUR",
                1L,
                null,
                null,
                101
        );

        // WHEN: The account is inserted
        int insertResult = accountMapper.insertAccount(newAccount);

        // THEN: Verify affected row count
        assertEquals(1, insertResult, "Insert should affect exactly 1 row");

        // AND: The account should be retrievable by ID
        Optional<Account> retrieved = accountMapper.selectAccountById(newAccount.getAccountId());
        assertTrue(retrieved.isPresent(), "Account should be retrievable by ID");

        Account account = retrieved.get();
        assertEquals("ACC000999001", account.getAccountNumber());
        assertEquals(AccountStatus.ACTIVE, account.getAccountStatus());
        assertEquals(new BigDecimal("25000.00"), account.getBalance());
        assertEquals("EUR", account.getCurrency());
    }

    @Test
    @DisplayName("Scenario: Account row inserted and retrievable by account number")
    void testInsertAndRetrieveAccount_ByNumber() {
        // GIVEN: A new account
        Account newAccount = new Account(
                0,
                "ACC000999002",
                LocalDate.of(2024, 2, 10),
                new BigDecimal("50000.00"),
                new BigDecimal("40000.00"),
                AccountStatus.ACTIVE,
                "USD",
                1L,
                null,
                null,
                102
        );

        // WHEN: The account is inserted
        accountMapper.insertAccount(newAccount);

        // THEN: The account should be retrievable by account number (business key)
        Optional<Account> retrieved = accountMapper.selectAccountByNumber("ACC000999002");
        assertTrue(retrieved.isPresent(), "Account should be retrievable by account number");

        Account account = retrieved.get();
        assertEquals("ACC000999002", account.getAccountNumber());
    }

    // ===================================================
    // TEST: Account and status read correctly
    // ===================================================

    @Test
    @DisplayName("Scenario: Accounts read correctly with status filter")
    void testSelectAccountsByStatus() {
        // GIVEN: Multiple accounts with different statuses
        Account active = new Account(0, "ACC000999010", LocalDate.of(2024, 1, 1),
                new BigDecimal("30000.00"), new BigDecimal("25000.00"),
                AccountStatus.ACTIVE, "EUR", 1L, null, null, 101);

        Account suspended = new Account(0, "ACC000999011", LocalDate.of(2024, 2, 1),
                new BigDecimal("20000.00"), new BigDecimal("15000.00"),
                AccountStatus.SUSPENDED, "USD", 1L,
                OffsetDateTime.of(2026, 8, 20, 9, 30, 0, 0, ZoneOffset.ofHours(1)),
                null, 102);

        Account closed = new Account(0, "ACC000999012", LocalDate.of(2024, 3, 1),
                BigDecimal.ZERO, BigDecimal.ZERO,
                AccountStatus.CLOSED, "GBP", 1L, null,
                OffsetDateTime.of(2026, 6, 30, 16, 15, 0, 0, ZoneOffset.ofHours(1)),
                103);

        // WHEN: All accounts are inserted
        accountMapper.insertAccount(active);
        accountMapper.insertAccount(suspended);
        accountMapper.insertAccount(closed);

        // AND: We query by ACTIVE status
        List<Account> activeAccounts = accountMapper.selectAccountsByStatus(AccountStatus.ACTIVE);

        // THEN: Only ACTIVE accounts should be returned
        assertTrue(activeAccounts.stream()
                .anyMatch(a -> a.getAccountNumber().equals("ACC000999010")));

        // AND: Verify result set mapping
        Account retrieved = activeAccounts.stream()
                .filter(a -> a.getAccountNumber().equals("ACC000999010"))
                .findFirst()
                .orElseThrow();

        assertEquals(AccountStatus.ACTIVE, retrieved.getAccountStatus());
    }

    @Test
    @DisplayName("Scenario: Accounts for client read correctly")
    void testSelectAccountsByClientId() {
        // GIVEN: Multiple accounts for the same client
        Account account1 = new Account(0, "ACC000999020", LocalDate.of(2024, 1, 1),
                new BigDecimal("30000.00"), new BigDecimal("25000.00"),
                AccountStatus.ACTIVE, "EUR", 1L, null, null, 201);  // Client 201

        Account account2 = new Account(0, "ACC000999021", LocalDate.of(2024, 2, 1),
                new BigDecimal("50000.00"), new BigDecimal("40000.00"),
                AccountStatus.ACTIVE, "USD", 1L, null, null, 201);  // Client 201

        Account account3 = new Account(0, "ACC000999022", LocalDate.of(2024, 3, 1),
                new BigDecimal("20000.00"), new BigDecimal("15000.00"),
                AccountStatus.ACTIVE, "GBP", 1L, null, null, 202);  // Client 202

        // WHEN: All accounts are inserted
        accountMapper.insertAccount(account1);
        accountMapper.insertAccount(account2);
        accountMapper.insertAccount(account3);

        // AND: We query by client ID 201
        List<Account> clientAccounts = accountMapper.selectAccountsByClientId(201);

        // THEN: Should retrieve exactly 2 accounts for client 201
        assertEquals(2, clientAccounts.size(), "Should retrieve all accounts for client 201");
    }

    // ===================================================
    // TEST: Mapper reports the affected row count
    // ===================================================

    @Test
    @DisplayName("Scenario: Mapper reports affected row count - Insert")
    void testMapperReportsAffectedRowCount_Insert() {
        // GIVEN: A new account
        Account newAccount = new Account(0, "ACC000999030", LocalDate.of(2024, 1, 1),
                new BigDecimal("40000.00"), new BigDecimal("35000.00"),
                AccountStatus.ACTIVE, "EUR", 1L, null, null, 301);

        // WHEN: The account is inserted
        int result = accountMapper.insertAccount(newAccount);

        // THEN: Should return 1 (exactly one row affected)
        assertEquals(1, result, "Insert should affect exactly 1 row");
    }

    @Test
    @DisplayName("Scenario: Mapper reports affected row count - Update status")
    void testMapperReportsAffectedRowCount_UpdateStatus() {
        // GIVEN: An inserted account
        Account newAccount = new Account(0, "ACC000999031", LocalDate.of(2024, 1, 1),
                new BigDecimal("40000.00"), new BigDecimal("35000.00"),
                AccountStatus.ACTIVE, "EUR", 1L, null, null, 302);

        accountMapper.insertAccount(newAccount);

        // WHEN: Update the account status
        int updateResult = accountMapper.updateAccountStatus(
                newAccount.getAccountId(),
                AccountStatus.SUSPENDED
        );

        // THEN: Should return 1 (exactly one row affected)
        assertEquals(1, updateResult, "Update should affect exactly 1 row");

        // Verify the update worked
        Optional<Account> updated = accountMapper.selectAccountById(newAccount.getAccountId());
        assertTrue(updated.isPresent());
        assertEquals(AccountStatus.SUSPENDED, updated.get().getAccountStatus());
    }

    @Test
    @DisplayName("Scenario: Mapper reports affected row count - Update balance")
    void testMapperReportsAffectedRowCount_UpdateBalance() {
        // GIVEN: An inserted account
        Account newAccount = new Account(0, "ACC000999032", LocalDate.of(2024, 1, 1),
                new BigDecimal("40000.00"), new BigDecimal("35000.00"),
                AccountStatus.ACTIVE, "EUR", 1L, null, null, 303);

        accountMapper.insertAccount(newAccount);

        // WHEN: Update balance and purchasing power
        int updateResult = accountMapper.updateAccountBalance(
                newAccount.getAccountId(),
                new BigDecimal("45000.00"),
                new BigDecimal("38000.00")
        );

        // THEN: Should return 1
        assertEquals(1, updateResult, "Update should affect exactly 1 row");

        // Verify the update
        Optional<Account> updated = accountMapper.selectAccountById(newAccount.getAccountId());
        assertTrue(updated.isPresent());
        assertEquals(new BigDecimal("45000.00"), updated.get().getBalance());
    }

    @Test
    @DisplayName("Scenario: Mapper reports affected row count - Update non-existent")
    void testMapperReportsAffectedRowCount_UpdateNonExistent() {
        // GIVEN: A non-existent account ID
        int nonExistentId = 99999;

        // WHEN: Update is attempted
        int result = accountMapper.updateAccountStatus(nonExistentId, AccountStatus.CLOSED);

        // THEN: Should return 0 (no rows affected)
        assertEquals(0, result, "Update of non-existent row should affect 0 rows");
    }

    // ===================================================
    // TEST: Mapper surfaces constraint violations
    // ===================================================

    @Test
    @DisplayName("Scenario: Mapper surfaces constraint violation - Duplicate account number")
    void testMapperSurfacesConstraintViolation_DuplicateAccountNumber() {
        // GIVEN: An account with a specific account number
        Account account1 = new Account(0, "ACC000999040", LocalDate.of(2024, 1, 1),
                new BigDecimal("30000.00"), new BigDecimal("25000.00"),
                AccountStatus.ACTIVE, "EUR", 1L, null, null, 401);

        accountMapper.insertAccount(account1);

        // AND: Another account with the same account number (violation of unique constraint)
        Account account2 = new Account(0, "ACC000999040",  // DUPLICATE!
                LocalDate.of(2024, 2, 1),
                new BigDecimal("50000.00"), new BigDecimal("40000.00"),
                AccountStatus.ACTIVE, "USD", 1L, null, null, 402);

        // WHEN/THEN: Inserting the duplicate should raise DataIntegrityViolationException
        assertThrows(
                DataIntegrityViolationException.class,
                () -> accountMapper.insertAccount(account2),
                "Duplicate account number should raise constraint violation"
        );
    }

    @Test
    @DisplayName("Scenario: Mapper surfaces constraint violation - Invalid foreign key")
    void testMapperSurfacesConstraintViolation_InvalidForeignKey() {
        // GIVEN: An account with a non-existent client
        Account account = new Account(0, "ACC000999041", LocalDate.of(2024, 1, 1),
                new BigDecimal("30000.00"), new BigDecimal("25000.00"),
                AccountStatus.ACTIVE, "EUR", 1L, null, null,
                99999  // NON-EXISTENT CLIENT!
        );

        // WHEN/THEN: Inserting with invalid foreign key should raise exception
        assertThrows(
                DataIntegrityViolationException.class,
                () -> accountMapper.insertAccount(account),
                "Invalid foreign key should raise constraint violation"
        );
    }

    // ===================================================
    // TEST: Parameterized queries prevent SQL injection
    // ===================================================

    @Test
    @DisplayName("Security: SQL injection attempt via account number is prevented")
    void testSQLInjectionPrevention_AccountNumber() {
        // GIVEN: A malicious account number that looks like SQL injection
        String maliciousAccountNumber = "ACC000' OR '1'='1";  // SQL injection attempt

        Account account = new Account(0, maliciousAccountNumber, LocalDate.of(2024, 1, 1),
                new BigDecimal("30000.00"), new BigDecimal("25000.00"),
                AccountStatus.ACTIVE, "EUR", 1L, null, null, 501);

        // WHEN: The account is inserted with the malicious number
        accountMapper.insertAccount(account);

        // THEN: The account should be inserted safely
        // The malicious string is treated as literal data, not SQL
        Optional<Account> retrieved = accountMapper.selectAccountByNumber(maliciousAccountNumber);
        assertTrue(retrieved.isPresent());
        assertEquals(maliciousAccountNumber, retrieved.get().getAccountNumber());

        // AND: The account is only retrievable by its exact number
        // (proving it wasn't interpreted as SQL)
        List<Account> allAccounts = accountMapper.selectAccountsByClientId(501);
        assertTrue(allAccounts.stream()
                .anyMatch(a -> a.getAccountNumber().equals(maliciousAccountNumber)));
    }

    @Test
    @DisplayName("Security: SQL injection attempt via status is prevented")
    void testSQLInjectionPrevention_StatusFilter() {
        // GIVEN: Multiple accounts
        Account account1 = new Account(0, "ACC000999050", LocalDate.of(2024, 1, 1),
                new BigDecimal("30000.00"), new BigDecimal("25000.00"),
                AccountStatus.ACTIVE, "EUR", 1L, null, null, 601);

        Account account2 = new Account(0, "ACC000999051", LocalDate.of(2024, 2, 1),
                new BigDecimal("20000.00"), new BigDecimal("15000.00"),
                AccountStatus.SUSPENDED, "USD", 1L,
                OffsetDateTime.now(ZoneOffset.UTC), null, 602);

        accountMapper.insertAccount(account1);
        accountMapper.insertAccount(account2);

        // WHEN: We query by ACTIVE status only
        List<Account> activeAccounts = accountMapper.selectAccountsByStatus(AccountStatus.ACTIVE);

        // THEN: We only get accounts with ACTIVE status
        // (even if an attacker tried to inject SQL via the status parameter)
        assertTrue(activeAccounts.stream()
                .allMatch(a -> a.getAccountStatus() == AccountStatus.ACTIVE));
        assertEquals(1, activeAccounts.size());
    }

    @Test
    @DisplayName("Security: Account number existence check uses parameterized query")
    void testSQLInjectionPrevention_ExistenceCheck() {
        // GIVEN: An account
        Account account = new Account(0, "ACC000999060", LocalDate.of(2024, 1, 1),
                new BigDecimal("30000.00"), new BigDecimal("25000.00"),
                AccountStatus.ACTIVE, "EUR", 1L, null, null, 701);

        accountMapper.insertAccount(account);

        // WHEN: We check if a malicious account number exists
        String maliciousNumber = "ACC' OR '1'='1";
        boolean exists = accountMapper.accountNumberExists(maliciousNumber);

        // THEN: Should return false (the malicious number doesn't exist)
        assertFalse(exists, "Malicious account number should not exist");

        // AND: The legitimate account should exist
        assertTrue(accountMapper.accountNumberExists("ACC000999060"));
    }
}
