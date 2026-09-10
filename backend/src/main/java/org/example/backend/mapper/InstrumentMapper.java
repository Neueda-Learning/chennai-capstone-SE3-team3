package org.example.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.backend.entities.Instrument;

import java.util.Optional;

@Mapper
public interface InstrumentMapper {

    Optional<Instrument> selectInstrumentByTicker(
            @Param("ticker") String ticker);
}