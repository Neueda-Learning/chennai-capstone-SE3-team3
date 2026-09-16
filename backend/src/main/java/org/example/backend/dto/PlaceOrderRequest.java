package org.example.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.backend.enums.OrderSide;

public record PlaceOrderRequest(
        @NotNull @Min(1) Long accountId,
        @NotBlank @Size(min = 1, max = 20) String symbol,
        @NotNull OrderSide side,
        @NotNull @Min(1) Integer quantity,
        @NotBlank @Size(min = 8, max = 100) String idempotencyKey) {
}
