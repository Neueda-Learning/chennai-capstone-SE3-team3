package org.example.backend.mapper;

import org.example.backend.entities.Holding;

import java.util.Optional;

public interface HoldingMapper {

    Optional<Holding> selectHoldingByAccountAndInstrument(int accountId, int instrumentId);

    long nextHoldingId();

    int insertHolding(Holding holding);

    int updateHoldingQuantity(long holdingId, long quantity);

    int updateHoldingQuantityAndPrice(long holdingId, long quantity, java.math.BigDecimal purchasePrice);
}

