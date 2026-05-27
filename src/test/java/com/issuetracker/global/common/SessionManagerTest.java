package com.issuetracker.global.common;

import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionManagerTest {

    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = new SessionManager();
    }

    @Test
    @DisplayName("초기 상태: 로그인되지 않았고 계정이 null이다")
    void initiallyNotLoggedIn() {
        // then
        assertFalse(sessionManager.isLoggedIn());
        assertNull(sessionManager.getLoggedInAccount());
    }

    @Test
    @DisplayName("login: 로그인하면 해당 계정이 세션에 등록된다")
    void loginRegistersAccount() {
        // given
        Account account = new Account("dev1", "pw", Role.DEV);

        // when
        sessionManager.login(account);

        // then
        assertTrue(sessionManager.isLoggedIn());
        assertSame(account, sessionManager.getLoggedInAccount());
    }

    @Test
    @DisplayName("logout: 로그아웃하면 초기 상태로 돌아간다")
    void logoutClearsSession() {
        // given
        sessionManager.login(new Account("dev1", "pw", Role.DEV));

        // when
        sessionManager.logout();

        // then
        assertFalse(sessionManager.isLoggedIn());
        assertNull(sessionManager.getLoggedInAccount());
    }

    @Test
    @DisplayName("재로그인: 새 계정으로 로그인하면 세션이 수정된다")
    void loginReplacesPreviousAccount() {
        // given
        sessionManager.login(new Account("dev1", "pw", Role.DEV));
        Account second = new Account("admin", "pw", Role.ADMIN);

        // when
        sessionManager.login(second);

        // then
        assertSame(second, sessionManager.getLoggedInAccount());
        assertEquals("admin", sessionManager.getLoggedInAccount().getUsername());
    }
}
