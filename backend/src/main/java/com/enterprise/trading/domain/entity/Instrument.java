package com.enterprise.trading.domain.entity;

import com.enterprise.trading.domain.enums.InstrumentAssetClass;
import com.enterprise.trading.domain.enums.InstrumentStatus;

import java.util.Objects;

public class Instrument {

    private final int instrumentId;
    private final String instrumentTicker;
    private final String instrumentName;
    private final InstrumentAssetClass assetClass;
    private InstrumentStatus instrumentStatus;

    public Instrument(
            int instrumentId,
            String instrumentTicker,
            String instrumentName,
            InstrumentAssetClass assetClass,
            InstrumentStatus instrumentStatus) {
        this.instrumentId = instrumentId;
        this.instrumentTicker = instrumentTicker;
        this.instrumentName = instrumentName;
        this.assetClass = assetClass;
        this.instrumentStatus = instrumentStatus;
    }

    public int getInstrumentId() {
        return instrumentId;
    }

    public String getInstrumentTicker() {
        return instrumentTicker;
    }

    public String getInstrumentName() {
        return instrumentName;
    }

    public InstrumentAssetClass getAssetClass() {
        return assetClass;
    }

    public InstrumentStatus getInstrumentStatus() {
        return instrumentStatus;
    }

}