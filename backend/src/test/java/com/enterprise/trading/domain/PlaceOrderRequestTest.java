package com.enterprise.trading.domain;

import com.enterprise.trading.domain.dto.PlaceOrderRequest;
import com.enterprise.trading.domain.enums.OrderSide;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlaceOrderRequestTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setup() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void teardown() {
        validatorFactory.close();
    }

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

        assertTrue(validator.validate(placeOrderRequest).isEmpty());
    }

    @Test
    @DisplayName("rejects accountId < 1")
    void rejectsAccountIdLessThan1() {
        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                0L,
                "RELIANCE.NS",
                OrderSide.BUY,
                10,
                new BigDecimal("150.25"),
                "IDEMPOTENCY-KEY"
        );

        assertFalse(validator.validate(placeOrderRequest).isEmpty());
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
        assertTrue(validator.validate(placeOrderRequest).isEmpty());
    }

    @Test
    @DisplayName("reject blank symbol")
    void rejectBlankSymbol() {
        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                1L,
                "   ",
                OrderSide.BUY,
                10,
                new BigDecimal("150.25"),
                "IDEMPOTENCY-KEY"
        );
        assertFalse(validator.validate(placeOrderRequest).isEmpty());
    }

    @Test
    @DisplayName("accept symbol with exactly 20 characters")
    void acceptsSymbolWithExactly20Characters() {
        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                1L,
                "ABCDEFGHIJKLMNOPQRST",
                OrderSide.BUY,
                10,
                new BigDecimal("150.25"),
                "IDEMPOTENCY-KEY"
        );
        assertTrue(validator.validate(placeOrderRequest).isEmpty());
    }

    @Test
    @DisplayName("reject symbols longer than 20 characters")
    void rejectsSymbolsLongerThan20Characters() {
        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                1L,
                "RELIANCE.NS",
                OrderSide.BUY,
                10,
                new BigDecimal("150.25"),
                "IDEMPOTENCY-KEY"
        );
        assertFalse(validator.validate(placeOrderRequest).isEmpty());
    }

    @Test
    @DisplayName("requires an order side")
    void requiresAnOrderSide() {
        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                1L,
                "RELIANCE.NS",
                null,
                10,
                new BigDecimal("150.25"),
                "IDEMPOTENCY-KEY"
        );
        assertFalse(validator.validate(placeOrderRequest).isEmpty());
    }

    @Test
    @DisplayName("rejects 0 quantity")
    void rejectsZeroQuantity() {
        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                1L,
                "RELIANCE.NS",
                OrderSide.BUY,
                0,
                new BigDecimal("150.25"),
                "IDEMPOTENCY-KEY"
        );
        assertFalse(validator.validate(placeOrderRequest).isEmpty());
    }

    @Test
    @DisplayName("rejects negative quantity")
    void rejectsNegativeQuantity() {
        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                1L,
                "RELIANCE.NS",
                OrderSide.BUY,
                -1,
                new BigDecimal("150.25"),
                "IDEMPOTENCY-KEY"
        );
        assertFalse(validator.validate(placeOrderRequest).isEmpty());
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
        assertTrue(validator.validate(placeOrderRequest).isEmpty());
    }

    @Test
    @DisplayName("rejects 0 price")
    void rejectsZeroPrice() {
        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                1L,
                "RELIANCE.NS",
                OrderSide.BUY,
                10,
                new BigDecimal("150.25"),
                "IDEMPOTENCY-KEY"
        );
        assertFalse(validator.validate(placeOrderRequest).isEmpty());
    }

    @Test
    @DisplayName("rejects negative price")
    void rejectsNegativePrice() {
        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                1L,
                "RELIANCE.NS",
                OrderSide.BUY,
                10,
                new BigDecimal("150.25"),
                "IDEMPOTENCY-KEY"
        );
        assertFalse(validator.validate(placeOrderRequest).isEmpty());
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
        assertTrue(validator.validate(placeOrderRequest).isEmpty());
    }

    @Test
    @DisplayName("rejects price with more than 2 decimal places")
    void rejectsPriceWithMoreThan2DecimalPlaces() {
        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                1L,
                "RELIANCE.NS",
                OrderSide.BUY,
                10,
                new BigDecimal("150.25"),
                "IDEMPOTENCY-KEY"
        );
        assertFalse(validator.validate(placeOrderRequest).isEmpty());
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
        assertTrue(validator.validate(placeOrderRequest).isEmpty());
    }

    @Test
    @DisplayName("rejects idempotency key shorter than 8 characters")
    void rejectsIdempotencyKeyShorterThan8Characters() {
        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                1L,
                "RELIANCE.NS",
                OrderSide.BUY,
                10,
                new BigDecimal("150.25"),
                "IDEMPOT"
        );
        assertFalse(validator.validate(placeOrderRequest).isEmpty());
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
        assertTrue(validator.validate(placeOrderRequest).isEmpty());
    }

    @Test
    @DisplayName("rejects idempotency key longer than 100 characters")
    void rejectsIdempotencyKeyLongerThan100Characters() {
        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                1L,
                "RELIANCE.NS",
                OrderSide.BUY,
                10,
                new BigDecimal("150.25"),
                "a".repeat(101)
        );
        assertFalse(validator.validate(placeOrderRequest).isEmpty());
    }

    @Test
    @DisplayName("rejects null idempotency key")
    void rejectsNullIdempotencyKey() {
        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                1L,
                "RELIANCE.NS",
                OrderSide.BUY,
                10,
                new BigDecimal("150.25"),
                null
        );
        assertFalse(validator.validate(placeOrderRequest).isEmpty());
    }
}
