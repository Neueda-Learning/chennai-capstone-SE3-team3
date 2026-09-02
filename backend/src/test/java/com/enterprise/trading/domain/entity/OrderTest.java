package com.enterprise.trading.domain.entity;

import com.enterprise.trading.domain.enums.OrderSide;
import com.enterprise.trading.domain.enums.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OrderTest {

    private Order createOrder(OrderStatus orderStatus) {
        return new Order(
                1L,
                "IDEMPOTENCY-KEY",
                orderStatus,
                OffsetDateTime.now(),
                OrderSide.BUY,
                new BigDecimal("100.00"),
                100L,
                OffsetDateTime.now(),
                1,
                1
        );
    }

    @Test
    @DisplayName("newly received order has RECEIVED status")
    void newlyReceivedOrderHasRECEIVEDSTATUS() {
        Order order = createOrder(OrderStatus.RECEIVED);

        assertEquals(OrderStatus.RECEIVED, order.getOrderStatus());
    }

    @Test
    @DisplayName("received order can be filled")
    void receivedOrderCanBeFilled() {
        Order order = createOrder(OrderStatus.RECEIVED);
        order.fill();
        assertEquals(OrderStatus.FILLED, order.getOrderStatus());
    }

    @Test
    @DisplayName("received order can be rejected")
    void receivedOrderCanBeRejected() {
        Order order = createOrder(OrderStatus.RECEIVED);
        order.reject();
        assertEquals(OrderStatus.REJECTED, order.getOrderStatus());
    }

    @Test
    @DisplayName("received order can be cancelled")
    void receivedOrderCanBeCancelled() {
        Order order = createOrder(OrderStatus.RECEIVED);
        order.cancel();
        assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
    }

    @Test
    @DisplayName("filled order can't be cancelled")
    void filledOrderCanNotBeCancelled() {
        Order order = createOrder(OrderStatus.FILLED);
        assertThrows(IllegalStateException.class, order::cancel);
    }

    @Test
    @DisplayName("rejected order can't be filled")
    void rejectedOrderCanNotBeFilled() {
        Order order = createOrder(OrderStatus.REJECTED);
        assertThrows(IllegalStateException.class, order::fill);
    }

    @Test
    @DisplayName("cancelled order can't be filled")
    void cancelledOrderCanNotBeFilled() {
        Order order = createOrder(OrderStatus.CANCELLED);
        assertThrows(IllegalStateException.class, order::fill);
    }
}
