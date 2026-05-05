package com.issuetracker.domain.account.service;

import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.account.repository.AccountRepository;

import java.util.List;

public class AccountService {

    private final AccountRepository accountRepository = new AccountRepository();

    public AccountService() {
        if (accountRepository.findAll().isEmpty()) {
            createAccount("admin", "admin123", Role.ADMIN);
            System.out.println("An initial administrator account has been created.");
        }
    }

    public Account login(String username, String password) {
        // 저장소에서 username으로 계정 조회
        Account account = accountRepository.findByUsername(username);

        // 계정이 존재하고 비밀번호가 일치하는지 확인
        if (account != null && account.getPassword().equals(password)) {
            return account;
        }
        return null;
    }

    public void createAccount(String username, String password, Role role) {
        // 중복 아이디 체크
        if (accountRepository.findByUsername(username) != null) {
            System.out.println("Username already exists: " + username);
            return;
        }

        // 새로운 계정 객체 생성 (ID는 Repository에서 자동 생성됨)
        Account newAccount = new Account(username, password, role);


        // 저장소에 저장
        accountRepository.save(newAccount);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }
}
