package com.issuetracker.global.common;

import com.issuetracker.domain.account.entity.Account;



public class SessionManager {
    private Account loggedInAccount = null;

    public void login(Account account) {
        this.loggedInAccount = account;
    }

    public void logout() {
        this.loggedInAccount = null;
    }

    public Account getLoggedInAccount() {
        return loggedInAccount;
    }

    public boolean isLoggedIn() {
        return loggedInAccount != null;
    }
}
