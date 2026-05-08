package com.issuetracker.domain.account.controller;

import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.account.service.AccountService;
import com.issuetracker.global.common.SessionManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final SessionManager sessionManager;

    // 로그인
    public boolean login(String username, String password) {
        Account account = accountService.login(username, password);
        if (account != null) {
            sessionManager.login(account);
            notifySuccess(account.getUsername() + "login successfully");
            return true;
        }
        notifyError("The ID or password is invalid.");
        return false;
    }

    // 로그아웃
    public void logout() {
        sessionManager.logout();
    }

    // 계정 생성 (admin만 가능)
    public void createAccount(String username, String password, Role role) {
        if (sessionManager.getLoggedInAccount().getRole() != Role.ADMIN) {
            notifyError("Only admin can create an account.");
            return;
        }
        accountService.createAccount(username, password, role);
        notifySuccess("Your account has been created.");
    }

    public Long getAccountIdByUsername(String username){
        return accountService.getAccountIdByUsername(username);
    }

    private void notifySuccess(String message) {
        System.out.println("[SUCCESS] " + message);
    }

    private void notifyError(String message) {
        System.out.println("[ERROR] " + message);
    }
}