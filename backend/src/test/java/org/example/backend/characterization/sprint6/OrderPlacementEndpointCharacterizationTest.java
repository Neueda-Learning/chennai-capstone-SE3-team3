package org.example.backend.characterization.sprint6;

import org.example.backend.controller.ApiExceptionHandler;
import org.example.backend.controller.TradeController;
import org.example.backend.dto.OrderResponse;
import org.example.backend.enums.AccountStatus;
import org.example.backend.enums.OrderSide;
import org.example.backend.enums.OrderStatus;
import org.example.backend.exceptions.AccountNotActiveException;
import org.example.backend.exceptions.DuplicateOrderException;
import org.example.backend.exceptions.InsufficientFundsException;
import org.example.backend.exceptions.InstrumentNotFoundException;
import org.example.backend.security.AuthContext;
import org.example.backend.service.TradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderPlacementEndpointCharacterizationTest {

    private TradeService tradeService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        tradeService = mock(TradeService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TradeController(tradeService))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void affordableOrderReturnedFieldByField() throws Exception {
        when(tradeService.placeOrder(
                anyLong(),
                anyString(),
                any(OrderSide.class),
                anyLong(),
                anyString()))
                .thenReturn(new OrderResponse(
                        "9001",
                        OrderStatus.NEW,
                        "Order placed successfully",
                        "ACME",
                        OrderSide.BUY,
                        100,
                        null));

        mockMvc.perform(post("/api/v1/orders")
                        .requestAttr(AuthContext.ACCOUNT_ID_ATTRIBUTE, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(placeOrderJson(1, "ACME", "BUY", 100, "idem-commit")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("9001"))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.message").value("Order placed successfully"))
                .andExpect(jsonPath("$.symbol").value("ACME"))
                .andExpect(jsonPath("$.side").value("BUY"))
                .andExpect(jsonPath("$.quantity").value(100));
    }

    @Test
    void reusedIdempotencyKeyReturnsOrd409() throws Exception {
        when(tradeService.placeOrder(
                anyLong(),
                anyString(),
                any(OrderSide.class),
                anyLong(),
                anyString()))
                .thenThrow(new DuplicateOrderException("idem-duplicate"));

        mockMvc.perform(post("/api/v1/orders")
                        .requestAttr(AuthContext.ACCOUNT_ID_ATTRIBUTE, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(placeOrderJson(1, "ACME", "BUY", 100, "idem-duplicate")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("ORD-409"))
                .andExpect(jsonPath("$.message").value("Duplicate order"));
    }

    @Test
    void unaffordableBuyReturnsOrd400() throws Exception {
        when(tradeService.placeOrder(
                anyLong(),
                anyString(),
                any(OrderSide.class),
                anyLong(),
                anyString()))
                .thenThrow(new InsufficientFundsException(
                        1L,
                        new BigDecimal("5000.00"),
                        new BigDecimal("4999.99")));

        mockMvc.perform(post("/api/v1/orders")
                        .requestAttr(AuthContext.ACCOUNT_ID_ATTRIBUTE, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(placeOrderJson(1, "ACME", "BUY", 100, "idem-insufficient")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("ORD-400"))
                .andExpect(jsonPath("$.message").value("Insufficient funds"));
    }

    @Test
    void unknownSymbolReturnsIns404() throws Exception {
        when(tradeService.placeOrder(
                anyLong(),
                anyString(),
                any(OrderSide.class),
                anyLong(),
                anyString()))
                .thenThrow(new InstrumentNotFoundException("UNKNOWN"));

        mockMvc.perform(post("/api/v1/orders")
                        .requestAttr(AuthContext.ACCOUNT_ID_ATTRIBUTE, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(placeOrderJson(1, "UNKNOWN", "BUY", 100, "idem-unknown")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("INS-404"))
                .andExpect(jsonPath("$.message").value("Instrument not found"));
    }

    @Test
    void inactiveAccountReturnsAcc403() throws Exception {
        when(tradeService.placeOrder(
                anyLong(),
                anyString(),
                any(OrderSide.class),
                anyLong(),
                anyString()))
                .thenThrow(new AccountNotActiveException(1L, AccountStatus.SUSPENDED));

        mockMvc.perform(post("/api/v1/orders")
                        .requestAttr(AuthContext.ACCOUNT_ID_ATTRIBUTE, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(placeOrderJson(1, "ACME", "BUY", 100, "idem-inactive")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACC-403"))
                .andExpect(jsonPath("$.message").value("Account not active"));
    }

    private static String placeOrderJson(
            long accountId,
            String symbol,
            String side,
            int quantity,
            String idempotencyKey) {

        return """
                {
                  "accountId": %d,
                  "symbol": "%s",
                  "side": "%s",
                  "quantity": %d,
                  "idempotencyKey": "%s"
                }
                """.formatted(
                accountId,
                symbol,
                side,
                quantity,
                idempotencyKey);
    }
}


