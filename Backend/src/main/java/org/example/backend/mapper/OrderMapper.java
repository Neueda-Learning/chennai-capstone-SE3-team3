package org.example.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.backend.entities.Order;
import org.example.backend.enums.OrderStatus;
import org.example.backend.enums.OrderSide;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * MyBatis mapper for Order entity.
 * All parameters are bound as JDBC bind parameters for security.
 * No string interpolation is allowed in SQL queries.
 * This prevents SQL injection attacks such as symbol-based injection (e.g., "AAPL' OR '1'='1").
 */
@Mapper
public interface OrderMapper {

    /**
     * Insert a new order.
     * All values are bound as parameters.
     * This includes the idempotency key, which serves as the database-level idempotency mechanism.
     *
     * @param order the order to insert
     * @return the number of rows affected (1 on success)
     * @throws org.springframework.dao.DataIntegrityViolationException if constraint violated
     */
    int insertOrder(@Param("order") Order order);

    /**
     * Select order by order ID.
     * Order ID is bound as a parameter.
     *
     * @param orderId the order ID to search for
     * @return Optional containing the order if found
     */
    Optional<Order> selectOrderById(@Param("orderId") long orderId);

    /**
     * Select order by idempotency key.
     * The idempotency key is bound as a parameter, never interpolated.
     *
     * @param idempotencyKey the idempotency key to search for
     * @return Optional containing the order if found
     */
    Optional<Order> selectOrderByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);

    /**
     * Select all orders for a specific account.
     * Account ID is bound as a parameter.
     *
     * @param accountId the account ID to filter by
     * @return list of orders for the account
     */
    List<Order> selectOrdersByAccountId(@Param("accountId") int accountId);

    /**
     * Select orders for an account with a specific status.
     * Both account ID and status are bound as parameters.
     *
     * @param accountId the account ID to filter by
     * @param orderStatus the status to filter by (RECEIVED, FILLED, CANCELLED, REJECTED)
     * @return list of matching orders
     */
    List<Order> selectOrdersByAccountIdAndStatus(
            @Param("accountId") int accountId,
            @Param("orderStatus") OrderStatus orderStatus);

    /**
     * Select orders by status filter.
     * Status is bound as a parameter.
     *
     * @param orderStatus the status to filter by
     * @return list of orders with the specified status
     */
    List<Order> selectOrdersByStatus(@Param("orderStatus") OrderStatus orderStatus);

    /**
     * Select orders by instrument (symbol).
     * Instrument ID is bound as a parameter (not the symbol string itself).
     *
     * @param instrumentId the instrument ID to filter by
     * @return list of orders for the instrument
     */
    List<Order> selectOrdersByInstrumentId(@Param("instrumentId") int instrumentId);

    /**
     * Select orders within a time range.
     * Timestamps are bound as parameters.
     *
     * @param fromDate the start of the range (inclusive)
     * @param toDate the end of the range (inclusive)
     * @return list of orders received within the range
     */
    List<Order> selectOrdersByReceivedDateRange(
            @Param("fromDate") OffsetDateTime fromDate,
            @Param("toDate") OffsetDateTime toDate);

    /**
     * Update order status.
     * Order ID and new status are bound as parameters.
     *
     * @param orderId the order to update
     * @param newStatus the new status
     * @return the number of rows affected
     * @throws org.springframework.dao.DataIntegrityViolationException if order is terminal
     */
    int updateOrderStatus(
            @Param("orderId") long orderId,
            @Param("newStatus") OrderStatus newStatus);

    /**
     * Update order status and transaction date.
     * All values are bound as parameters.
     *
     * @param orderId the order to update
     * @param newStatus the new status
     * @param transactionDate the transaction date
     * @return the number of rows affected
     * @throws org.springframework.dao.DataIntegrityViolationException if constraints violated
     */
    int updateOrderStatusAndDate(
            @Param("orderId") long orderId,
            @Param("newStatus") OrderStatus newStatus,
            @Param("transactionDate") OffsetDateTime transactionDate);

    /**
     * Count orders for an account.
     * Account ID is bound as a parameter.
     *
     * @param accountId the account ID
     * @return the number of orders for the account
     */
    long countOrdersByAccountId(@Param("accountId") int accountId);

    /**
     * Count orders by status.
     * Status is bound as a parameter.
     *
     * @param orderStatus the status to count
     * @return the number of orders with the specified status
     */
    long countOrdersByStatus(@Param("orderStatus") OrderStatus orderStatus);

    /**
     * Select recent orders for an account.
     * Account ID and limit are bound as parameters.
     *
     * @param accountId the account ID
     * @param limit the maximum number of orders to return
     * @return list of recent orders
     */
    List<Order> selectRecentOrdersByAccountId(
            @Param("accountId") int accountId,
            @Param("limit") int limit);

    /**
     * Select orders with order side filter.
     * Both account ID and order side are bound as parameters.
     *
     * @param accountId the account ID
     * @param orderSide the order side (BUY or SELL)
     * @return list of orders with the specified side
     */
    List<Order> selectOrdersByAccountIdAndSide(
            @Param("accountId") int accountId,
            @Param("orderSide") OrderSide orderSide);
}
