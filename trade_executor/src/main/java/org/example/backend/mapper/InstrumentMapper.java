package org.example.backend.mapper;

import org.example.backend.entities.Instrument;

import java.util.Optional;

public interface InstrumentMapper {

    Optional<Instrument> selectInstrumentById(int instrumentId);
}

