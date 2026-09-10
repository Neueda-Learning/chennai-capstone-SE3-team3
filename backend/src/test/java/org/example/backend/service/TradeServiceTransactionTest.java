package org.example.backend.service;

import org.example.backend.dto.OrderResponse;
import org.example.backend.entities.Account;
import org.example.backend.entities.Holding;
import org.example.backend.entities.Instrument;
import org.example.backend.entities.Order;
import org.example.backend.enums.AccountStatus;
import org.example.backend.enums.InstrumentAssetClass;
import org.example.backend.enums.InstrumentStatus;
import org.example.backend.enums.OrderSide;
import org.example.backend.enums.OrderStatus;
import org.example.backend.exceptions.AccountNotActiveException;
import org.example.backend.exceptions.OptimisticLockException;
import org.example.backend.exceptions.OrderNotCancellableException;
import org.example.backend.mapper.AccountMapper;
import org.example.backend.mapper.HoldingMapper;
import org.example.backend.mapper.InstrumentMapper;
import org.example.backend.mapper.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeServiceTransactionTest {

    private static final int ACCOUNT_ID = 1;
    private static final int INSTRUMENT_ID = 101;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private HoldingMapper holdingMapper;

    @Mock
    private InstrumentMapper instrumentMapper;

    @Mock
    private OrderMapper orderMapper;

    private TradeService tradeService;

    @BeforeEach
    void setUp() {
        tradeService = new TradeService(
                accountMapper,
                holdingMapper,
                instrumentMapper,
                orderMapper);
    }

    @Test
    void placeOrderCommitsCashAndPositionTogether() {
        Account account = activeAccount(new BigDecimal("25000.00"), 3L);
        Instrument instrument = tradableInstrument();

        when(accountMapper.selectAccountById(ACCOUNT_ID))
                .thenReturn(Optional.of(account));
        when(instrumentMapper.selectInstrumentByTicker("ACME"))
                .thenReturn(Optional.of(instrument));
        when(orderMapper.selectOrderByIdempotencyKey("idem-commit"))
                .thenReturn(Optional.empty());
        when(accountMapper.updateAccountBalanceWithVersion(
                ACCOUNT_ID,
                new BigDecimal("20000.00"),
                3L))
                .thenReturn(1);
        when(holdingMapper.selectHoldingByAccountAndInstrument(
                ACCOUNT_ID,
                INSTRUMENT_ID))
                .thenReturn(Optional.empty());
        when(holdingMapper.nextHoldingId())
                .thenReturn(5001L);
        when(orderMapper.nextOrderId())
                .thenReturn(9001L);

        OrderResponse response = tradeService.placeOrder(
                ACCOUNT_ID,
                "ACME",
                OrderSide.BUY,
                100L,
                new BigDecimal("50.00"),
                "idem-commit");

        assertEquals("9001", response.orderId());
        assertEquals(OrderStatus.FILLED, response.status());
        assertEquals("ACME", response.symbol());
        assertEquals(OrderSide.BUY, response.side());
        assertEquals(100, response.quantity());
        assertEquals(new BigDecimal("50.00"), response.price());

        InOrder orderedCalls = inOrder(accountMapper, holdingMapper, orderMapper);
        orderedCalls.verify(accountMapper)
                .updateAccountBalanceWithVersion(
                        ACCOUNT_ID,
                        new BigDecimal("20000.00"),
                        3L);
        orderedCalls.verify(holdingMapper)
                .insertHolding(any(Holding.class));
        orderedCalls.verify(orderMapper)
                .insertOrder(any(Order.class));
    }

    @Test
    void concurrentUpdateIsDetectedAndSecondWriterIsRefused() {
        Account account = activeAccount(new BigDecimal("25000.00"), 7L);
        Instrument instrument = tradableInstrument();

        when(accountMapper.selectAccountById(ACCOUNT_ID))
                .thenReturn(Optional.of(account));
        when(instrumentMapper.selectInstrumentByTicker("ACME"))
                .thenReturn(Optional.of(instrument));
        when(orderMapper.selectOrderByIdempotencyKey("idem-conflict"))
                .thenReturn(Optional.empty());
        when(accountMapper.updateAccountBalanceWithVersion(
                ACCOUNT_ID,
                new BigDecimal("20000.00"),
                7L))
                .thenReturn(0);

        assertThrows(
                OptimisticLockException.class,
                () -> tradeService.placeOrder(
                        ACCOUNT_ID,
                        "ACME",
                        OrderSide.BUY,
                        100L,
                        new BigDecimal("50.00"),
                        "idem-conflict"));

        verify(holdingMapper, never()).insertHolding(any(Holding.class));
        verify(orderMapper, never()).insertOrder(any(Order.class));
    }

    @Test
    void cancellingAFilledOrderReturnsOrd409() {
        Order filledOrder = new Order(
                9001L,
                "idem-filled",
                OrderStatus.FILLED,
                OffsetDateTime.parse("2026-09-10T09:00:00Z"),
                OrderSide.BUY,
                new BigDecimal("50.00"),
                100L,
                OffsetDateTime.parse("2026-09-10T09:01:00Z"),
                ACCOUNT_ID,
                INSTRUMENT_ID);

        when(orderMapper.selectOrderById(9001L))
                .thenReturn(Optional.of(filledOrder));
        when(orderMapper.cancelOrder(9001L, 1L))
                .thenReturn(0);

        assertThrows(
                OrderNotCancellableException.class,
                () -> tradeService.cancelOrder(9001L, ACCOUNT_ID));
    }

    @Test
    void cancellingAnotherAccountsOrderReturnsAcc403() {
        Order otherAccountsOrder = new Order(
                9002L,
                "idem-other",
                OrderStatus.NEW,
                OffsetDateTime.parse("2026-09-10T09:00:00Z"),
                OrderSide.SELL,
                new BigDecimal("25.00"),
                20L,
                null,
                2,
                INSTRUMENT_ID);

        when(orderMapper.selectOrderById(9002L))
                .thenReturn(Optional.of(otherAccountsOrder));

        assertThrows(
                AccountNotActiveException.class,
                () -> tradeService.cancelOrder(9002L, ACCOUNT_ID));

        verify(orderMapper, never()).cancelOrder(eq(9002L), eq(1L));
    }

    private static Account activeAccount(
            BigDecimal balance,
            long version) {

        return new Account(
                ACCOUNT_ID,
                "ACC-000001",
                LocalDate.of(2024, 1, 1),
                balance,
                balance,
                AccountStatus.ACTIVE,
                "USD",
                version,
                null,
                null,
                11);
    }

    private static Instrument tradableInstrument() {
        return new Instrument(
                INSTRUMENT_ID,
                "ACME",
                "Acme Corp",
                InstrumentAssetClass.EQUITY,
                InstrumentStatus.TRADING);
    }
}

