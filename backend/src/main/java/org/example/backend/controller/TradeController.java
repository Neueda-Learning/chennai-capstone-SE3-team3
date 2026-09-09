package org.example.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.example.backend.dto.AccountResponse;
import org.example.backend.dto.BalanceResponse;
import org.example.backend.dto.OrderHistoryEntry;
import org.example.backend.dto.OrderResponse;
import org.example.backend.dto.PlaceOrderRequest;
import org.example.backend.dto.PositionResponse;
import org.example.backend.enums.AccountStatus;
import org.example.backend.enums.OrderStatus;
import org.example.backend.exceptions.AccountNotActiveException;
import org.example.backend.security.AuthContext;
import org.example.backend.service.dummyservice;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Validated
public class TradeController {

    private final dummyservice tradeService;

    public TradeController(dummyservice tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping("/orders")
    public OrderResponse placeOrder(
            @Valid @RequestBody PlaceOrderRequest request,
            HttpServletRequest httpServletRequest) {

        long authenticatedAccountId = AuthContext.requiredAccountId(httpServletRequest);
        if (authenticatedAccountId != request.accountId()) {
            throw new AccountNotActiveException(request.accountId(), AccountStatus.ACTIVE);
        }
        return tradeService.placeOrder(request);
    }

    @DeleteMapping("/orders/{id}")
    public OrderResponse cancelOrder(
            @PathVariable UUID id,
            HttpServletRequest httpServletRequest) {
        long authenticatedAccountId = AuthContext.requiredAccountId(httpServletRequest);
        return tradeService.cancelOrder(id, authenticatedAccountId);
    }

    @GetMapping("/accounts/{id}")
    public AccountResponse getAccount(
            @PathVariable("id") @Min(1) long accountId,
            HttpServletRequest httpServletRequest) {

        assertAccess(accountId, httpServletRequest);
        return tradeService.getAccount(accountId);
    }

    @GetMapping("/accounts/{id}/balance")
    public BalanceResponse getBalance(
            @PathVariable("id") @Min(1) long accountId,
            HttpServletRequest httpServletRequest) {

        assertAccess(accountId, httpServletRequest);
        return tradeService.getBalance(accountId);
    }

    @GetMapping("/accounts/{id}/positions")
    public List<PositionResponse> getPositions(
            @PathVariable("id") @Min(1) long accountId,
            HttpServletRequest httpServletRequest) {

        assertAccess(accountId, httpServletRequest);
        return tradeService.getPositions(accountId);
    }

    @GetMapping("/accounts/{id}/orders")
    public List<OrderHistoryEntry> getOrders(
            @PathVariable("id") @Min(1) long accountId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            HttpServletRequest httpServletRequest) {

        assertAccess(accountId, httpServletRequest);
        return tradeService.getOrders(accountId, status, from, to);
    }

    private static void assertAccess(long accountId, HttpServletRequest request) {
        long authenticatedAccountId = AuthContext.requiredAccountId(request);
        if (authenticatedAccountId != accountId) {
            throw new AccountNotActiveException(accountId, AccountStatus.ACTIVE);
        }
    }
}
