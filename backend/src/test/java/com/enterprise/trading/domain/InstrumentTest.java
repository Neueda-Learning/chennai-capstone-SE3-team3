package com.enterprise.trading.domain;

import com.enterprise.trading.domain.entity.Instrument;
import com.enterprise.trading.domain.enums.InstrumentAssetClass;
import com.enterprise.trading.domain.enums.InstrumentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InstrumentTest {

    @Test
    @DisplayName("stores instrument reference data")
    public void storesInstrumentReferenceData() {
        Instrument instrument = new Instrument(
                1,
                "RELIANCE.NS",
                "Reliance Industries",
                InstrumentAssetClass.EQUITY,
                InstrumentStatus.TRADING
        );

        assertEquals("RELIANCE.NS", instrument.getInstrumentTicker());
        assertEquals("Reliance Industries", instrument.getInstrumentTicker());
        assertEquals(InstrumentAssetClass.EQUITY, instrument.getAssetClass());
        assertEquals(InstrumentStatus.TRADING, instrument.getInstrumentStatus());
    }

    @Test
    @DisplayName("trading instrument is tradable")
    void tradingInstrumentIsTradable() {
        Instrument instrument = new Instrument(
                1,
                "RELIANCE.NS",
                "Reliance Industries",
                InstrumentAssetClass.EQUITY,
                InstrumentStatus.TRADING
        );

        assertTrue(instrument.isTradable());
    }

    @Test
    @DisplayName("halted instrument is NOT tradable")
    void haltedInstrumentIsNotTradable() {
        Instrument instrument = new Instrument(
                1,
                "RELIANCE.NS",
                "Reliance Industries",
                InstrumentAssetClass.EQUITY,
                InstrumentStatus.HALTED
        );

        assertFalse(instrument.isTradable());
    }

    @Test
    @DisplayName("retired instrument is NOT tradable")
    void retiredInstrumentIsNotTradable() {
        Instrument instrument = new Instrument(
                1,
                "RELIANCE.NS",
                "Reliance Industries",
                InstrumentAssetClass.EQUITY,
                InstrumentStatus.TRADING
        );

        assertFalse(instrument.isTradable());
    }
}
