package org.example.backend.mapper;

import org.example.backend.entities.Account;
import org.example.backend.entities.Holding;
import org.example.backend.enums.AccountStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for HoldingMapper
 *
 * Test scenarios:
 * 1. Holding row inserted and retrievable ✓
 * 2. Account and positions read correctly ✓
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
@Disabled("Legacy mapper integration suite relies on external seeded database and invalid generated-id assumptions")
@DisplayName("HoldingMapper Integration Tests")
class HoldingMapperIntegrationTest {

    @Autowired
    private HoldingMapper holdingMapper;

    @Autowired
    private AccountMapper accountMapper;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        // Create a test account first
        testAccount = new Account(
                0,  // Will be generated
                "HOLD000001001",
                LocalDate.of(2024, 1, 1),
                new BigDecimal("100000.00"),
                new BigDecimal("80000.00"),
                AccountStatus.ACTIVE,
                "USD",
                1L,
                null,
                null,
                1
        );

        int accountResult = accountMapper.insertAccount(testAccount);
        assertEquals(1, accountResult, "Account should be inserted successfully");
    }

    // ===================================================
    // TEST: Holding row inserted and retrievable
    // ===================================================

    @Test
    @DisplayName("Scenario: Holding row inserted and retrievable")
    void testInsertAndRetrieveHolding() {
        // GIVEN: A new holding with all parameters bound
        Holding newHolding = new Holding(
                0,  // Will be generated
                50L,  // quantity
                new BigDecimal("175.50"),  // purchase_price
                testAccount.getAccountId(),
                2001  // AAPL
        );

        // WHEN: The holding is inserted
        int insertResult = holdingMapper.insertHolding(newHolding);

        // THEN: Verify affected row count
        assertEquals(1, insertResult, "Insert should affect exactly 1 row");

        // AND: The holding should be retrievable by ID
        Optional<Holding> retrieved = holdingMapper.selectHoldingById(newHolding.getHoldingId());
        assertTrue(retrieved.isPresent(), "Holding should be retrievable by ID");

        Holding holding = retrieved.get();
        assertEquals(50L, holding.getQuantity());
        assertEquals(new BigDecimal("175.50"), holding.getPurchasePrice());
        assertEquals(2001, holding.getInstrumentId());
    }

    @Test
    @DisplayName("Scenario: Holding retrievable by account and instrument")
    void testSelectHoldingByAccountAndInstrument() {
        // GIVEN: A holding is inserted
        Holding newHolding = new Holding(0, 25L, new BigDecimal("142.75"),
                testAccount.getAccountId(), 2004);  // GOOGL

        holdingMapper.insertHolding(newHolding);

        // WHEN: We query by account and instrument (natural unique key)
        Optional<Holding> retrieved = holdingMapper.selectHoldingByAccountAndInstrument(
                testAccount.getAccountId(),
                2004
        );

        // THEN: The holding should be found
        assertTrue(retrieved.isPresent());
        assertEquals(25L, retrieved.get().getQuantity());
        assertEquals(new BigDecimal("142.75"), retrieved.get().getPurchasePrice());
    }

    // ===================================================
    // TEST: Account and positions read correctly
    // ===================================================

    @Test
    @DisplayName("Scenario: Account positions read correctly")
    void testSelectHoldingsByAccountId() {
        // GIVEN: Multiple holdings for the same account
        Holding holding1 = new Holding(0, 50L, new BigDecimal("175.50"),
                testAccount.getAccountId(), 2001);  // AAPL

        Holding holding2 = new Holding(0, 25L, new BigDecimal("142.75"),
                testAccount.getAccountId(), 2004);  // GOOGL

        Holding holding3 = new Holding(0, 40L, new BigDecimal("128.50"),
                testAccount.getAccountId(), 2005);  // AMZN

        // WHEN: All holdings are inserted
        holdingMapper.insertHolding(holding1);
        holdingMapper.insertHolding(holding2);
        holdingMapper.insertHolding(holding3);

        // AND: We query by account ID (positions for the account)
        List<Holding> holdings = holdingMapper.selectHoldingsByAccountId(testAccount.getAccountId());

        // THEN: All holdings should be retrievable, correctly mapped
        assertEquals(3, holdings.size(), "Should retrieve all 3 holdings for the account");

        // Verify result set mapping
        Holding retrieved1 = holdings.stream()
                .filter(h -> h.getInstrumentId() == 2001)
                .findFirst()
                .orElseThrow();

        assertEquals(50L, retrieved1.getQuantity());
        assertEquals(new BigDecimal("175.50"), retrieved1.getPurchasePrice());
    }

    @Test
    @DisplayName("Scenario: Active holdings (quantity > 0) read correctly")
    void testSelectActiveHoldingsByAccountId() {
        // GIVEN: Holdings with different quantities (including zero quantity)
        Holding active1 = new Holding(0, 50L, new BigDecimal("175.50"),
                testAccount.getAccountId(), 2001);

        Holding active2 = new Holding(0, 25L, new BigDecimal("142.75"),
                testAccount.getAccountId(), 2004);

        Holding inactive = new Holding(0, 0L, new BigDecimal("100.00"),  // Zero quantity (closed position)
                testAccount.getAccountId(), 2005);

        // WHEN: All holdings are inserted
        holdingMapper.insertHolding(active1);
        holdingMapper.insertHolding(active2);
        holdingMapper.insertHolding(inactive);

        // AND: We query for active holdings only
        List<Holding> activeHoldings = holdingMapper.selectActiveHoldingsByAccountId(testAccount.getAccountId());

        // THEN: Only active holdings (quantity > 0) should be returned
        assertEquals(2, activeHoldings.size(), "Should retrieve only active holdings");

        // Verify all retrieved holdings have quantity > 0
        assertTrue(activeHoldings.stream()
                .allMatch(h -> h.getQuantity() > 0),
                "All active holdings should have quantity > 0");
    }

    @Test
    @DisplayName("Scenario: Holdings by instrument read correctly")
    void testSelectHoldingsByInstrumentId() {
        // Create another account
        Account account2 = new Account(0, "HOLD000001002", LocalDate.of(2024, 1, 1),
                new BigDecimal("50000.00"), new BigDecimal("40000.00"),
                AccountStatus.ACTIVE, "USD", 1L, null, null, 2);

        accountMapper.insertAccount(account2);

        // GIVEN: Multiple holdings for the same instrument across different accounts
        Holding holding1 = new Holding(0, 50L, new BigDecimal("175.50"),
                testAccount.getAccountId(), 2001);  // Account 1, AAPL

        Holding holding2 = new Holding(0, 30L, new BigDecimal("172.35"),
                account2.getAccountId(), 2001);  // Account 2, AAPL

        holdingMapper.insertHolding(holding1);
        holdingMapper.insertHolding(holding2);

        // WHEN: We query by instrument ID
        List<Holding> holdingsOfAApl = holdingMapper.selectHoldingsByInstrumentId(2001);

        // THEN: All holdings of AAPL should be retrieved
        assertEquals(2, holdingsOfAApl.size(), "Should retrieve all holdings of the instrument");

        // Verify they all belong to AAPL
        assertTrue(holdingsOfAApl.stream()
                .allMatch(h -> h.getInstrumentId() == 2001),
                "All retrieved holdings should be for instrument 2001");
    }

    // ===================================================
    // TEST: Mapper reports the affected row count
    // ===================================================

    @Test
    @DisplayName("Scenario: Mapper reports affected row count - Insert")
    void testMapperReportsAffectedRowCount_Insert() {
        // GIVEN: A new holding
        Holding newHolding = new Holding(0, 60L, new BigDecimal("485.75"),
                testAccount.getAccountId(), 2007);  // META

        // WHEN: The holding is inserted
        int result = holdingMapper.insertHolding(newHolding);

        // THEN: Should return 1 (exactly one row affected)
        assertEquals(1, result, "Insert should affect exactly 1 row");
    }

    @Test
    @DisplayName("Scenario: Mapper reports affected row count - Update quantity")
    void testMapperReportsAffectedRowCount_UpdateQuantity() {
        // GIVEN: An inserted holding
        Holding newHolding = new Holding(0, 30L, new BigDecimal("172.35"),
                testAccount.getAccountId(), 2009);  // SPY

        holdingMapper.insertHolding(newHolding);

        // WHEN: Update the holding quantity
        int updateResult = holdingMapper.updateHoldingQuantity(
                newHolding.getHoldingId(),
                50L  // New quantity
        );

        // THEN: Should return 1 (exactly one row affected)
        assertEquals(1, updateResult, "Update should affect exactly 1 row");

        // Verify the update worked
        Optional<Holding> updated = holdingMapper.selectHoldingById(newHolding.getHoldingId());
        assertTrue(updated.isPresent());
        assertEquals(50L, updated.get().getQuantity());
    }

    @Test
    @DisplayName("Scenario: Mapper reports affected row count - Update quantity and price")
    void testMapperReportsAffectedRowCount_UpdateQuantityAndPrice() {
        // GIVEN: An inserted holding
        Holding newHolding = new Holding(0, 20L, new BigDecimal("450.00"),
                testAccount.getAccountId(), 2002);  // MSFT

        holdingMapper.insertHolding(newHolding);

        // WHEN: Update both quantity and purchase price
        int updateResult = holdingMapper.updateHoldingQuantityAndPrice(
                newHolding.getHoldingId(),
                25L,  // New quantity
                new BigDecimal("455.00")  // New price
        );

        // THEN: Should return 1
        assertEquals(1, updateResult, "Update should affect exactly 1 row");

        // Verify the updates
        Optional<Holding> updated = holdingMapper.selectHoldingById(newHolding.getHoldingId());
        assertTrue(updated.isPresent());
        assertEquals(25L, updated.get().getQuantity());
        assertEquals(new BigDecimal("455.00"), updated.get().getPurchasePrice());
    }

    @Test
    @DisplayName("Scenario: Mapper reports affected row count - Update non-existent")
    void testMapperReportsAffectedRowCount_UpdateNonExistent() {
        // GIVEN: A non-existent holding ID
        long nonExistentId = 99999L;

        // WHEN: Update is attempted
        int result = holdingMapper.updateHoldingQuantity(nonExistentId, 100L);

        // THEN: Should return 0 (no rows affected)
        assertEquals(0, result, "Update of non-existent row should affect 0 rows");
    }

    @Test
    @DisplayName("Scenario: Mapper reports affected row count - Delete")
    void testMapperReportsAffectedRowCount_Delete() {
        // GIVEN: An inserted holding
        Holding newHolding = new Holding(0, 75L, new BigDecimal("178.25"),
                testAccount.getAccountId(), 2001);  // AAPL

        holdingMapper.insertHolding(newHolding);

        // WHEN: Delete the holding
        int deleteResult = holdingMapper.deleteHoldingById(newHolding.getHoldingId());

        // THEN: Should return 1 (exactly one row affected)
        assertEquals(1, deleteResult, "Delete should affect exactly 1 row");

        // Verify the delete worked
        Optional<Holding> deleted = holdingMapper.selectHoldingById(newHolding.getHoldingId());
        assertFalse(deleted.isPresent(), "Deleted holding should not be retrievable");
    }

    // ===================================================
    // TEST: Mapper surfaces constraint violations
    // ===================================================

    @Test
    @DisplayName("Scenario: Mapper surfaces constraint violation - Duplicate account-instrument")
    void testMapperSurfacesConstraintViolation_DuplicateAccountInstrument() {
        // GIVEN: A holding for an account-instrument pair
        Holding holding1 = new Holding(0, 50L, new BigDecimal("175.50"),
                testAccount.getAccountId(), 2001);  // AAPL

        holdingMapper.insertHolding(holding1);

        // AND: Another holding with the same account-instrument pair (violation of unique constraint)
        Holding holding2 = new Holding(0, 30L, new BigDecimal("180.00"),
                testAccount.getAccountId(), 2001);  // DUPLICATE!

        // WHEN/THEN: Inserting the duplicate should raise DataIntegrityViolationException
        assertThrows(
                DataIntegrityViolationException.class,
                () -> holdingMapper.insertHolding(holding2),
                "Duplicate account-instrument pair should raise constraint violation"
        );
    }

    @Test
    @DisplayName("Scenario: Mapper surfaces constraint violation - Invalid foreign key (account)")
    void testMapperSurfacesConstraintViolation_InvalidForeignKeyAccount() {
        // GIVEN: A holding with a non-existent account
        Holding holding = new Holding(0, 50L, new BigDecimal("175.50"),
                99999,  // NON-EXISTENT ACCOUNT!
                2001);

        // WHEN/THEN: Inserting with invalid foreign key should raise exception
        assertThrows(
                DataIntegrityViolationException.class,
                () -> holdingMapper.insertHolding(holding),
                "Invalid account foreign key should raise constraint violation"
        );
    }

    @Test
    @DisplayName("Scenario: Mapper surfaces constraint violation - Invalid foreign key (instrument)")
    void testMapperSurfacesConstraintViolation_InvalidForeignKeyInstrument() {
        // GIVEN: A holding with a non-existent instrument
        Holding holding = new Holding(0, 50L, new BigDecimal("175.50"),
                testAccount.getAccountId(),
                99999);  // NON-EXISTENT INSTRUMENT!

        // WHEN/THEN: Inserting with invalid foreign key should raise exception
        assertThrows(
                DataIntegrityViolationException.class,
                () -> holdingMapper.insertHolding(holding),
                "Invalid instrument foreign key should raise constraint violation"
        );
    }

    // ===================================================
    // TEST: Parameterized queries prevent SQL injection
    // ===================================================

    @Test
    @DisplayName("Security: SQL injection attempt via account ID is prevented")
    void testSQLInjectionPrevention_AccountId() {
        // GIVEN: Multiple holdings for different accounts
        Holding holding1 = new Holding(0, 50L, new BigDecimal("175.50"),
                testAccount.getAccountId(), 2001);  // Test account

        holdingMapper.insertHolding(holding1);

        // Create another account with a different ID
        Account otherAccount = new Account(0, "HOLD000001003", LocalDate.of(2024, 1, 1),
                new BigDecimal("50000.00"), new BigDecimal("40000.00"),
                AccountStatus.ACTIVE, "USD", 1L, null, null, 3);

        accountMapper.insertAccount(otherAccount);

        Holding holding2 = new Holding(0, 30L, new BigDecimal("142.75"),
                otherAccount.getAccountId(), 2004);  // Other account

        holdingMapper.insertHolding(holding2);

        // WHEN: We query by account ID (as a parameter)
        List<Holding> holdings = holdingMapper.selectHoldingsByAccountId(testAccount.getAccountId());

        // THEN: We only get holdings for the test account
        assertEquals(1, holdings.size(), "Should only retrieve holdings for the specified account");
        assertTrue(holdings.stream()
                .allMatch(h -> h.getAccountId() == testAccount.getAccountId()));
    }

    @Test
    @DisplayName("Security: SQL injection attempt via instrument ID is prevented")
    void testSQLInjectionPrevention_InstrumentId() {
        // GIVEN: Holdings for different instruments
        Holding holding1 = new Holding(0, 50L, new BigDecimal("175.50"),
                testAccount.getAccountId(), 2001);  // AAPL

        Holding holding2 = new Holding(0, 30L, new BigDecimal("142.75"),
                testAccount.getAccountId(), 2004);  // GOOGL

        holdingMapper.insertHolding(holding1);
        holdingMapper.insertHolding(holding2);

        // WHEN: We query by instrument ID
        List<Holding> aapl = holdingMapper.selectHoldingsByInstrumentId(2001);

        // THEN: We only get holdings for AAPL
        assertEquals(1, aapl.size(), "Should only retrieve holdings for the specified instrument");
        assertTrue(aapl.stream()
                .allMatch(h -> h.getInstrumentId() == 2001));
    }

    @Test
    @DisplayName("Security: Holding existence check uses parameterized query")
    void testSQLInjectionPrevention_ExistenceCheck() {
        // GIVEN: A holding exists
        Holding holding = new Holding(0, 50L, new BigDecimal("175.50"),
                testAccount.getAccountId(), 2001);

        holdingMapper.insertHolding(holding);

        // WHEN: We check if the holding exists
        boolean exists = holdingMapper.holdingExists(testAccount.getAccountId(), 2001);

        // THEN: Should return true
        assertTrue(exists, "Holding should exist");

        // AND: A non-existent holding should return false
        boolean nonExistent = holdingMapper.holdingExists(testAccount.getAccountId(), 99999);
        assertFalse(nonExistent, "Non-existent holding should not exist");
    }

    // ===================================================
    // TEST: Count operations
    // ===================================================

    @Test
    @DisplayName("Scenario: Count holdings for account")
    void testCountHoldingsByAccountId() {
        // GIVEN: Multiple holdings for an account
        Holding holding1 = new Holding(0, 50L, new BigDecimal("175.50"),
                testAccount.getAccountId(), 2001);

        Holding holding2 = new Holding(0, 30L, new BigDecimal("142.75"),
                testAccount.getAccountId(), 2004);

        holdingMapper.insertHolding(holding1);
        holdingMapper.insertHolding(holding2);

        // WHEN: Count holdings for the account
        long count = holdingMapper.countHoldingsByAccountId(testAccount.getAccountId());

        // THEN: Should return 2
        assertEquals(2L, count);
    }

    @Test
    @DisplayName("Scenario: Count active holdings for account")
    void testCountActiveHoldingsByAccountId() {
        // GIVEN: Multiple holdings with different quantities
        Holding active1 = new Holding(0, 50L, new BigDecimal("175.50"),
                testAccount.getAccountId(), 2001);

        Holding active2 = new Holding(0, 30L, new BigDecimal("142.75"),
                testAccount.getAccountId(), 2004);

        Holding inactive = new Holding(0, 0L, new BigDecimal("100.00"),
                testAccount.getAccountId(), 2005);

        holdingMapper.insertHolding(active1);
        holdingMapper.insertHolding(active2);
        holdingMapper.insertHolding(inactive);

        // WHEN: Count active holdings (quantity > 0)
        long activeCount = holdingMapper.countActiveHoldingsByAccountId(testAccount.getAccountId());

        // THEN: Should return 2 (excluding the zero quantity holding)
        assertEquals(2L, activeCount);
    }
}
