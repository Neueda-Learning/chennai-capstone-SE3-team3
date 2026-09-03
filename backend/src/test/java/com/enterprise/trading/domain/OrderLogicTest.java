package com.enterprise.trading.domain;

import com.enterprise.trading.domain.dto.PlaceOrderRequest;
import com.enterprise.trading.domain.entity.Account;
import com.enterprise.trading.domain.entity.Holding;
import com.enterprise.trading.domain.entity.Instrument;
import com.enterprise.trading.domain.enums.AccountStatus;
import com.enterprise.trading.domain.enums.InstrumentAssetClass;
import com.enterprise.trading.domain.enums.InstrumentStatus;
import com.enterprise.trading.domain.enums.OrderSide;
import com.enterprise.trading.domain.exception.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class OrderLogicTest {

    @Nested
    @DisplayName("Rule 1: The Account must exist")
    class AccountExistence {

        @Test
        @DisplayName("reject order when the account doesn't exist")
        void rejectOrderWhenAccountDoesNotExist() {
            OrderLogic orderLogic = new OrderLogic(
                    List.of(),
                    List.of(),
                    List.of(),
                    Set.of()
            );
            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    1L,
                    "RELIANCE.NS",
                    OrderSide.BUY,
                    1,
                    new BigDecimal("150.25"),
                    "IDEMPOTENCY-KEY"
            );

            AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> orderLogic.placeOrder(placeOrderRequest));
            assertEquals("ACC-404", exception.getErrorCode());
            assertEquals(1L, exception.getAccountId());
        }

        @Test
        @DisplayName("continue when account exists")
        void continueWhenAccountExists() {
            Account account = new Account(
                    1,
                    "ETP000000001",
                    LocalDate.of(2026, 9, 2),
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    AccountStatus.ACTIVE,
                    "INR",
                    1L,
                    null,
                    null,
                    10
            );

            Instrument instrument = new Instrument(
                    1,
                    "RELIANCE.NS",
                    "Reliance Industries",
                    InstrumentAssetClass.EQUITY,
                    InstrumentStatus.TRADING
            );

            OrderLogic orderLogic = new OrderLogic(
                    List.of(account),
                    List.of(instrument),
                    List.of(),
                    Set.of()
            );

            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    1L,
                    "RELIANCE.NS",
                    OrderSide.BUY,
                    1,
                    new BigDecimal("150.25"),
                    "IDEMPOTENCY-KEY"
            );

            assertDoesNotThrow(() -> orderLogic.placeOrder(placeOrderRequest));
        }


    }

    @Nested
    @DisplayName("Rule 2: The Account must be ACTIVE")
    class AccountStatusChecker {

        @Test
        @DisplayName("reject suspended account")
        void rejectSuspendedAccount() {
            Account account = new Account(
                    1,
                    "ETP000000001",
                    LocalDate.of(2026, 9, 2),
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    AccountStatus.SUSPENDED,
                    "INR",
                    1L,
                    null,
                    null,
                    10
            );

            OrderLogic orderLogic = new OrderLogic(
                    List.of(account),
                    List.of(),
                    List.of(),
                    Set.of()
            );

            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    1L,
                    "RELIANCE.NS",
                    OrderSide.BUY,
                    1,
                    new BigDecimal("150.25"),
                    "IDEMPOTENCY-KEY"
            );

            AccountNotActiveException exception = assertThrows(AccountNotActiveException.class, () -> orderLogic.placeOrder(placeOrderRequest));
            assertEquals("ACC-403", exception.getErrorCode());
            assertEquals(1L, exception.getAccountId());
            assertEquals(AccountStatus.SUSPENDED, exception.getStatus());
        }

        @Test
        @DisplayName("reject closed account")
        void rejectClosedAccount() {
            Account account = new Account(
                    1,
                    "ETP000000001",
                    LocalDate.of(2026, 9, 2),
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    AccountStatus.CLOSED,
                    "INR",
                    1L,
                    null,
                    null,
                    10
            );

            OrderLogic orderLogic = new OrderLogic(
                    List.of(account),
                    List.of(),
                    List.of(),
                    Set.of()
            );

            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    1L,
                    "RELIANCE.NS",
                    OrderSide.BUY,
                    1,
                    new BigDecimal("150.25"),
                    "IDEMPOTENCY-KEY"
            );

            AccountNotActiveException exception = assertThrows(AccountNotActiveException.class, () -> orderLogic.placeOrder(placeOrderRequest));
            assertEquals("ACC-403", exception.getErrorCode());
            assertEquals(1L, exception.getAccountId());
            assertEquals(AccountStatus.CLOSED, exception.getStatus());
        }

        @Test
        @DisplayName("accept active account")
        void acceptActiveAccount() {

            Account account = new Account(
                    1,
                    "ETP000000001",
                    LocalDate.of(2026, 9, 2),
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    AccountStatus.ACTIVE,
                    "INR",
                    1L,
                    null,
                    null,
                    10
            );

            Instrument instrument = new Instrument(
                    1,
                    "RELIANCE.NS",
                    "Reliance Industries",
                    InstrumentAssetClass.EQUITY,
                    InstrumentStatus.TRADING
            );

            OrderLogic orderLogic = new OrderLogic(
                    List.of(account),
                    List.of(instrument),
                    List.of(),
                    Set.of()
            );

            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    1L,
                    "RELIANCE.NS",
                    OrderSide.BUY,
                    1,
                    new BigDecimal("150.25"),
                    "IDEMPOTENCY-KEY"
            );

            assertDoesNotThrow(() -> orderLogic.placeOrder(placeOrderRequest));
        }
    }

    @Nested
    @DisplayName("Rule 3: The Instrument must EXIST and be TRADABLE")
    class InstrumentValidity {

        @Test
        @DisplayName("reject unknown instrument")
        void rejectUnknownInstrument() {
            Account account = new Account(
                    1,
                    "ETP000000001",
                    LocalDate.of(2026, 9, 2),
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    AccountStatus.ACTIVE,
                    "INR",
                    1L,
                    null,
                    null,
                    10
            );

            OrderLogic orderLogic = new OrderLogic(
                    List.of(account),
                    List.of(),
                    List.of(),
                    Set.of()
            );

            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    1L,
                    "RELIANCE.NS",
                    OrderSide.BUY,
                    1,
                    new BigDecimal("150.25"),
                    "IDEMPOTENCY-KEY"
            );

            InstrumentNotFoundException exception = assertThrows(InstrumentNotFoundException.class, () -> orderLogic.placeOrder(placeOrderRequest));
            assertEquals("INS-404", exception.getErrorCode());
            assertEquals("RELIANCE.NS", exception.getSymbol());
        }

        @Test
        @DisplayName("reject halted instrument")
        void rejectHaltedInstrument() {
            Account account = new Account(
                    1,
                    "ETP000000001",
                    LocalDate.of(2026, 9, 2),
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    AccountStatus.ACTIVE,
                    "INR",
                    1L,
                    null,
                    null,
                    10
            );

            Instrument instrument = new Instrument(
                    1,
                    "RELIANCE.NS",
                    "Reliance Industries",
                    InstrumentAssetClass.EQUITY,
                    InstrumentStatus.HALTED
            );

            OrderLogic orderLogic = new OrderLogic(
                    List.of(account),
                    List.of(instrument),
                    List.of(),
                    Set.of()
            );

            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    1L,
                    "RELIANCE.NS",
                    OrderSide.BUY,
                    1,
                    new BigDecimal("150.25"),
                    "IDEMPOTENCY-KEY"
            );

            InstrumentNotFoundException exception = assertThrows(InstrumentNotFoundException.class, () -> orderLogic.placeOrder(placeOrderRequest));
            assertEquals("INS-404", exception.getErrorCode());
            assertEquals("RELIANCE.NS", exception.getSymbol());
        }

        @Test
        @DisplayName("reject retired instrument")
        void rejectRetiredInstrument() {
            Account account = new Account(
                    1,
                    "ETP000000001",
                    LocalDate.of(2026, 9, 2),
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    AccountStatus.ACTIVE,
                    "INR",
                    1L,
                    null,
                    null,
                    10
            );

            Instrument instrument = new Instrument(
                    1,
                    "RELIANCE.NS",
                    "Reliance Industries",
                    InstrumentAssetClass.EQUITY,
                    InstrumentStatus.RETIRED
            );

            OrderLogic orderLogic = new OrderLogic(
                    List.of(account),
                    List.of(instrument),
                    List.of(),
                    Set.of()
            );

            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    1L,
                    "RELIANCE.NS",
                    OrderSide.BUY,
                    1,
                    new BigDecimal("150.25"),
                    "IDEMPOTENCY-KEY"
            );

            InstrumentNotFoundException exception = assertThrows(InstrumentNotFoundException.class, () -> orderLogic.placeOrder(placeOrderRequest));
            assertEquals("INS-404", exception.getErrorCode());
            assertEquals("RELIANCE.NS", exception.getSymbol());
        }

        @Test
        @DisplayName("accept trading instrument")
        void acceptTradingInstrument() {
            Account account = new Account(
                    1,
                    "ETP000000001",
                    LocalDate.of(2026, 9, 2),
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    AccountStatus.ACTIVE,
                    "INR",
                    1L,
                    null,
                    null,
                    10
            );

            Instrument instrument = new Instrument(
                    1,
                    "RELIANCE.NS",
                    "Reliance Industries",
                    InstrumentAssetClass.EQUITY,
                    InstrumentStatus.TRADING
            );

            OrderLogic orderLogic = new OrderLogic(
                    List.of(account),
                    List.of(instrument),
                    List.of(),
                    Set.of()
            );

            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    1L,
                    "RELIANCE.NS",
                    OrderSide.BUY,
                    1,
                    new BigDecimal("150.25"),
                    "IDEMPOTENCY-KEY"
            );

            assertDoesNotThrow(() -> orderLogic.placeOrder(placeOrderRequest));
        }

    }

    @Nested
    @DisplayName("Rule 4: Quantity > 0")
    class QuantityValidity {

        @Test
        @DisplayName("reject 0 quantity")
        void rejectZeroQuantity() {
            Account account = new Account(
                    1,
                    "ETP000000001",
                    LocalDate.of(2026, 9, 2),
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    AccountStatus.ACTIVE,
                    "INR",
                    1L,
                    null,
                    null,
                    10
            );

            Instrument instrument = new Instrument(
                    1,
                    "RELIANCE.NS",
                    "Reliance Industries",
                    InstrumentAssetClass.EQUITY,
                    InstrumentStatus.TRADING
            );

            OrderLogic orderLogic = new OrderLogic(
                    List.of(account),
                    List.of(instrument),
                    List.of(),
                    Set.of()
            );

            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    1L,
                    "RELIANCE.NS",
                    OrderSide.BUY,
                    0,
                    new BigDecimal("150.25"),
                    "IDEMPOTENCY-KEY"
            );

            assertThrows(IllegalArgumentException.class, () -> orderLogic.placeOrder(placeOrderRequest));
        }

        @Test
        @DisplayName("reject negative quantity")
        void rejectNegativeQuantity() {
            Account account = new Account(
                    1,
                    "ETP000000001",
                    LocalDate.of(2026, 9, 2),
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    AccountStatus.ACTIVE,
                    "INR",
                    1L,
                    null,
                    null,
                    10
            );

            Instrument instrument = new Instrument(
                    1,
                    "RELIANCE.NS",
                    "Reliance Industries",
                    InstrumentAssetClass.EQUITY,
                    InstrumentStatus.TRADING
            );

            OrderLogic orderLogic = new OrderLogic(
                    List.of(account),
                    List.of(instrument),
                    List.of(),
                    Set.of()
            );

            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    1L,
                    "RELIANCE.NS",
                    OrderSide.BUY,
                    -1,
                    new BigDecimal("150.25"),
                    "IDEMPOTENCY-KEY"
            );

            assertThrows(IllegalArgumentException.class, () -> orderLogic.placeOrder(placeOrderRequest));
        }

        @Test
        @DisplayName("accept positive quantity")
        void acceptPositiveQuantity() {

            Account account = new Account(
                    1,
                    "ETP000000001",
                    LocalDate.of(2026, 9, 2),
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    AccountStatus.ACTIVE,
                    "INR",
                    1L,
                    null,
                    null,
                    10
            );

            Instrument instrument = new Instrument(
                    1,
                    "RELIANCE.NS",
                    "Reliance Industries",
                    InstrumentAssetClass.EQUITY,
                    InstrumentStatus.TRADING
            );

            OrderLogic orderLogic = new OrderLogic(
                    List.of(account),
                    List.of(instrument),
                    List.of(),
                    Set.of()
            );

            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    1L,
                    "RELIANCE.NS",
                    OrderSide.BUY,
                    1,
                    new BigDecimal("150.25"),
                    "IDEMPOTENCY-KEY"
            );

            assertDoesNotThrow(() -> orderLogic.placeOrder(placeOrderRequest));
        }
    }

    @Nested
    @DisplayName("Rule 5: Price > 0")
    class PriceValidity {

        @Test
        @DisplayName("reject 0 price")
        void rejectZeroPrice() {
            Account account = new Account(
                    1,
                    "ETP000000001",
                    LocalDate.of(2026, 9, 2),
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    AccountStatus.ACTIVE,
                    "INR",
                    1L,
                    null,
                    null,
                    10
            );

            Instrument instrument = new Instrument(
                    1,
                    "RELIANCE.NS",
                    "Reliance Industries",
                    InstrumentAssetClass.EQUITY,
                    InstrumentStatus.TRADING
            );

            OrderLogic orderLogic = new OrderLogic(
                    List.of(account),
                    List.of(instrument),
                    List.of(),
                    Set.of()
            );

            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    1L,
                    "RELIANCE.NS",
                    OrderSide.BUY,
                    1,
                    BigDecimal.ZERO,
                    "IDEMPOTENCY-KEY"
            );

            assertThrows(IllegalArgumentException.class, () -> orderLogic.placeOrder(placeOrderRequest));
        }

        @Test
        @DisplayName("reject negative price")
        void rejectNegativePrice() {

            Account account = new Account(
                    1,
                    "ETP000000001",
                    LocalDate.of(2026, 9, 2),
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    AccountStatus.ACTIVE,
                    "INR",
                    1L,
                    null,
                    null,
                    10
            );

            Instrument instrument = new Instrument(
                    1,
                    "RELIANCE.NS",
                    "Reliance Industries",
                    InstrumentAssetClass.EQUITY,
                    InstrumentStatus.TRADING
            );

            OrderLogic orderLogic = new OrderLogic(
                    List.of(account),
                    List.of(instrument),
                    List.of(),
                    Set.of()
            );

            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    1L,
                    "RELIANCE.NS",
                    OrderSide.BUY,
                    1,
                    new BigDecimal("-1"),
                    "IDEMPOTENCY-KEY"
            );

            assertThrows(IllegalArgumentException.class, () -> orderLogic.placeOrder(placeOrderRequest));
        }

        @Test
        @DisplayName("accept positive price")
        void acceptPositivePrice() {

            Account account = new Account(
                    1,
                    "ETP000000001",
                    LocalDate.of(2026, 9, 2),
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    AccountStatus.ACTIVE,
                    "INR",
                    1L,
                    null,
                    null,
                    10
            );

            Instrument instrument = new Instrument(
                    1,
                    "RELIANCE.NS",
                    "Reliance Industries",
                    InstrumentAssetClass.EQUITY,
                    InstrumentStatus.TRADING
            );

            OrderLogic orderLogic = new OrderLogic(
                    List.of(account),
                    List.of(instrument),
                    List.of(),
                    Set.of()
            );

            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    1L,
                    "RELIANCE.NS",
                    OrderSide.BUY,
                    1,
                    new BigDecimal("150.25"),
                    "IDEMPOTENCY-KEY"
            );

            assertDoesNotThrow(() -> orderLogic.placeOrder(placeOrderRequest));
        }
    }

    @Nested
    @DisplayName("Rule 6: On a BUY, Quantity * Price <= Cash Balance")
    class BuyValidity {

        @Test
        @DisplayName("reject buy when cost (quantity * price) > balance")
        void rejectBuyWhenCostGreaterThanBalance() {
            Account account = new Account(
                    1,
                    "ETP000000001",
                    LocalDate.of(2026, 9, 2),
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    AccountStatus.ACTIVE,
                    "INR",
                    1L,
                    null,
                    null,
                    10
            );

            Instrument instrument = new Instrument(
                    1,
                    "RELIANCE.NS",
                    "Reliance Industries",
                    InstrumentAssetClass.EQUITY,
                    InstrumentStatus.TRADING
            );

            OrderLogic orderLogic = new OrderLogic(
                    List.of(account),
                    List.of(instrument),
                    List.of(),
                    Set.of()
            );

            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    1L,
                    "RELIANCE.NS",
                    OrderSide.BUY,
                    1,
                    new BigDecimal("1100.00"),
                    "IDEMPOTENCY-KEY"
            );

            InsufficientFundsException exception = assertThrows(InsufficientFundsException.class, () -> orderLogic.placeOrder(placeOrderRequest));
            assertEquals(1L, exception.getAccountId());
            assertEquals(new BigDecimal("1100.00"), exception.getRequired());
            assertEquals(new BigDecimal("1000.00"), exception.getAvailable());
        }

        @Test
        @DisplayName("accept buy when cost = balance")
        void acceptBuyWhenCostEqualToBalance() {
            Account account = new Account(
                    1,
                    "ETP000000001",
                    LocalDate.of(2026, 9, 2),
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    AccountStatus.ACTIVE,
                    "INR",
                    1L,
                    null,
                    null,
                    10
            );

            Instrument instrument = new Instrument(
                    1,
                    "RELIANCE.NS",
                    "Reliance Industries",
                    InstrumentAssetClass.EQUITY,
                    InstrumentStatus.TRADING
            );

            OrderLogic orderLogic = new OrderLogic(
                    List.of(account),
                    List.of(instrument),
                    List.of(),
                    Set.of()
            );

            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    1L,
                    "RELIANCE.NS",
                    OrderSide.BUY,
                    1,
                    new BigDecimal("1000.00"),
                    "IDEMPOTENCY-KEY"
            );

            assertDoesNotThrow(() -> orderLogic.placeOrder(placeOrderRequest));
        }

        @Test
        @DisplayName("accept buy when cost < balance")
        void acceptBuyWhenCostLessThanBalance() {
            Account account = new Account(
                    1,
                    "ETP000000001",
                    LocalDate.of(2026, 9, 2),
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    AccountStatus.ACTIVE,
                    "INR",
                    1L,
                    null,
                    null,
                    10
            );

            Instrument instrument = new Instrument(
                    1,
                    "RELIANCE.NS",
                    "Reliance Industries",
                    InstrumentAssetClass.EQUITY,
                    InstrumentStatus.TRADING
            );

            OrderLogic orderLogic = new OrderLogic(
                    List.of(account),
                    List.of(instrument),
                    List.of(),
                    Set.of()
            );

            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    1L,
                    "RELIANCE.NS",
                    OrderSide.BUY,
                    1,
                    new BigDecimal("999.00"),
                    "IDEMPOTENCY-KEY"
            );

            assertDoesNotThrow(() -> orderLogic.placeOrder(placeOrderRequest));
        }
    }

    @Nested
    @DisplayName("Rule 7: On a SELL, Quantity to be Sold <= Quantity in Holdings")
    class SellValidity {

        @Test
        @DisplayName("reject sell when quantity > holdings")
        void rejectSellWhenQuantityGreaterThanHoldings() {
            Account account = new Account(
                    1,
                    "ETP000000001",
                    LocalDate.of(2026, 9, 2),
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    AccountStatus.ACTIVE,
                    "INR",
                    1L,
                    null,
                    null,
                    10
            );

            Instrument instrument = new Instrument(
                    1,
                    "RELIANCE.NS",
                    "Reliance Industries",
                    InstrumentAssetClass.EQUITY,
                    InstrumentStatus.TRADING
            );

            Holding holding = new Holding(
                    1L,
                    1L,
                    new BigDecimal("25.50"),
                    1,
                    1
            );

            OrderLogic orderLogic = new OrderLogic(
                    List.of(account),
                    List.of(instrument),
                    List.of(holding),
                    Set.of()
            );

            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    1L,
                    "RELIANCE.NS",
                    OrderSide.SELL,
                    20,
                    new BigDecimal("25.50"),
                    "IDEMPOTENCY-KEY"
            );

            InsufficientHoldingsException exception = assertThrows(InsufficientHoldingsException.class, () -> orderLogic.placeOrder(placeOrderRequest));
            assertEquals("ORD-409", exception.getErrorCode());
            assertEquals(1L, exception.getAccountId());
            assertEquals("RELIANCE.NS", exception.getSymbol());
            assertEquals(20L, exception.getRequestedQuantity());
            assertEquals(1L, exception.getAvailableQuantity());
        }

        @Test
        @DisplayName("accept sell when quantity = holdings")
        void acceptSellWhenQuantityEqualToHoldings() {
            Account account = new Account(
                    1,
                    "ETP000000001",
                    LocalDate.of(2026, 9, 2),
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    AccountStatus.ACTIVE,
                    "INR",
                    1L,
                    null,
                    null,
                    10
            );

            Instrument instrument = new Instrument(
                    1,
                    "RELIANCE.NS",
                    "Reliance Industries",
                    InstrumentAssetClass.EQUITY,
                    InstrumentStatus.TRADING
            );

            Holding holding = new Holding(
                    1L,
                    1L,
                    new BigDecimal("25.50"),
                    1,
                    1
            );

            OrderLogic orderLogic = new OrderLogic(
                    List.of(account),
                    List.of(instrument),
                    List.of(holding),
                    Set.of()
            );

            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    1L,
                    "RELIANCE.NS",
                    OrderSide.SELL,
                    1,
                    new BigDecimal("25.50"),
                    "IDEMPOTENCY-KEY"
            );

            assertDoesNotThrow(() -> orderLogic.placeOrder(placeOrderRequest));
        }

        @Test
        @DisplayName("accept sell when quantity < holdings")
        void acceptSellWhenQuantityLessThanHoldings() {
            Account account = new Account(
                    1,
                    "ETP000000001",
                    LocalDate.of(2026, 9, 2),
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    AccountStatus.ACTIVE,
                    "INR",
                    1L,
                    null,
                    null,
                    10
            );

            Instrument instrument = new Instrument(
                    1,
                    "RELIANCE.NS",
                    "Reliance Industries",
                    InstrumentAssetClass.EQUITY,
                    InstrumentStatus.TRADING
            );

            Holding holding = new Holding(
                    1L,
                    100L,
                    new BigDecimal("25.50"),
                    1,
                    1
            );

            OrderLogic orderLogic = new OrderLogic(
                    List.of(account),
                    List.of(instrument),
                    List.of(holding),
                    Set.of()
            );

            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    1L,
                    "RELIANCE.NS",
                    OrderSide.SELL,
                    20,
                    new BigDecimal("25.50"),
                    "IDEMPOTENCY-KEY"
            );
        }
    }

    @Nested
    @DisplayName("Rule 8: Idempotency Key must not already have been used")
    class IdempotencyKey {

        @Test
        @DisplayName("reject already accepted idempotency key")
        void rejectAlreadyAcceptedIdempotencyKey() {
            Account account = new Account(
                    1,
                    "ETP000000001",
                    LocalDate.of(2026, 9, 2),
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    AccountStatus.ACTIVE,
                    "INR",
                    1L,
                    null,
                    null,
                    10
            );

            Instrument instrument = new Instrument(
                    1,
                    "RELIANCE.NS",
                    "Reliance Industries",
                    InstrumentAssetClass.EQUITY,
                    InstrumentStatus.TRADING
            );

            Holding holding = new Holding(
                    1L,
                    100L,
                    new BigDecimal("25.50"),
                    1,
                    1
            );

            OrderLogic orderLogic = new OrderLogic(
                    List.of(account),
                    List.of(instrument),
                    List.of(holding),
                    Set.of("IDEMPOTENCY-KEY")
            );

            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    1L,
                    "RELIANCE.NS",
                    OrderSide.SELL,
                    20,
                    new BigDecimal("25.50"),
                    "IDEMPOTENCY-KEY"
            );

            DuplicateOrderException exception = assertThrows(DuplicateOrderException.class, () -> orderLogic.placeOrder(placeOrderRequest));
            assertEquals("ORD-409", exception.getErrorCode());
            assertEquals("IDEMPOTENCY-KEY", exception.getIdempotencyKey());
        }

        @Test
        @DisplayName("accept unused idempotency key")
        void acceptUnusedIdempotencyKey() {
            Account account = new Account(
                    1,
                    "ETP000000001",
                    LocalDate.of(2026, 9, 2),
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    AccountStatus.ACTIVE,
                    "INR",
                    1L,
                    null,
                    null,
                    10
            );

            Instrument instrument = new Instrument(
                    1,
                    "RELIANCE.NS",
                    "Reliance Industries",
                    InstrumentAssetClass.EQUITY,
                    InstrumentStatus.TRADING
            );

            Holding holding = new Holding(
                    1L,
                    100L,
                    new BigDecimal("25.50"),
                    1,
                    1
            );

            OrderLogic orderLogic = new OrderLogic(
                    List.of(account),
                    List.of(instrument),
                    List.of(holding),
                    Set.of("IDEMPOTENCY-KEY2")
            );

            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    1L,
                    "RELIANCE.NS",
                    OrderSide.SELL,
                    20,
                    new BigDecimal("25.50"),
                    "IDEMPOTENCY-KEY"
            );

            assertDoesNotThrow(() -> orderLogic.placeOrder(placeOrderRequest));
        }
    }

    @Nested
    @DisplayName("evaluation order")
    class EvaluationOrder {

        @Test
        @DisplayName("account existence fails before account status")
        void accountExistenceFailsFirst() {

            OrderLogic orderLogic = new OrderLogic(
                    List.of(),
                    List.of(),
                    List.of(),
                    Set.of()
            );

            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    1L,
                    "RELIANCE.NS",
                    OrderSide.BUY,
                    1,
                    new BigDecimal("150.25"),
                    "IDEMPOTENCY-KEY"
            );

            AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> orderLogic.placeOrder(placeOrderRequest));

            assertEquals("ACC-404", exception.getErrorCode());
        }

        @Test
        @DisplayName("account status fails before instrument validation")
        void accountStatusFailsBeforeInstrumentValidation() {

            Account account = new Account(
                    1,
                    "ETP000000001",
                    LocalDate.of(2026, 9, 2),
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    AccountStatus.SUSPENDED,
                    "INR",
                    1L,
                    null,
                    null,
                    10
            );

            OrderLogic orderLogic = new OrderLogic(
                    List.of(account),
                    List.of(),
                    List.of(),
                    Set.of()
            );

            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    1L,
                    "RELIANCE.NS",
                    OrderSide.BUY,
                    1,
                    new BigDecimal("150.25"),
                    "IDEMPOTENCY-KEY"
            );

            AccountNotActiveException exception = assertThrows(AccountNotActiveException.class, () -> orderLogic.placeOrder(placeOrderRequest));

            assertEquals("ACC-403", exception.getErrorCode());
        }

        @Test
        @DisplayName("instrument validation fails before quantity validation")
        void instrumentValidationFailsBeforeQuantityValidation() {

            Account account = new Account(
                    1,
                    "ETP000000001",
                    LocalDate.of(2026, 9, 2),
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    AccountStatus.ACTIVE,
                    "INR",
                    1L,
                    null,
                    null,
                    10
            );

            OrderLogic orderLogic = new OrderLogic(
                    List.of(account),
                    List.of(),
                    List.of(),
                    Set.of()
            );

            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    1L,
                    "UNKNOWN.NS",
                    OrderSide.BUY,
                    1,
                    new BigDecimal("150.25"),
                    "IDEMPOTENCY-KEY"
            );

            InstrumentNotFoundException exception = assertThrows(InstrumentNotFoundException.class, () -> orderLogic.placeOrder(placeOrderRequest));

            assertEquals("INS-404", exception.getErrorCode());
            assertEquals("UNKNOWN.NS", exception.getSymbol());
        }

        @Test
        @DisplayName("quantity validation fails before price validation")
        void quantityFailsBeforePrice() {

            Account account = new Account(
                    1,
                    "ETP000000001",
                    LocalDate.of(2026, 9, 2),
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    AccountStatus.ACTIVE,
                    "INR",
                    1L,
                    null,
                    null,
                    10
            );

            Instrument instrument = new Instrument(
                    1,
                    "RELIANCE.NS",
                    "Reliance Industries",
                    InstrumentAssetClass.EQUITY,
                    InstrumentStatus.TRADING
            );

            OrderLogic orderLogic = new OrderLogic(
                    List.of(account),
                    List.of(instrument),
                    List.of(),
                    Set.of()
            );

            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    1L,
                    "RELIANCE.NS",
                    OrderSide.BUY,
                    1,
                    new BigDecimal("150.25"),
                    "IDEMPOTENCY-KEY"
            );

            assertDoesNotThrow(() -> orderLogic.placeOrder(placeOrderRequest));
        }

        @Test
        @DisplayName("price validation fails before BUY validation")
        void priceValidationFailsBeforeBuyValidation() {

            Account account = new Account(
                    1,
                    "ETP000000001",
                    LocalDate.of(2026, 9, 2),
                    new BigDecimal("100.00"),
                    new BigDecimal("100.00"),
                    AccountStatus.ACTIVE,
                    "INR",
                    1L,
                    null,
                    null,
                    10
            );

            Instrument instrument = new Instrument(
                    1,
                    "RELIANCE.NS",
                    "Reliance Industries",
                    InstrumentAssetClass.EQUITY,
                    InstrumentStatus.TRADING
            );

            OrderLogic orderLogic = new OrderLogic(
                    List.of(account),
                    List.of(instrument),
                    List.of(),
                    Set.of()
            );

            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    1L,
                    "RELIANCE.NS",
                    OrderSide.BUY,
                    1,
                    new BigDecimal("100.00"),
                    "IDEMPOTENCY-KEY"
            );

            assertDoesNotThrow(() -> orderLogic.placeOrder(placeOrderRequest));
        }

        @Test
        @DisplayName("INSUFFICIENT FUNDS fail before later validation")
        void insufficientFundsFailBeforeInsufficientHoldings() {

            Account account = new Account(
                    1,
                    "ETP000000001",
                    LocalDate.of(2026, 9, 2),
                    new BigDecimal("100.00"),
                    new BigDecimal("100.00"),
                    AccountStatus.ACTIVE,
                    "INR",
                    1L,
                    null,
                    null,
                    10
            );

            Instrument instrument = new Instrument(
                    1,
                    "RELIANCE.NS",
                    "Reliance Industries",
                    InstrumentAssetClass.EQUITY,
                    InstrumentStatus.TRADING
            );

            OrderLogic orderLogic = new OrderLogic(
                    List.of(account),
                    List.of(instrument),
                    List.of(),
                    Set.of()
            );

            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    1L,
                    "RELIANCE.NS",
                    OrderSide.BUY,
                    1,
                    new BigDecimal("150.25"),
                    "IDEMPOTENCY-KEY"
            );

            InsufficientFundsException exception = assertThrows(InsufficientFundsException.class, () -> orderLogic.placeOrder(placeOrderRequest));

            assertEquals("ORD-400", exception.getErrorCode());
            assertEquals(1L, exception.getAccountId());
        }

        @Test
        @DisplayName("INSUFFICIENT HOLDINGS fail before IDEMPOTENCY check")
        void insufficientHoldingsFailBeforeIdempotencyKey() {

            Account account = new Account(
                    1,
                    "ETP000000001",
                    LocalDate.of(2026, 9, 2),
                    new BigDecimal("1000.00"),
                    new BigDecimal("1000.00"),
                    AccountStatus.ACTIVE,
                    "INR",
                    1L,
                    null,
                    null,
                    10
            );

            Instrument instrument = new Instrument(
                    1,
                    "RELIANCE.NS",
                    "Reliance Industries",
                    InstrumentAssetClass.EQUITY,
                    InstrumentStatus.TRADING
            );

            Holding holding = new Holding(
                    1L,
                    1L,
                    new BigDecimal("25.50"),
                    1,
                    1
            );

            OrderLogic orderLogic = new OrderLogic(
                    List.of(account),
                    List.of(instrument),
                    List.of(holding),
                    Set.of("IDEMPOTENCY-KEY")
            );

            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    1L,
                    "RELIANCE.NS",
                    OrderSide.SELL,
                    20,
                    new BigDecimal("25.50"),
                    "IDEMPOTENCY-KEY"
            );

            InsufficientHoldingsException exception = assertThrows(InsufficientHoldingsException.class, () -> orderLogic.placeOrder(placeOrderRequest));

            assertEquals("ORD-409", exception.getErrorCode());
            assertEquals(1L, exception.getAccountId());
            assertEquals("RELIANCE.NS", exception.getSymbol());
            assertEquals(20L, exception.getRequestedQuantity());
            assertEquals(1L, exception.getAvailableQuantity());
        }
    }
}