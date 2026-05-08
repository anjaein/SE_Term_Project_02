package com.issuetracker.domain.account.repository;

import com.google.gson.reflect.TypeToken;
import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.global.common.JsonFileManager;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

public class AccountRepository {
    private static final String FILE_PATH = "data/accounts.json";
    private static final Type TYPE = new TypeToken<List<Account>>(){}.getType();

    public List<Account> findAll() {
        return JsonFileManager.readList(FILE_PATH, TYPE);
    }

    public Account findByUsername(String username) {
        return findAll().stream()
                .filter(a -> a.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    public Account findById(Long accountId) {
        return findAll().stream()
                .filter(a -> a.getAccountId().equals(accountId))
                .findFirst()
                .orElse(null);
    }

    public void save(Account account) {
        List<Account> accounts = findAll();
        Long newId = accounts.stream()
                .mapToLong(Account::getAccountId)
                .max()
                .orElse(0L) + 1L;
        account.setAccountId(newId);
        accounts.add(account);
        JsonFileManager.writeList(FILE_PATH, accounts);
    }

    public Long getAccountIdByUsername(String username){
        return Optional.ofNullable(findByUsername(username))
                .map(Account::getAccountId)
                .orElse(null);
    }
}
