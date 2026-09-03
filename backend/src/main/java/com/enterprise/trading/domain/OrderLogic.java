package com.enterprise.trading.domain;

import com.enterprise.trading.domain.dto.PlaceOrderRequest;
import com.enterprise.trading.domain.entity.Account;
import com.enterprise.trading.domain.entity.Holding;
import com.enterprise.trading.domain.entity.Instrument;
import com.enterprise.trading.domain.entity.Order;
import com.enterprise.trading.domain.enums.AccountStatus;
import com.enterprise.trading.domain.enums.InstrumentStatus;
import com.enterprise.trading.domain.exception.AccountNotActiveException;
import com.enterprise.trading.domain.exception.AccountNotFoundException;
import com.enterprise.trading.domain.exception.InstrumentNotFoundException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;

public class OrderLogic {

    private final List<Account> accounts;
    private final List<Instrument> instruments;
    private final List<Holding> holdings;
    private final Set<String> acceptedIdempotencyKeys;

    public OrderLogic(
            List<Account> accounts,
            List<Instrument> instruments,
            List<Holding> holdings,
            Set<String> acceptedIdempotencyKeys) {
        this.accounts = List.copyOf(accounts);
        this.instruments = List.copyOf(instruments);
        this.holdings = List.copyOf(holdings);
        this.acceptedIdempotencyKeys = Set.copyOf(acceptedIdempotencyKeys);
    }

    public void placeOrder(PlaceOrderRequest request) {
        Account account = accounts.stream()
                .filter(currentAccount -> currentAccount.getAccountId() == request.getAccountId())
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException(request.getAccountId()));

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(account.getAccountId(), account.getAccountStatus());
        }

        Instrument instrument = instruments.stream()
                .filter(currentInstrument -> currentInstrument.getInstrumentTicker().equals(request.getSymbol()))
                .filter(currentInstrument -> currentInstrument.getInstrumentStatus() == InstrumentStatus.TRADING)
                .findFirst()
                .orElseThrow(() -> new InstrumentNotFoundException(request.getSymbol()));

        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        


    }
}
