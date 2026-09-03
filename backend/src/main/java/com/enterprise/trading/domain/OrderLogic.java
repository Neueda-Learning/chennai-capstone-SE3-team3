package com.enterprise.trading.domain;

import com.enterprise.trading.domain.dto.PlaceOrderRequest;
import com.enterprise.trading.domain.entity.Account;
import com.enterprise.trading.domain.entity.Holding;
import com.enterprise.trading.domain.entity.Instrument;
import com.enterprise.trading.domain.entity.Order;
import com.enterprise.trading.domain.exception.AccountNotFoundException;

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
    }
}
