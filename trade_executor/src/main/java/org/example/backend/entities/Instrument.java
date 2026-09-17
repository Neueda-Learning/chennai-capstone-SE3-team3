package org.example.backend.entities;

import org.example.backend.enums.InstrumentAssetClass;
import org.example.backend.enums.InstrumentStatus;

import java.util.Objects;

public class Instrument {

    private final int instrumentId;
    private final String instrumentTicker;
    private final String instrumentName;
    private final InstrumentAssetClass instrumentAssetClass;
    private final InstrumentStatus instrumentStatus;

    public Instrument(
            int instrumentId,
            String instrumentTicker,
            String instrumentName,
            InstrumentAssetClass instrumentAssetClass,
            InstrumentStatus instrumentStatus) {

        this.instrumentId = instrumentId;
        this.instrumentTicker = Objects.requireNonNull(instrumentTicker, "instrumentTicker is required");
        this.instrumentName = Objects.requireNonNull(instrumentName, "instrumentName is required");
        this.instrumentAssetClass = Objects.requireNonNull(instrumentAssetClass, "instrumentAssetClass is required");
        this.instrumentStatus = Objects.requireNonNull(instrumentStatus, "instrumentStatus is required");
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

    public InstrumentAssetClass getInstrumentAssetClass() {
        return instrumentAssetClass;
    }

    public InstrumentStatus getInstrumentStatus() {
        return instrumentStatus;
    }

    public boolean isTradable() {
        return instrumentStatus == InstrumentStatus.TRADING;
    }
}

