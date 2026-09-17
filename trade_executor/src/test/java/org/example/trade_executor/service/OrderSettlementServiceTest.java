package org.example.trade_executor.service;

import org.example.backend.entities.Account;
import org.example.backend.entities.Holding;
import org.example.backend.entities.Instrument;
import org.example.backend.entities.Order;
import org.example.backend.enums.*;
import org.example.trade_executor.events.OrderResolvedAfterCommitListener;
import org.example.trade_executor.events.OrderResolvedEvent;
import org.example.trade_executor.executor.ExecutionOutcome;
import org.example.trade_executor.executor.ExecutionRejectReason;
import org.example.backend.mapper.AccountMapper;
import org.example.backend.mapper.HoldingMapper;
import org.example.backend.mapper.InstrumentMapper;
import org.example.backend.mapper.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderSettlementServiceTest {

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private HoldingMapper holdingMapper;

    @Mock
    private InstrumentMapper instrumentMapper;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderResolvedAfterCommitListener orderResolvedAfterCommitListener;

    private OrderSettlementService orderSettlementService;

    @BeforeEach
    void setUp() {
        orderSettlementService = new OrderSettlementService(
                accountMapper,
                holdingMapper,
                instrumentMapper,
                orderMapper,
                orderResolvedAfterCommitListener);
    }

    @Test
    void executionTimeRecheckRejectsOrderWhoseAccountCanNoLongerAffordIt() {
        Order order = order(OrderSide.BUY, OrderPricingType.MARKET, null, 100L);
        Instrument instrument = instrument();
        Account account = account(AccountStatus.ACTIVE, new BigDecimal("1000.00"), 4L);

        when(orderMapper.selectOrderById(1L)).thenReturn(Optional.of(order));
        when(instrumentMapper.selectInstrumentById(101)).thenReturn(Optional.of(instrument));
        when(accountMapper.selectAccountById(1)).thenReturn(Optional.of(account));
        when(orderMapper.rejectOrderIfNew(anyLong(), any())).thenReturn(1);

        orderSettlementService.settle(
                1L,
                ExecutionOutcome.filled(new BigDecimal("50.1234")));

        ArgumentCaptor<OrderResolvedEvent> eventCaptor = ArgumentCaptor.forClass(OrderResolvedEvent.class);
        verify(orderMapper).rejectOrderIfNew(anyLong(), any());
        verify(orderResolvedAfterCommitListener).onOrderResolved(eventCaptor.capture());
        verify(accountMapper, never()).updateAccountBalanceWithVersion(anyInt(), any(BigDecimal.class), anyLong());
        verify(holdingMapper, never()).insertHolding(any(Holding.class));

        assertEquals("ORDER_REJECTED", eventCaptor.getValue().eventType());
        assertEquals(ExecutionRejectReason.INSUFFICIENT_FUNDS, eventCaptor.getValue().rejectReason());
    }

    @Test
    void suspendedAccountDoesNotTrade() {
        Order order = order(OrderSide.BUY, OrderPricingType.MARKET, null, 10L);
        Instrument instrument = instrument();
        Account account = account(AccountStatus.SUSPENDED, new BigDecimal("10000.00"), 4L);

        when(orderMapper.selectOrderById(1L)).thenReturn(Optional.of(order));
        when(instrumentMapper.selectInstrumentById(101)).thenReturn(Optional.of(instrument));
        when(accountMapper.selectAccountById(1)).thenReturn(Optional.of(account));
        when(orderMapper.rejectOrderIfNew(anyLong(), any())).thenReturn(1);

        orderSettlementService.settle(
                1L,
                ExecutionOutcome.filled(new BigDecimal("50.1234")));

        ArgumentCaptor<OrderResolvedEvent> eventCaptor = ArgumentCaptor.forClass(OrderResolvedEvent.class);
        verify(orderMapper).rejectOrderIfNew(anyLong(), any());
        verify(orderResolvedAfterCommitListener).onOrderResolved(eventCaptor.capture());
        verify(accountMapper, never()).updateAccountBalanceWithVersion(anyInt(), any(BigDecimal.class), anyLong());

        assertEquals("ORDER_REJECTED", eventCaptor.getValue().eventType());
        assertEquals(ExecutionRejectReason.ACCOUNT_NOT_ACTIVE, eventCaptor.getValue().rejectReason());
    }

    private static Order order(
            OrderSide side,
            OrderPricingType pricingType,
            BigDecimal price,
            long quantity) {

        return new Order(
                1L,
                "idem-1",
                OrderStatus.NEW,
                OffsetDateTime.parse("2026-09-17T09:15:30Z"),
                side,
                pricingType,
                price,
                quantity,
                null,
                1,
                101);
    }

    private static Instrument instrument() {
        return new Instrument(
                101,
                "ACME",
                "Acme Corp",
                InstrumentAssetClass.EQUITY,
                InstrumentStatus.TRADING);
    }

    private static Account account(
            AccountStatus status,
            BigDecimal balance,
            long version) {

        return new Account(
                1,
                "ACC-000001",
                LocalDate.of(2024, 1, 1),
                balance,
                balance,
                status,
                "USD",
                version,
                null,
                null,
                11);
    }
}


