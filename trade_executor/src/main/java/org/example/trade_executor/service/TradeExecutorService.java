package org.example.trade_executor.service;

import org.example.trade_executor.client.LiveQuote;
import org.example.trade_executor.client.QuoteClient;
import org.example.trade_executor.client.QuoteUnavailableException;
import org.example.backend.entities.Instrument;
import org.example.backend.entities.Order;
import org.example.backend.enums.OrderStatus;
import org.example.backend.events.OrderPlacedMessage;
import org.example.backend.exceptions.OptimisticLockException;
import org.example.trade_executor.executor.ExecutionOutcome;
import org.example.trade_executor.executor.ExecutionRejectReason;
import org.example.trade_executor.executor.FillDecisionEngine;
import org.example.backend.mapper.InstrumentMapper;
import org.example.backend.mapper.OrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TradeExecutorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TradeExecutorService.class);

    private final OrderMapper orderMapper;
    private final InstrumentMapper instrumentMapper;
    private final QuoteClient quoteClient;
    private final FillDecisionEngine fillDecisionEngine;
    private final OrderSettlementService orderSettlementService;
    private final int maxSettlementAttempts;

    public TradeExecutorService(
            OrderMapper orderMapper,
            InstrumentMapper instrumentMapper,
            QuoteClient quoteClient,
            FillDecisionEngine fillDecisionEngine,
            OrderSettlementService orderSettlementService,
            @Value("${trading.executor.max-settlement-attempts:3}") int maxSettlementAttempts) {

        this.orderMapper = orderMapper;
        this.instrumentMapper = instrumentMapper;
        this.quoteClient = quoteClient;
        this.fillDecisionEngine = fillDecisionEngine;
        this.orderSettlementService = orderSettlementService;
        this.maxSettlementAttempts = Math.max(1, maxSettlementAttempts);
    }

    public void execute(OrderPlacedMessage message) {
        Order order = orderMapper.selectOrderById(message.orderId()).orElse(null);
        if (order == null) {
            LOGGER.warn("ORDER_PLACED received for missing order {}", message.orderId());
            return;
        }

        if (order.getOrderStatus() != OrderStatus.NEW) {
            LOGGER.info("Skipping order {} because status is {}", order.getOrderId(), order.getOrderStatus());
            return;
        }

        Instrument instrument = instrumentMapper.selectInstrumentById(order.getInstrumentId()).orElse(null);
        ExecutionOutcome marketOutcome = determineMarketOutcome(order, instrument);
        settleWithRetry(order.getOrderId(), marketOutcome);
    }

    private ExecutionOutcome determineMarketOutcome(Order order, Instrument instrument) {
        if (instrument == null || !instrument.isTradable()) {
            return ExecutionOutcome.rejected(ExecutionRejectReason.INSTRUMENT_NOT_TRADABLE);
        }

        try {
            Optional<LiveQuote> quote = quoteClient.getQuote(instrument.getInstrumentTicker());
            if (quote.isEmpty()) {
                return ExecutionOutcome.rejected(ExecutionRejectReason.PRICE_UNAVAILABLE);
            }

            return fillDecisionEngine.decide(order, quote.get());
        } catch (QuoteUnavailableException ex) {
            LOGGER.warn("Quote unavailable for symbol {} after retries", ex.getSymbol(), ex);
            return ExecutionOutcome.rejected(ExecutionRejectReason.PRICE_UNAVAILABLE);
        }
    }

    private void settleWithRetry(long orderId, ExecutionOutcome marketOutcome) {
        for (int attempt = 1; attempt <= maxSettlementAttempts; attempt++) {
            try {
                orderSettlementService.settle(orderId, marketOutcome);
                return;
            } catch (OptimisticLockException ex) {
                if (attempt == maxSettlementAttempts) {
                    throw ex;
                }

                LOGGER.warn(
                        "Retrying settlement for order {} after optimistic lock conflict (attempt {} of {})",
                        orderId,
                        attempt,
                        maxSettlementAttempts);
            }
        }
    }
}

