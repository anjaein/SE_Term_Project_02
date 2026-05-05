package com.issuetracker.domain.account.controller;

import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.account.service.AccountService;

public class AccountController {
    private AccountService accountService = new AccountService();
    private Account loggedInAccount = null; // 현재 로그인한 계정

    // 로그인
    public boolean login(String username, String password) {
        Account account = accountService.login(username, password);
        if (account != null) {
            loggedInAccount = account;
            notifySuccess(account.getUsername() + "login successfully");
            return true;
        }
        notifyError("The ID or password is invalid.");
        return false;
    }

    // 로그아웃
    public void logout() {
        loggedInAccount = null;
    }

    // 현재 로그인한 계정 반환
    public Account getLoggedInAccount() {
        return loggedInAccount;
    }

    // 계정 생성 (admin만 가능)
    public void createAccount(String username, String password, Role role) {
        if (loggedInAccount.getRole() != Role.ADMIN) {
            notifyError("Only admin can create an account.");
            return;
        }
        accountService.createAccount(username, password, role);
        notifySuccess("Your account has been created.");
    }

    private void notifySuccess(String message) {
        System.out.println("[SUCCESS] " + message);
    }

    private void notifyError(String message) {
        System.out.println("[ERROR] " + message);
    }
}