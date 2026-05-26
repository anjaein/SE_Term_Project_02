package com.issuetracker.domain.account.controller;

import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.account.service.AccountService;
import com.issuetracker.global.common.Response;
import com.issuetracker.global.common.SessionManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final SessionManager sessionManager;

    // 로그인
    public Response<Account> login(String username, String password) {
        Response<Account> result = accountService.login(username, password);
        if (!result.isSuccess()) {
            return Response.fail(result.getMessage());
        }
        sessionManager.login(result.getData());
        return Response.success(result.getData().getUsername() + " logged in successfully.", result.getData());
    }

    // 로그아웃
    public Response<Void> logout() {
        sessionManager.logout();
        return Response.success("Logged out.");
    }

    // 계정 생성 (admin만 가능)
    public Response<Account> createAccount(String username, String password, Role role) {
        Account currentUser = sessionManager.getLoggedInAccount();
        if (currentUser == null) {
            return Response.fail("You are not logged in.");
        }
        if (currentUser.getRole() != Role.ADMIN) {
            return Response.fail("Only admin can create an account.");
        }
        return accountService.createAccount(username, password, role);
    }

    public Response<Long> getAccountIdByUsername(String username) {
        return accountService.getAccountIdByUsername(username);
    }
    public Response<Account> getAccountById(Long accountId) {
        return accountService.getAccountById(accountId);
    }
}
