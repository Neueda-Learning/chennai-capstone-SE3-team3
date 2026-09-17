package org.example.backend.events;

import org.example.backend.enums.OrderSide;

import java.time.OffsetDateTime;

public record OrderPlacedMessage(
        long orderId,
        int accountId,
        String symbol,
        OrderSide side,
        long quantity,
        OffsetDateTime occurredAt) {
}

