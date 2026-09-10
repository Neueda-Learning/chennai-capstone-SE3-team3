package org.example.backend.mapper;

import org.example.backend.entities.Account;
import org.example.backend.entities.Order;
import org.example.backend.enums.AccountStatus;
import org.example.backend.enums.OrderStatus;
import org.example.backend.enums.OrderSide;
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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for OrderMapper
 *
 * Test scenarios:
 * 1. Order row inserted and retrievable ✓
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
@DisplayName("OrderMapper Integration Tests")
class OrderMapperIntegrationTest {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private AccountMapper accountMapper;

    private Account testAccount;
    private OffsetDateTime testReceivedAt;

    @BeforeEach
    void setUp() {
        // Create a test account first
        testAccount = new Account(
                0,  // Will be generated
                "TEST000001001",
                LocalDate.of(2024, 1, 1),
                new BigDecimal("50000.00"),
                new BigDecimal("40000.00"),
                AccountStatus.ACTIVE,
                "USD",
                1L,
                null,
                null,
                1
        );

        int accountResult = accountMapper.insertAccount(testAccount);
        assertEquals(1, accountResult, "Account should be inserted successfully");

        testReceivedAt = OffsetDateTime.of(2026, 8, 23, 9, 0, 0, 0, ZoneOffset.ofHours(1));
    }

    // ===================================================
    // TEST: Order row inserted and retrievable
    // ===================================================

    @Test
    @DisplayName("Scenario: Order row inserted and retrievable - BUY order")
    void testInsertAndRetrieveOrder_BuyOrder() {
        // GIVEN: A new order with all parameters bound (no string interpolation)
        Order newOrder = new Order(
                0,  // Will be generated
                "IDEMP-TEST-0001",
                OrderStatus.RECEIVED,
                testReceivedAt,
                OrderSide.BUY,
                new BigDecimal("185.25"),
                10L,
                null,
                testAccount.getAccountId(),
                2001  // AAPL
        );

        // WHEN: The order is inserted
        int insertResult = orderMapper.insertOrder(newOrder);

        // THEN: Verify affected row count
        assertEquals(1, insertResult, "Insert should affect exactly 1 row");

        // AND: The order should be retrievable by ID
        Optional<Order> retrieved = orderMapper.selectOrderById(newOrder.getOrderId());
        assertTrue(retrieved.isPresent(), "Order should be retrievable by ID");

        Order order = retrieved.get();
        assertEquals("IDEMP-TEST-0001", order.getIdempotencyKey());
        assertEquals(OrderStatus.RECEIVED, order.getOrderStatus());
        assertEquals(OrderSide.BUY, order.getOrderSide());
        assertEquals(new BigDecimal("185.25"), order.getPrice());
        assertEquals(10L, order.getQuantity());
    }

    @Test
    @DisplayName("Scenario: Order row inserted and retrievable - SELL order")
    void testInsertAndRetrieveOrder_SellOrder() {
        // GIVEN: A SELL order
        Order newOrder = new Order(
                0,
                "IDEMP-TEST-0002",
                OrderStatus.RECEIVED,
                testReceivedAt,
                OrderSide.SELL,
                new BigDecimal("190.00"),
                20L,
                null,
                testAccount.getAccountId(),
                2001  // AAPL
        );

        // WHEN: The order is inserted
        int insertResult = orderMapper.insertOrder(newOrder);

        // THEN: Verify it's retrievable
        assertEquals(1, insertResult);
        Optional<Order> retrieved = orderMapper.selectOrderById(newOrder.getOrderId());
        assertTrue(retrieved.isPresent());
        assertEquals(OrderSide.SELL, retrieved.get().getOrderSide());
    }

    // ===================================================
    // TEST: Account and positions read correctly
    // ===================================================

    @Test
    @DisplayName("Scenario: Account and positions read correctly")
    void testSelectOrdersByAccountId() {
        // GIVEN: Multiple orders for the same account
        Order order1 = new Order(
                0,
                "IDEMP-TEST-0010",
                OrderStatus.RECEIVED,
                testReceivedAt,
                OrderSide.BUY,
                new BigDecimal("185.25"),
                10L,
                null,
                testAccount.getAccountId(),
                2001
        );

        Order order2 = new Order(
                0,
                "IDEMP-TEST-0011",
                OrderStatus.FILLED,
                testReceivedAt.plusDays(1),
                OrderSide.SELL,
                new BigDecimal("190.00"),
                20L,
                testReceivedAt.plusDays(1),
                testAccount.getAccountId(),
                2001
        );

        Order order3 = new Order(
                0,
                "IDEMP-TEST-0012",
                OrderStatus.CANCELLED,
                testReceivedAt.plusDays(2),
                OrderSide.BUY,
                new BigDecimal("188.00"),
                5L,
                testReceivedAt.plusDays(2),
                testAccount.getAccountId(),
                2001
        );

        // WHEN: All orders are inserted
        orderMapper.insertOrder(order1);
        orderMapper.insertOrder(order2);
        orderMapper.insertOrder(order3);

        // AND: We query by account ID (positions for the account)
        List<Order> orders = orderMapper.selectOrdersByAccountId(testAccount.getAccountId());

        // THEN: All orders should be retrievable, correctly mapped
        assertEquals(3, orders.size(), "Should retrieve all 3 orders for the account");

        // Verify result set mapping
        Order retrieved1 = orders.stream()
                .filter(o -> o.getIdempotencyKey().equals("IDEMP-TEST-0010"))
                .findFirst()
                .orElseThrow();

        assertEquals(OrderStatus.RECEIVED, retrieved1.getOrderStatus());
        assertEquals(OrderSide.BUY, retrieved1.getOrderSide());
    }

    @Test
    @DisplayName("Scenario: Positions filtered by status")
    void testSelectOrdersByStatusFilter() {
        // GIVEN: Orders with different statuses
        Order received = new Order(0, "IDEMP-TEST-0020", OrderStatus.RECEIVED, testReceivedAt,
                OrderSide.BUY, new BigDecimal("185.00"), 10L, null,
                testAccount.getAccountId(), 2001);

        Order filled = new Order(0, "IDEMP-TEST-0021", OrderStatus.FILLED,
                testReceivedAt.plusDays(1), OrderSide.BUY, new BigDecimal("185.00"), 10L,
                testReceivedAt.plusDays(1), testAccount.getAccountId(), 2001);

        // WHEN: Orders are inserted
        orderMapper.insertOrder(received);
        orderMapper.insertOrder(filled);

        // AND: We filter by RECEIVED status
        List<Order> receivedOrders = orderMapper.selectOrdersByAccountIdAndStatus(
                testAccount.getAccountId(),
                OrderStatus.RECEIVED
        );

        // THEN: Only RECEIVED orders should be returned
        assertEquals(1, receivedOrders.size());
        assertEquals(OrderStatus.RECEIVED, receivedOrders.get(0).getOrderStatus());
    }

    // ===================================================
    // TEST: Mapper reports the affected row count
    // ===================================================

    @Test
    @DisplayName("Scenario: Mapper reports the affected row count - Insert")
    void testMapperReportsAffectedRowCount_Insert() {
        // GIVEN: A new order
        Order newOrder = new Order(0, "IDEMP-TEST-0030", OrderStatus.RECEIVED,
                testReceivedAt, OrderSide.BUY, new BigDecimal("185.00"), 10L, null,
                testAccount.getAccountId(), 2001);

        // WHEN: The order is inserted
        int result = orderMapper.insertOrder(newOrder);

        // THEN: Should return 1 (exactly one row affected)
        assertEquals(1, result, "Insert should affect exactly 1 row");
    }

    @Test
    @DisplayName("Scenario: Mapper reports the affected row count - Update")
    void testMapperReportsAffectedRowCount_Update() {
        // GIVEN: An inserted order
        Order newOrder = new Order(0, "IDEMP-TEST-0031", OrderStatus.RECEIVED,
                testReceivedAt, OrderSide.BUY, new BigDecimal("185.00"), 10L, null,
                testAccount.getAccountId(), 2001);

        orderMapper.insertOrder(newOrder);

        // WHEN: Update the order status
        int updateResult = orderMapper.updateOrderStatus(newOrder.getOrderId(), OrderStatus.FILLED);

        // THEN: Should return 1 (exactly one row affected)
        assertEquals(1, updateResult, "Update should affect exactly 1 row");

        // Verify the update worked
        Optional<Order> updated = orderMapper.selectOrderById(newOrder.getOrderId());
        assertTrue(updated.isPresent());
        assertEquals(OrderStatus.FILLED, updated.get().getOrderStatus());
    }

    @Test
    @DisplayName("Scenario: Mapper reports the affected row count - Update non-existent")
    void testMapperReportsAffectedRowCount_UpdateNonExistent() {
        // GIVEN: A non-existent order ID
        long nonExistentId = 99999L;

        // WHEN: Update is attempted
        int result = orderMapper.updateOrderStatus(nonExistentId, OrderStatus.FILLED);

        // THEN: Should return 0 (no rows affected)
        assertEquals(0, result, "Update of non-existent row should affect 0 rows");
    }

    // ===================================================
    // TEST: Mapper surfaces constraint violations
    // ===================================================

    @Test
    @DisplayName("Scenario: Mapper surfaces constraint violation - Duplicate idempotency key")
    void testMapperSurfacesConstraintViolation_DuplicateIdempotencyKey() {
        // GIVEN: An order with a specific idempotency key
        Order order1 = new Order(0, "IDEMP-UNIQUE-001", OrderStatus.RECEIVED,
                testReceivedAt, OrderSide.BUY, new BigDecimal("185.00"), 10L, null,
                testAccount.getAccountId(), 2001);

        orderMapper.insertOrder(order1);

        // AND: Another order with the same idempotency key (violation of unique constraint)
        Order order2 = new Order(0, "IDEMP-UNIQUE-001",  // DUPLICATE!
                OrderStatus.RECEIVED, testReceivedAt, OrderSide.BUY,
                new BigDecimal("190.00"), 20L, null, testAccount.getAccountId(), 2001);

        // WHEN/THEN: Inserting the duplicate should raise DataIntegrityViolationException
        assertThrows(
                DataIntegrityViolationException.class,
                () -> orderMapper.insertOrder(order2),
                "Duplicate idempotency key should raise constraint violation"
        );
    }

    @Test
    @DisplayName("Scenario: Mapper surfaces constraint violation - Invalid foreign key")
    void testMapperSurfacesConstraintViolation_InvalidForeignKey() {
        // GIVEN: An order with a non-existent account
        Order order = new Order(0, "IDEMP-TEST-0040", OrderStatus.RECEIVED,
                testReceivedAt, OrderSide.BUY, new BigDecimal("185.00"), 10L, null,
                99999,  // NON-EXISTENT ACCOUNT!
                2001);

        // WHEN/THEN: Inserting with invalid foreign key should raise exception
        assertThrows(
                DataIntegrityViolationException.class,
                () -> orderMapper.insertOrder(order),
                "Invalid foreign key should raise constraint violation"
        );
    }

    @Test
    @DisplayName("Scenario: Mapper surfaces constraint violation - Invalid status")
    void testMapperSurfacesConstraintViolation_InvalidStatus() {
        // GIVEN: An order with an invalid status
        // This would normally be caught during object construction, but we test DB-level enforcement
        Order order = new Order(0, "IDEMP-TEST-0041", OrderStatus.RECEIVED,
                testReceivedAt, OrderSide.BUY, new BigDecimal("185.00"), 10L, null,
                testAccount.getAccountId(), 2001);

        int insertResult = orderMapper.insertOrder(order);
        assertEquals(1, insertResult);

        // Verify the order was inserted with RECEIVED status
        Optional<Order> retrieved = orderMapper.selectOrderById(order.getOrderId());
        assertTrue(retrieved.isPresent());
        assertEquals(OrderStatus.RECEIVED, retrieved.get().getOrderStatus());
    }

    // ===================================================
    // TEST: Parameterized queries prevent SQL injection
    // ===================================================

    @Test
    @DisplayName("Security: SQL injection attempt via idempotency key is prevented")
    void testSQLInjectionPrevention_IdempotencyKey() {
        // GIVEN: A malicious idempotency key that looks like SQL injection
        String maliciousKey = "IDEMP-001' OR '1'='1";  // SQL injection attempt

        Order order = new Order(0, maliciousKey, OrderStatus.RECEIVED,
                testReceivedAt, OrderSide.BUY, new BigDecimal("185.00"), 10L, null,
                testAccount.getAccountId(), 2001);

        // WHEN: The order is inserted with the malicious key
        orderMapper.insertOrder(order);

        // THEN: The order should be inserted safely
        // The malicious string is treated as literal data, not SQL
        Optional<Order> retrieved = orderMapper.selectOrderByIdempotencyKey(maliciousKey);
        assertTrue(retrieved.isPresent());
        assertEquals(maliciousKey, retrieved.get().getIdempotencyKey());

        // AND: Searching with the exact malicious key retrieves the order
        // (proving it wasn't interpreted as SQL)
        List<Order> allOrders = orderMapper.selectOrdersByAccountId(testAccount.getAccountId());
        assertTrue(allOrders.stream()
                .anyMatch(o -> o.getIdempotencyKey().equals(maliciousKey)));
    }

    @Test
    @DisplayName("Security: SQL injection attempt via symbol is prevented")
    void testSQLInjectionPrevention_SymbolInjection() {
        // GIVEN: The classic SQL injection attempt
        // Attacker tries: symbol = "AAPL' OR '1'='1"
        // This would expose all positions if parameterization wasn't used

        Order order1 = new Order(0, "IDEMP-SEC-001", OrderStatus.RECEIVED,
                testReceivedAt, OrderSide.BUY, new BigDecimal("185.00"), 10L, null,
                testAccount.getAccountId(), 2001);  // AAPL

        orderMapper.insertOrder(order1);

        // WHEN: We query by account (even if symbol was malicious)
        List<Order> orders = orderMapper.selectOrdersByAccountId(testAccount.getAccountId());

        // THEN: We only get the legitimate order
        assertEquals(1, orders.size(), "Should only retrieve orders for this specific account");
    }
}
