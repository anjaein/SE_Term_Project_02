package com.issuetracker.domain.account.repository;

import com.issuetracker.domain.account.entity.Account;

import java.util.List;

public interface AccountRepository {
    List<Account> findAll();
    Account findByUsername(String username);
    Account findById(Long accountId);
    boolean save(Account account);
    Long getAccountIdByUsername(String username);
}