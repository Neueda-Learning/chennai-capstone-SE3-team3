package org.example.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.backend.entities.Holding;

import java.util.List;
import java.util.Optional;

/**
 * MyBatis mapper for Holding entity.
 * All parameters are bound as JDBC bind parameters for security.
 * No string interpolation is allowed in SQL queries.
 */
@Mapper
public interface HoldingMapper {

    /**
     * Insert a new holding.
     * All values are bound as parameters.
     *
     * @param holding the holding to insert
     * @return the number of rows affected (1 on success)
     * @throws org.springframework.dao.DataIntegrityViolationException if constraint violated
     */
    int insertHolding(@Param("holding") Holding holding);

    /**
     * Select holding by holding ID.
     * Holding ID is bound as a parameter.
     *
     * @param holdingId the holding ID to search for
     * @return Optional containing the holding if found
     */
    Optional<Holding> selectHoldingById(@Param("holdingId") long holdingId);

    /**
     * Select holding by account and instrument.
     * Both account ID and instrument ID are bound as parameters.
     *
     * @param accountId the account ID
     * @param instrumentId the instrument ID
     * @return Optional containing the holding if found
     */
    Optional<Holding> selectHoldingByAccountAndInstrument(
            @Param("accountId") int accountId,
            @Param("instrumentId") int instrumentId);

    /**
     * Select all holdings for a specific account.
     * Account ID is bound as a parameter.
     *
     * @param accountId the account ID to filter by
     * @return list of holdings for the account
     */
    List<Holding> selectHoldingsByAccountId(@Param("accountId") int accountId);

    /**
     * Select all holdings for a specific instrument.
     * Instrument ID is bound as a parameter.
     *
     * @param instrumentId the instrument ID to filter by
     * @return list of holdings for the instrument
     */
    List<Holding> selectHoldingsByInstrumentId(@Param("instrumentId") int instrumentId);

    /**
     * Select holdings with quantity greater than zero (active positions).
     * Account ID is bound as a parameter.
     *
     * @param accountId the account ID
     * @return list of active holdings (quantity > 0)
     */
    List<Holding> selectActiveHoldingsByAccountId(@Param("accountId") int accountId);

    /**
     * Update holding quantity.
     * Holding ID and new quantity are bound as parameters.
     *
     * @param holdingId the holding to update
     * @param newQuantity the new quantity
     * @return the number of rows affected
     */
    int updateHoldingQuantity(
            @Param("holdingId") long holdingId,
            @Param("newQuantity") long newQuantity);

    /**
     * Update holding quantity and purchase price.
     * All values are bound as parameters.
     *
     * @param holdingId the holding to update
     * @param newQuantity the new quantity
     * @param newPurchasePrice the new purchase price
     * @return the number of rows affected
     */
    int updateHoldingQuantityAndPrice(
            @Param("holdingId") long holdingId,
            @Param("newQuantity") long newQuantity,
            @Param("newPurchasePrice") java.math.BigDecimal newPurchasePrice);

    /**
     * Delete a holding by ID.
     * Holding ID is bound as a parameter.
     *
     * @param holdingId the holding to delete
     * @return the number of rows affected
     */
    int deleteHoldingById(@Param("holdingId") long holdingId);

    /**
     * Count holdings for an account.
     * Account ID is bound as a parameter.
     *
     * @param accountId the account ID
     * @return the number of holdings for the account
     */
    long countHoldingsByAccountId(@Param("accountId") int accountId);

    /**
     * Count active holdings for an account.
     * Account ID is bound as a parameter.
     *
     * @param accountId the account ID
     * @return the number of active holdings (quantity > 0)
     */
    long countActiveHoldingsByAccountId(@Param("accountId") int accountId);

    /**
     * Check if a holding exists for account and instrument.
     * Both account ID and instrument ID are bound as parameters.
     *
     * @param accountId the account ID
     * @param instrumentId the instrument ID
     * @return true if holding exists, false otherwise
     */
    boolean holdingExists(
            @Param("accountId") int accountId,
            @Param("instrumentId") int instrumentId);
}
