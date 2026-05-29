package com.issuetracker.domain.account.controller;

import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.repository.AccountRepository;
import com.issuetracker.domain.account.repository.JsonAccountRepository;
import com.issuetracker.domain.account.service.AccountService;
import com.issuetracker.domain.account.service.AccountValidator;
import com.issuetracker.global.common.Response;
import com.issuetracker.global.common.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AccountControllerTest {

    private static final Path ACCOUNTS_FILE = Path.of("data", "accounts.json");

    private AccountRepository accountRepository;
    private SessionManager sessionManager;
    private AccountController accountController;

    private String originalAccountsJson;

    @BeforeEach
    void setUp() throws IOException {
        originalAccountsJson = Files.exists(ACCOUNTS_FILE)
                ? Files.readString(ACCOUNTS_FILE, StandardCharsets.UTF_8)
                : null;
        Files.createDirectories(ACCOUNTS_FILE.getParent());
        Files.writeString(ACCOUNTS_FILE, "[]", StandardCharsets.UTF_8);

        accountRepository = new JsonAccountRepository();
        AccountValidator accountValidator = new AccountValidator(accountRepository);
        AccountService accountService = new AccountService(accountRepository, accountValidator);

        sessionManager = new SessionManager();
        accountController = new AccountController(accountService, sessionManager);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (originalAccountsJson == null) {
            Files.deleteIfExists(ACCOUNTS_FILE);
        } else {
            Files.writeString(ACCOUNTS_FILE, originalAccountsJson, StandardCharsets.UTF_8);
        }
    }

    // ─── login ───────────────────────────────────────────────

    @Test
    @DisplayName("로그인 성공: 세션에 계정이 등록됨")
    void loginSucceedsSetsSession() {
        Response<Account> result = accountController.login("admin", "admin123");

        assertTrue(result.isSuccess());
        assertNotNull(sessionManager.getLoggedInAccount());
        assertEquals("admin", sessionManager.getLoggedInAccount().getUsername());
    }

    @Test
    @DisplayName("로그인 실패: 잘못된 비밀번호 → 세션 등록 안 됨")
    void loginFailsDoesNotSetSession() {
        Response<Account> result = accountController.login("admin", "wrongpassword");

        assertFalse(result.isSuccess());
        assertNull(sessionManager.getLoggedInAccount());
    }

    @Test
    @DisplayName("로그인 실패: 존재하지 않는 username")
    void loginFailsWithUnknownUsername() {
        Response<Account> result = accountController.login("nobody", "1234");

        assertFalse(result.isSuccess());
        assertNull(sessionManager.getLoggedInAccount());
    }

    // ─── logout ──────────────────────────────────────────────

    @Test
    @DisplayName("로그아웃 성공: 세션이 초기화됨")
    void logoutClearsSession() {
        accountController.login("admin", "admin123");

        Response<Void> result = accountController.logout();

        assertTrue(result.isSuccess());
        assertNull(sessionManager.getLoggedInAccount());
    }

    // ─── createAccount ────────────────────────────────────────

    @Test
    @DisplayName("계정 생성 성공: admin이 새 계정 생성")
    void createAccountSucceedsByAdmin() {
        accountController.login("admin", "admin123");

        Response<Account> result = accountController.createAccount("dev1", "1234", false);

        assertTrue(result.isSuccess());
        assertEquals("dev1", result.getData().getUsername());
        assertFalse(result.getData().isAdmin());
        assertNotNull(accountRepository.findByUsername("dev1"));
    }

    @Test
    @DisplayName("계정 생성 실패: 비로그인 상태")
    void createAccountFailsWhenNotLoggedIn() {
        Response<Account> result = accountController.createAccount("dev1", "1234", false);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("not logged in"));
    }

    @Test
    @DisplayName("계정 생성 실패: admin이 아닌 사용자")
    void createAccountFailsForNonAdmin() {
        accountController.login("admin", "admin123");
        accountController.createAccount("dev1", "1234", false);
        accountController.logout();
        accountController.login("dev1", "1234");

        Response<Account> result = accountController.createAccount("dev2", "5678", false);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Only admin"));
    }

    @Test
    @DisplayName("계정 생성 실패: 중복 username")
    void createAccountFailsWithDuplicateUsername() {
        accountController.login("admin", "admin123");
        accountController.createAccount("dev1", "1234", false);

        Response<Account> result = accountController.createAccount("dev1", "5678", false);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Username already exists"));
    }
}
