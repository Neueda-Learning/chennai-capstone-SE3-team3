package org.example.backend.mapper;

import org.example.backend.entities.Account;

import java.util.Optional;

public interface AccountMapper {

    Optional<Account> selectAccountById(int accountId);

    int updateAccountBalanceWithVersion(int accountId, java.math.BigDecimal balance, long version);
}

