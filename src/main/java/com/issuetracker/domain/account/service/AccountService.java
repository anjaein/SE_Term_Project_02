package com.issuetracker.domain.account.service;

import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.account.repository.AccountRepository;
import com.issuetracker.global.common.Response;

import java.util.List;

public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountValidator accountValidator;

    public AccountService(AccountRepository accountRepository, AccountValidator accountValidator) {
        this.accountRepository = accountRepository;
        this.accountValidator = accountValidator;
        if (accountRepository.findAll().isEmpty()) {
            createAccount("admin", "admin123", Role.ADMIN);
            System.out.println("An initial administrator account has been created.");
        }
    }

    public Response<Account> login(String username, String password) {
        // 요청에 누락 검증
        String missingParams = accountValidator.checkNonNull(username, password);
        if (missingParams != null) {
            return Response.fail(missingParams);
        }

        // 저장소에서 username으로 계정 조회
        Account account = accountRepository.findByUsername(username);

        // 계정이 존재하고 비밀번호가 일치하는지 확인
        if (account != null && account.getPassword().equals(password)) {
            return Response.success("Logged in.", account);
        }
        return Response.fail("Invalid username or password.");
    }

    public Response<Account> createAccount(String username, String password, Role role) {
        // 요청에 누락 검증
        String missingParams = accountValidator.checkNonNull(username, password, role);
        if (missingParams != null) {
            return Response.fail(missingParams);
        }

        // username 필수값 검증
        String blankUsername = accountValidator.checkNonBlank(username, "Username");
        if (blankUsername != null) {
            return Response.fail(blankUsername);
        }

        // password 필수값 검증
        String blankPassword = accountValidator.checkNonBlank(password, "Password");
        if (blankPassword != null) {
            return Response.fail(blankPassword);
        }

        // 중복 아이디 체크
        String duplicateUsername = accountValidator.checkUsernameDuplicate(username);
        if (duplicateUsername != null) {
            return Response.fail(duplicateUsername);
        }

        // 새로운 계정 객체 생성 (ID는 Repository에서 자동 생성됨)
        Account newAccount = new Account(username, password, role);

        // 저장소에 저장
        if (!accountRepository.save(newAccount)) {
            return Response.fail("Failed to save the account.");
        }
        return Response.success("Account created.", newAccount);
    }

    public Response<Long> getAccountIdByUsername(String username) {
        Long accountId = accountRepository.getAccountIdByUsername(username);
        if (accountId == null) {
            return Response.fail("Account not found.");
        }
        return Response.success("Account id retrieved.", accountId);
    }

    public Response<List<Account>> getAllAccounts() {
        return Response.success("Accounts retrieved.", accountRepository.findAll());
    }

    public Response<Account> getAccountById(Long accountId) {
        if (accountId == null) {
            return Response.fail("Account ID cannot be null.");
        }
        Account account = accountRepository.findById(accountId);
        if (account == null) {
            return Response.fail("User not found.");
        }
        return Response.success("User retrieved successfully.", account);
    }
}