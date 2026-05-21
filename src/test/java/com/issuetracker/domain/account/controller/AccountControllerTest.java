package com.issuetracker.domain.account.controller;

import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.account.repository.AccountRepository;
import com.issuetracker.domain.account.service.AccountService;
import com.issuetracker.global.common.SessionManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AccountControllerTest {

    @Test
    @DisplayName("로그인 성공 시 세션에 현재 계정을 저장한다")
    void loginStoresAccountInSessionWhenCredentialsMatch() {
        // 판단근거: Controller의 핵심 책임은 인증 결과를 SessionManager에 반영하는 것이므로 세션 상태 변화를 검증해야 한다.
        // given
        FakeAccountRepository accountRepository = new FakeAccountRepository();
        AccountService accountService = new AccountService(accountRepository);
        SessionManager sessionManager = new SessionManager();
        AccountController accountController = new AccountController(accountService, sessionManager);
        accountService.createAccount("dev1", "1234", Role.DEV);

        // when
        boolean result = accountController.login("dev1", "1234");

        // then
        assertTrue(result);
        assertTrue(sessionManager.isLoggedIn());
        assertEquals("dev1", sessionManager.getLoggedInAccount().getUsername());
    }

    @Test
    @DisplayName("로그인 실패 시 세션을 변경하지 않는다")
    void loginDoesNotStoreSessionWhenCredentialsDoNotMatch() {
        // 판단근거: 실패한 인증 요청이 기존/빈 세션을 로그인 상태로 바꾸지 않는지 확인해야 한다.
        // given
        FakeAccountRepository accountRepository = new FakeAccountRepository();
        AccountService accountService = new AccountService(accountRepository);
        SessionManager sessionManager = new SessionManager();
        AccountController accountController = new AccountController(accountService, sessionManager);
        accountService.createAccount("tester1", "1234", Role.TESTER);

        // when
        boolean result = accountController.login("tester1", "wrong-password");

        // then
        assertFalse(result);
        assertFalse(sessionManager.isLoggedIn());
    }

    @Test
    @DisplayName("로그아웃하면 세션의 현재 계정을 제거한다")
    void logoutClearsCurrentSession() {
        // 판단근거: 로그아웃 후에도 세션이 남아 있으면 권한이 유지될 수 있으므로 세션 제거를 검증해야 한다.
        // given
        FakeAccountRepository accountRepository = new FakeAccountRepository();
        AccountService accountService = new AccountService(accountRepository);
        SessionManager sessionManager = new SessionManager();
        AccountController accountController = new AccountController(accountService, sessionManager);
        accountController.login("admin", "admin123");

        // when
        accountController.logout();

        // then
        assertFalse(sessionManager.isLoggedIn());
        assertNull(sessionManager.getLoggedInAccount());
    }

    @Test
    @DisplayName("관리자로 로그인한 경우 계정을 생성할 수 있다")
    void adminCanCreateAccount() {
        // 판단근거: 계정 생성은 관리자 전용 기능이므로 ADMIN 권한에서 성공하는 경로를 검증해야 한다.
        // given
        FakeAccountRepository accountRepository = new FakeAccountRepository();
        AccountService accountService = new AccountService(accountRepository);
        SessionManager sessionManager = new SessionManager();
        AccountController accountController = new AccountController(accountService, sessionManager);
        accountController.login("admin", "admin123");

        // when
        accountController.createAccount("pl1", "1234", Role.PL);

        // then
        assertNotNull(accountService.login("pl1", "1234"));
        assertEquals(Role.PL, accountService.login("pl1", "1234").getRole());
    }

    @Test
    @DisplayName("관리자가 아닌 사용자는 계정을 생성할 수 없다")
    void nonAdminCannotCreateAccount() {
        // 판단근거: 역할 기반 접근 제어가 깨지면 일반 사용자가 임의 계정을 만들 수 있으므로 거부 경로를 검증해야 한다.
        // given
        FakeAccountRepository accountRepository = new FakeAccountRepository();
        AccountService accountService = new AccountService(accountRepository);
        SessionManager sessionManager = new SessionManager();
        AccountController accountController = new AccountController(accountService, sessionManager);
        accountService.createAccount("dev1", "1234", Role.DEV);
        accountController.login("dev1", "1234");

        // when
        accountController.createAccount("hacker", "pw", Role.ADMIN);

        // then
        assertNull(accountService.login("hacker", "pw"));
    }

    private static class FakeAccountRepository extends AccountRepository {
        private final List<Account> accounts = new ArrayList<>();
        private long nextId = 1L;

        @Override
        public List<Account> findAll() {
            return accounts;
        }

        @Override
        public Account findByUsername(String username) {
            return accounts.stream()
                    .filter(account -> account.getUsername().equals(username))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public Account findById(Long accountId) {
            return accounts.stream()
                    .filter(account -> account.getAccountId().equals(accountId))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public void save(Account account) {
            account.setAccountId(nextId++);
            accounts.add(account);
        }

        @Override
        public Long getAccountIdByUsername(String username) {
            return Optional.ofNullable(findByUsername(username))
                    .map(Account::getAccountId)
                    .orElse(null);
        }
    }
}
