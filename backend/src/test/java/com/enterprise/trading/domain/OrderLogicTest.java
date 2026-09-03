package com.enterprise.trading.domain;

import com.enterprise.trading.domain.dto.PlaceOrderRequest;
import com.enterprise.trading.domain.entity.Account;
import com.enterprise.trading.domain.entity.Instrument;
import com.enterprise.trading.domain.enums.AccountStatus;
import com.enterprise.trading.domain.enums.InstrumentAssetClass;
import com.enterprise.trading.domain.enums.InstrumentStatus;
import com.enterprise.trading.domain.enums.OrderSide;
import com.enterprise.trading.domain.exception.AccountNotActiveException;
import com.enterprise.trading.domain.exception.AccountNotFoundException;
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
                    10,
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
                    10,
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
                    10,
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
                    10,
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
                    10,
                    new BigDecimal("150.25"),
                    "IDEMPOTENCY-KEY"
            );

            assertDoesNotThrow(() -> orderLogic.placeOrder(placeOrderRequest));
        }
    }
//
//    @Nested
//    @DisplayName("Rule 3: The Instrument must EXIST and be TRADABLE")
//    class InstrumentValidity {
//
//        @Test
//        @DisplayName("reject unknown instrument")
//        void rejectUnknownInstrument() {
//        }
//
//        @Test
//        @DisplayName("reject halted instrument")
//        void rejectHaltedInstrument() {
//        }
//
//        @Test
//        @DisplayName("reject retired instrument")
//        void rejectRetiredInstrument() {
//        }
//
//        @Test
//        @DisplayName("accept trading instrument")
//        void acceptTradingInstrument() {
//        }
//
//    }
//
//    @Nested
//    @DisplayName("Rule 4: Quantity > 0")
//    class QuantityValidity {
//
//        @Test
//        @DisplayName("reject 0 quantity")
//        void rejectZeroQuantity() {
//        }
//
//        @Test
//        @DisplayName("reject negative quantity")
//        void rejectNegativeQuantity() {
//        }
//
//        @Test
//        @DisplayName("accept positive quantity")
//        void acceptPositiveQuantity() {
//        }
//    }
//
//    @Nested
//    @DisplayName("Rule 5: Price > 0")
//    class PriceValidity {
//
//        @Test
//        @DisplayName("reject 0 price")
//        void rejectZeroPrice() {
//        }
//
//        @Test
//        @DisplayName("reject negative price")
//        void rejectNegativePrice() {
//        }
//
//        @Test
//        @DisplayName("accept positive price")
//        void acceptPositivePrice() {
//        }
//    }
//
//    @Nested
//    @DisplayName("Rule 6: On a BUY, Quantity * Price <= Cash Balance")
//    class BuyValidity {
//
//        @Test
//        @DisplayName("reject buy when cost (quantity * price) > balance")
//        void rejectBuyWhenCostGreaterThanBalance() {
//        }
//
//        @Test
//        @DisplayName("accept buy when cost = balance")
//        void acceptBuyWhenCostEqualToBalance() {
//        }
//
//        @Test
//        @DisplayName("accept buy when cost < balance")
//        void acceptBuyWhenCostLessThanBalance() {
//        }
//    }
//
//    @Nested
//    @DisplayName("Rule 7: On a SELL, Quantity to be Sold <= Quantity in Holdings")
//    class SellValidity {
//
//        @Test
//        @DisplayName("reject sell when quantity > holdings")
//        void rejectSellWhenQuantityGreaterThanHoldings() {
//        }
//
//        @Test
//        @DisplayName("accept sell when quantity = holdings")
//        void acceptSellWhenQuantityEqualToHoldings() {
//        }
//
//        @Test
//        @DisplayName("accept sell when quantity < holdings")
//        void acceptSellWhenQuantityLessThanHoldings() {
//        }
//    }
//
//    @Nested
//    @DisplayName("Rule 8: Idempotency Key must not already have been used")
//    class IdempotencyKey {
//
//        @Test
//        @DisplayName("reject already accepted idempotency key")
//        void rejectAlreadyAcceptedIdempotencyKey() {
//        }
//
//        @Test
//        @DisplayName("accept unused idempotency key")
//        void acceptUnusedIdempotencyKey() {
//        }
//    }
//
//    @Nested
//    @DisplayName("evaluation order")
//    class EvaluationOrder {
//
//        @Test
//        @DisplayName("account existence fails before account status")
//        void accountExistenceFailsFirst() {
//        }
//
//        @Test
//        @DisplayName("account status fails before instrument validation")
//        void accountStatusFailsBeforeInstrumentValidation() {
//        }
//
//        @Test
//        @DisplayName("instrument validation fails before quantity validation")
//        void instrumentValidationFailsBeforeQuantityValidation() {
//        }
//
//        @Test
//        @DisplayName("quantity validation fails before price validation")
//        void quantityFailsBeforePrice() {
//        }
//
//        @Test
//        @DisplayName("price validation fails before BUY validation")
//        void priceValidationFailsBeforeBuyValidation() {
//        }
//
//        @Test
//        @DisplayName("INSUFFICIENT FUNDS fail before INSUFFICIENT HOLDINGS")
//        void insufficientFundsFailBeforeInsufficientHoldings() {
//        }
//
//        @Test
//        @DisplayName("INSUFFICIENT HOLDINGS fail before IDEMPOTENCY check")
//        void insufficientHoldingsFailBeforeIdempotencyKey() {
//        }
//    }
}