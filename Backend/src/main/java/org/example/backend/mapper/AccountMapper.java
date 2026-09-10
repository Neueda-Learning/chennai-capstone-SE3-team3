package org.example.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.backend.entities.Account;
import org.example.backend.enums.AccountStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * MyBatis mapper for Account entity.
 * All parameters are bound as JDBC bind parameters for security.
 * No string interpolation is allowed in SQL queries.
 */
@Mapper
public interface AccountMapper {

    /**
     * Insert a new account.
     * All values are bound as parameters.
     *
     * @param account the account to insert
     * @return the number of rows affected
     */
    int insertAccount(@Param("account") Account account);

    /**
     * Select account by account ID.
     * Account ID is bound as a parameter.
     *
     * @param accountId the account ID to search for
     * @return Optional containing the account if found
     */
    Optional<Account> selectAccountById(@Param("accountId") int accountId);

    /**
     * Select account by account number (key).
     * Account number is bound as a parameter.
     *
     * @param accountNumber the account number to search for
     * @return Optional containing the account if found
     */
    Optional<Account> selectAccountByNumber(@Param("accountNumber") String accountNumber);

    /**
     * Select all accounts for a specific client.
     * Client ID is bound as a parameter.
     *
     * @param clientId the client ID to filter by
     * @return list of accounts for the client
     */
    List<Account> selectAccountsByClientId(@Param("clientId") int clientId);

    /**
     * Select accounts by status filter.
     * Status is bound as a parameter.
     *
     * @param accountStatus the status to filter by (ACTIVE, SUSPENDED, CLOSED)
     * @return list of accounts with the specified status
     */
    List<Account> selectAccountsByStatus(@Param("accountStatus") AccountStatus accountStatus);

    /**
     * Select accounts by status and client ID.
     * Both parameters are bound.
     *
     * @param clientId the client ID to filter by
     * @param accountStatus the status to filter by
     * @return list of accounts matching both criteria
     */
    List<Account> selectAccountsByClientIdAndStatus(
            @Param("clientId") int clientId,
            @Param("accountStatus") AccountStatus accountStatus);

    /**
     * Update account status.
     * Account ID and new status are bound as parameters.
     *
     * @param accountId the account to update
     * @param newStatus the new status
     * @return the number of rows affected
     */
    int updateAccountStatus(
            @Param("accountId") int accountId,
            @Param("newStatus") AccountStatus newStatus);

    /**
     * Update account balance and purchasing power.
     * All values are bound as parameters.
     *
     * @param accountId the account to update
     * @param newBalance the new balance
     * @param newPurchasingPower the new purchasing power
     * @return the number of rows affected
     */
    int updateAccountBalance(
            @Param("accountId") int accountId,
            @Param("newBalance") java.math.BigDecimal newBalance,
            @Param("newPurchasingPower") java.math.BigDecimal newPurchasingPower);

    /**
     * Update account suspension timestamp.
     * Account ID and timestamp are bound as parameters.
     *
     * @param accountId the account to update
     * @param suspendedAt the suspension timestamp
     * @return the number of rows affected
     */
    int updateSuspensionTimestamp(
            @Param("accountId") int accountId,
            @Param("suspendedAt") OffsetDateTime suspendedAt);

    /**
     * Update account closure timestamp.
     * Account ID and timestamp are bound as parameters.
     *
     * @param accountId the account to update
     * @param closedAt the closure timestamp
     * @return the number of rows affected
     */
    int updateClosureTimestamp(
            @Param("accountId") int accountId,
            @Param("closedAt") OffsetDateTime closedAt);

    /**
     * Select accounts opened on or after a specific date.
     * Date is bound as a parameter.
     *
     * @param openingDate the opening date filter
     * @return list of accounts opened on or after the date
     */
    List<Account> selectAccountsByOpeningDateRange(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    /**
     * Count all accounts.
     *
     * @return the total number of accounts
     */
    long countAllAccounts();

    /**
     * Check if an account number exists.
     * Account number is bound as a parameter.
     *
     * @param accountNumber the account number to check
     * @return true if account exists, false otherwise
     */
    boolean accountNumberExists(@Param("accountNumber") String accountNumber);
}
