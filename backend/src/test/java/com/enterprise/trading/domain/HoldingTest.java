package com.enterprise.trading.domain;

import com.enterprise.trading.domain.entity.Holding;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HoldingTest {

    @Test
    @DisplayName("stores purchase price at 2 decimal places")
    void storesPurchasePriceAt2DecimalPlaces() {
        Holding holding = new Holding(
                1L,
                100L,
                new BigDecimal("25.50"),
                10,
                20
        );

        assertEquals(new BigDecimal("25.50"), holding.getPurchasePrice());
    }

    @Test
    @DisplayName("stores the initial quantity")
    void storesInitialQuantity() {
        Holding holding = new Holding(
                1L,
                100L,
                new BigDecimal("25.50"),
                10,
                20
        );

        assertEquals(100, holding.getQuantity());
    }

    @Test
    @DisplayName("rejects negative quantity")
    void rejectsNegativeQuantity() {
        Holding holding = new Holding(
                1L,
                100L,
                new BigDecimal("25.50"),
                10,
                20
        );

        assertThrows(IllegalArgumentException.class, () -> holding.createHolding(-1));
    }
}
