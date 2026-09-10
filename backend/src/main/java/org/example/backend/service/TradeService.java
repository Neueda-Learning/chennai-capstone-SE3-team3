package org.example.backend.service;

import org.example.backend.dto.AccountResponse;
import org.example.backend.dto.BalanceResponse;
import org.example.backend.dto.OrderHistoryEntry;
import org.example.backend.dto.OrderResponse;
import org.example.backend.dto.PositionResponse;
import org.example.backend.entities.Account;
import org.example.backend.entities.Holding;
import org.example.backend.entities.Instrument;
import org.example.backend.entities.Order;
import org.example.backend.enums.AccountStatus;
import org.example.backend.enums.OrderSide;
import org.example.backend.enums.OrderStatus;
import org.example.backend.exceptions.AccountNotActiveException;
import org.example.backend.exceptions.AccountNotFoundException;
import org.example.backend.exceptions.DuplicateOrderException;
import org.example.backend.exceptions.InsufficientFundsException;
import org.example.backend.exceptions.InsufficientHoldingsException;
import org.example.backend.exceptions.InstrumentNotFoundException;
import org.example.backend.exceptions.OptimisticLockException;
import org.example.backend.exceptions.OrderNotFoundException;
import org.example.backend.exceptions.OrderNotCancellableException;
import org.example.backend.exceptions.OrderNotFoundException;
import org.example.backend.mapper.AccountMapper;
import org.example.backend.mapper.HoldingMapper;
import org.example.backend.mapper.InstrumentMapper;
import org.example.backend.mapper.OrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TradeService {

    private final AccountMapper accountMapper;
    private final HoldingMapper holdingMapper;
    private final InstrumentMapper instrumentMapper;
    private final OrderMapper orderMapper;

    public TradeService(
            AccountMapper accountMapper,
            HoldingMapper holdingMapper,
            InstrumentMapper instrumentMapper,
            OrderMapper orderMapper) {

        this.accountMapper = accountMapper;
        this.holdingMapper = holdingMapper;
        this.instrumentMapper = instrumentMapper;
        this.orderMapper = orderMapper;
    }

    // =========================================================
    // STORY 5 - PLACE ORDER
    // =========================================================

    @Transactional
    public OrderResponse placeOrder(
            long accountId,
            String symbol,
            OrderSide side,
            long quantity,
            BigDecimal price,
            String idempotencyKey) {

        Account account = accountMapper
                .selectAccountById((int) accountId)
                .orElseThrow(() ->
                        new AccountNotFoundException(accountId));

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(
                    accountId,
                    account.getAccountStatus());
        }

        Instrument instrument = instrumentMapper
                .selectInstrumentByTicker(symbol)
                .orElseThrow(() ->
                        new InstrumentNotFoundException(symbol));

        if (!instrument.isTradable()) {
            throw new InstrumentNotFoundException(symbol);
        }

        if (orderMapper
                .selectOrderByIdempotencyKey(idempotencyKey)
                .isPresent()) {

            throw new DuplicateOrderException(idempotencyKey);
        }

        BigDecimal orderValue = price
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);

        Holding holding = null;

        if (side == OrderSide.BUY) {

            if (!account.canAfford(orderValue)) {
                throw new InsufficientFundsException(
                        accountId,
                        orderValue,
                        account.getBalance());
            }

        } else {

            holding = holdingMapper
                    .selectHoldingByAccountAndInstrument(
                            (int) accountId,
                            instrument.getInstrumentId())
                    .orElse(null);

            long availableQuantity =
                    holding == null
                            ? 0
                            : holding.getQuantity();

            if (availableQuantity < quantity) {
                throw new InsufficientHoldingsException(
                        accountId,
                        symbol,
                        quantity,
                        availableQuantity);
            }
        }

        // -----------------------------------------------------
        // Change account in memory
        // -----------------------------------------------------

        if (side == OrderSide.BUY) {
            account.debit(orderValue);
        } else {
            account.credit(orderValue);
        }

        // -----------------------------------------------------
        // Story 5 - optimistic locking
        // -----------------------------------------------------

        int affectedRows =
                accountMapper.updateAccountBalanceWithVersion(
                        (int) accountId,
                        account.getBalance(),
                        account.getVersion());

        if (affectedRows == 0) {
            throw new OptimisticLockException();
        }

        // -----------------------------------------------------
        // Update holding
        // -----------------------------------------------------

        if (side == OrderSide.BUY) {

            Holding existingHolding =
                    holdingMapper
                            .selectHoldingByAccountAndInstrument(
                                    (int) accountId,
                                    instrument.getInstrumentId())
                            .orElse(null);

            if (existingHolding == null) {

                long holdingId =
                        holdingMapper.nextHoldingId();

                Holding newHolding = new Holding(
                        holdingId,
                        quantity,
                        price,
                        (int) accountId,
                        instrument.getInstrumentId());

                holdingMapper.insertHolding(newHolding);

            } else {

                long oldQuantity =
                        existingHolding.getQuantity();

                long newQuantity =
                        oldQuantity + quantity;

                BigDecimal oldValue =
                        existingHolding.getPurchasePrice()
                                .multiply(
                                        BigDecimal.valueOf(oldQuantity));

                BigDecimal newValue =
                        price.multiply(
                                BigDecimal.valueOf(quantity));

                BigDecimal averagePrice =
                        oldValue
                                .add(newValue)
                                .divide(
                                        BigDecimal.valueOf(newQuantity),
                                        2,
                                        RoundingMode.HALF_UP);

                holdingMapper.updateHoldingQuantityAndPrice(
                        existingHolding.getHoldingId(),
                        newQuantity,
                        averagePrice);
            }

        } else {

            long remainingQuantity =
                    holding.getQuantity() - quantity;

            holdingMapper.updateHoldingQuantity(
                    holding.getHoldingId(),
                    remainingQuantity);
        }

        // -----------------------------------------------------
        // Create order
        // -----------------------------------------------------

        long orderId = orderMapper.nextOrderId();

        OffsetDateTime receivedAt =
                OffsetDateTime.now();

        Order order = new Order(
                orderId,
                idempotencyKey,
                OrderStatus.NEW,
                receivedAt,
                side,
                price,
                quantity,
                null,
                (int) accountId,
                instrument.getInstrumentId());

        order.fill();

        orderMapper.insertOrder(order);

        return new OrderResponse(
                String.valueOf(order.getOrderId()),
                order.getOrderStatus(),
                "Order placed successfully",
                symbol,
                order.getOrderSide(),
                (int) order.getQuantity(),
                order.getPrice());
    }

    // =========================================================
    // STORY 5 - CANCEL ORDER
    // =========================================================

    /*
     * NOTE:
     * The current Order entity/database uses long order_id,
     * while the API contract specifies UUID for cancellation.
     *
     * This method therefore cannot be made contract-correct until
     * the order ID representation is aligned.
     *
     * Do NOT fake a UUID -> long conversion.
     */
    @Transactional
    public OrderResponse cancelOrder(
            long orderId,
            long accountId) {

        Order order = orderMapper
                .selectOrderById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                syntheticOrderUuid(orderId)));

        if (order.getAccountId() != accountId) {
            throw new AccountNotActiveException(
                    accountId,
                    AccountStatus.ACTIVE);
        }

        int affectedRows =
                orderMapper.cancelOrder(
                        orderId,
                        accountId);

        if (affectedRows == 0) {
            throw new OrderNotCancellableException(
                    syntheticOrderUuid(orderId));
        }

        return new OrderResponse(
                String.valueOf(orderId),
                OrderStatus.CANCELLED,
                "Order cancelled successfully",
                null,
                order.getOrderSide(),
                (int) order.getQuantity(),
                order.getPrice());
    }

    private static UUID syntheticOrderUuid(long orderId) {
        return UUID.nameUUIDFromBytes(
                String.valueOf(orderId)
                        .getBytes());
    }

    // =========================================================
    // STORY 6 - ACCOUNT
    // =========================================================

    public AccountResponse getAccount(long accountId) {

        AccountResponse account =
                accountMapper.selectAccountResponseById(accountId);

        if (account == null) {
            throw new AccountNotFoundException(accountId);
        }

        return account;
    }

    // =========================================================
    // STORY 6 - BALANCE
    // =========================================================

    public BalanceResponse getBalance(long accountId) {

        BalanceResponse balance =
                accountMapper.selectBalanceResponseByAccountId(
                        accountId);

        if (balance == null) {
            throw new AccountNotFoundException(accountId);
        }

        return balance;
    }

    // =========================================================
    // STORY 6 - POSITIONS
    // =========================================================

    public List<PositionResponse> getPositions(
            long accountId) {

        if (accountMapper.selectAccountById((int) accountId)
                .isEmpty()) {

            throw new AccountNotFoundException(accountId);
        }

        return holdingMapper
                .selectPositionResponsesByAccountId(accountId);
    }

    // =========================================================
    // STORY 6 - ORDER HISTORY
    // =========================================================

    public List<OrderHistoryEntry> getOrders(
            long accountId,
            OrderStatus status,
            OffsetDateTime from,
            OffsetDateTime to) {

        if (accountMapper.selectAccountById((int) accountId)
                .isEmpty()) {

            throw new AccountNotFoundException(accountId);
        }

        return orderMapper.selectOrderHistoryByAccountId(
                accountId,
                status,
                from,
                to);
    }
}