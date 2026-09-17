package org.example.backend.mapper;

import org.example.backend.entities.Order;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

public interface OrderMapper {

    Optional<Order> selectOrderById(long orderId);

    int rejectOrderIfNew(long orderId, OffsetDateTime rejectedAt);

    int fillOrderIfNew(long orderId, BigDecimal executionPrice, OffsetDateTime filledAt);
}

