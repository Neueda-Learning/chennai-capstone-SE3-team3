package com.enterprise.trading.domain;

import com.enterprise.trading.domain.dto.PlaceOrderRequest;
import com.enterprise.trading.domain.enums.OrderSide;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class PlaceOrderRequestTest {

    @Test
    @DisplayName("accepts a valid place order request")
    void acceptsValidPlaceOrderRequest() {
        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                1L,
                "RELIANCE.NS",
                OrderSide.BUY,
                10,
                new BigDecimal("150.25"),
                "IDEMPOTENCY-KEY"
        );

        assertNotNull(placeOrderRequest);
    }

    @Test
    @DisplayName("rejects null accountId")
    void rejectsNullAccountId() {
        assertThrows(IllegalArgumentException.class, () -> new PlaceOrderRequest(
                null,
                "RELIANCE.NS",
                OrderSide.BUY,
                10,
                new BigDecimal("150.25"),
                "IDEMPOTENCY-KEY"
        ));
    }

    @Test
    @DisplayName("rejects accountId < 1")
    void rejectsAccountIdLessThan1() {
        assertThrows(IllegalArgumentException.class, () -> new PlaceOrderRequest(
                -1L,
                "RELIANCE.NS",
                OrderSide.BUY,
                10,
                new BigDecimal("150.25"),
                "IDEMPOTENCY-KEY"
        ));
    }

    @Test
    @DisplayName("accepts accountId >= 1")
    void acceptsAccountIdGreaterThanOrEqualTo1() {
        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                1L,
                "RELIANCE.NS",
                OrderSide.BUY,
                10,
                new BigDecimal("150.25"),
                "IDEMPOTENCY-KEY"
        );
        assertNotNull(placeOrderRequest);
    }

    @Test
    @DisplayName("rejects NULL symbol")
    void rejectsNullSymbol() {
        assertThrows(IllegalArgumentException.class, () -> new PlaceOrderRequest(
                1L,
                null,
                OrderSide.BUY,
                10,
                new BigDecimal("150.25"),
                "IDEMPOTENCY-KEY"
        ));
    }

    @Test
    @DisplayName("reject blank symbol")
    void rejectBlankSymbol() {
        assertThrows(IllegalArgumentException.class, () -> new PlaceOrderRequest(
                1L,
                " ",
                OrderSide.BUY,
                10,
                new BigDecimal("150.25"),
                "IDEMPOTENCY-KEY"
        ));
    }

    @Test
    @DisplayName("accept symbol with exactly 20 characters")
    void acceptsSymbolWithExactly20Characters() {
        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                1L,
                "a".repeat(20),
                OrderSide.BUY,
                10,
                new BigDecimal("150.25"),
                "IDEMPOTENCY-KEY"
        );
        assertNotNull(placeOrderRequest);
    }

    @Test
    @DisplayName("reject symbols longer than 20 characters")
    void rejectsSymbolsLongerThan20Characters() {
        assertThrows(IllegalArgumentException.class, () -> new PlaceOrderRequest(
                1L,
                "a".repeat(21),
                OrderSide.BUY,
                10,
                new BigDecimal("150.25"),
                "IDEMPOTENCY-KEY"
        ));
    }

    @Test
    @DisplayName("rejects NULL order side")
    void rejectsNullOrderSide() {
        assertThrows(IllegalArgumentException.class, () -> new PlaceOrderRequest(
                1L,
                "RELIANCE.NS",
                null,
                10,
                new BigDecimal("150.25"),
                "IDEMPOTENCY-KEY"
        ));
    }

    @Test
    @DisplayName("rejects NULL quantity")
    void rejectsNullQuantity() {
        assertThrows(IllegalArgumentException.class, () -> new PlaceOrderRequest(
                1L,
                "RELIANCE.NS",
                OrderSide.BUY,
                null,
                new BigDecimal("150.25"),
                "IDEMPOTENCY-KEY"
        ));
    }

    @Test
    @DisplayName("rejects 0 quantity")
    void rejectsZeroQuantity() {
        assertThrows(IllegalArgumentException.class, () -> new PlaceOrderRequest(
                1L,
                "RELIANCE.NS",
                OrderSide.BUY,
                0,
                new BigDecimal("150.25"),
                "IDEMPOTENCY-KEY"
        ));
    }

    @Test
    @DisplayName("rejects negative quantity")
    void rejectsNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () -> new PlaceOrderRequest(
                1L,
                "RELIANCE.NS",
                OrderSide.BUY,
                -1,
                new BigDecimal("150.25"),
                "IDEMPOTENCY-KEY"
        ));
    }

    @Test
    @DisplayName("accepts positive quantity")
    void acceptsPositiveQuantity() {
        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                1L,
                "RELIANCE.NS",
                OrderSide.BUY,
                1,
                new BigDecimal("150.25"),
                "IDEMPOTENCY-KEY"
        );
        assertNotNull(placeOrderRequest);
    }

    @Test
    @DisplayName("rejects NULL price")
    void rejectsNullPrice() {
        assertThrows(IllegalArgumentException.class, () -> new PlaceOrderRequest(
                1L,
                "RELIANCE.NS",
                OrderSide.BUY,
                10,
                null,
                "IDEMPOTENCY-KEY"
        ));
    }

    @Test
    @DisplayName("rejects 0 price")
    void rejectsZeroPrice() {
        assertThrows(IllegalArgumentException.class, () -> new PlaceOrderRequest(
                1L,
                "RELIANCE.NS",
                OrderSide.BUY,
                10,
                BigDecimal.ZERO,
                "IDEMPOTENCY-KEY"
        ));
    }

    @Test
    @DisplayName("rejects negative price")
    void rejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class, () -> new PlaceOrderRequest(
                1L,
                "RELIANCE.NS",
                OrderSide.BUY,
                10,
                new BigDecimal("-150.25"),
                "IDEMPOTENCY-KEY"
        ));
    }

    @Test
    @DisplayName("accepts price with 2 decimal places")
    void acceptsPriceWith2DecimalPlaces() {
        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                1L,
                "RELIANCE.NS",
                OrderSide.BUY,
                10,
                new BigDecimal("150.25"),
                "IDEMPOTENCY-KEY"
        );
        assertNotNull(placeOrderRequest);
    }

    @Test
    @DisplayName("rejects price with more than 2 decimal places")
    void rejectsPriceWithMoreThan2DecimalPlaces() {
        assertThrows(IllegalArgumentException.class, () -> new PlaceOrderRequest(
                1L,
                "RELIANCE.NS",
                OrderSide.BUY,
                10,
                new BigDecimal("150.251"),
                "IDEMPOTENCY-KEY"
        ));
    }

    @Test
    @DisplayName("rejects NULL idempotency key")
    void rejectsNullIdempotencyKey() {
        assertThrows(IllegalArgumentException.class, () -> new PlaceOrderRequest(
                1L,
                "RELIANCE.NS",
                OrderSide.BUY,
                10,
                new BigDecimal("150.25"),
                null
        ));
    }

    @Test
    @DisplayName("accepts idempotency key with exactly 8 characters")
    void acceptsIdempotencyKeyWithExactly8Characters() {
        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                1L,
                "RELIANCE.NS",
                OrderSide.BUY,
                10,
                new BigDecimal("150.25"),
                "IDEMPOTE"
        );
        assertNotNull(placeOrderRequest);
    }

    @Test
    @DisplayName("rejects idempotency key shorter than 8 characters")
    void rejectsIdempotencyKeyShorterThan8Characters() {
        assertThrows(IllegalArgumentException.class, () -> new PlaceOrderRequest(
                1L,
                "RELIANCE.NS",
                OrderSide.BUY,
                10,
                new BigDecimal("150.25"),
                "IDEMPOT"
        ));
    }

    @Test
    @DisplayName("accepts idempotency key with exactly 100 characters")
    void acceptsIdempotencyKeyWithExactly100Characters() {
        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                1L,
                "RELIANCE.NS",
                OrderSide.BUY,
                10,
                new BigDecimal("150.25"),
                "a".repeat(100)
        );
        assertNotNull(placeOrderRequest);
    }

    @Test
    @DisplayName("rejects idempotency key longer than 100 characters")
    void rejectsIdempotencyKeyLongerThan100Characters() {
        assertThrows(IllegalArgumentException.class, () -> new PlaceOrderRequest(
                1L,
                "RELIANCE.NS",
                OrderSide.BUY,
                10,
                new BigDecimal("150.25"),
                "a".repeat(101)
        ));
    }
}
