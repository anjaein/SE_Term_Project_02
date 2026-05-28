package com.issuetracker.domain.account.service;

import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.account.repository.AccountRepository;
import com.issuetracker.domain.account.repository.JsonAccountRepository;
import com.issuetracker.global.common.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AccountServiceTest {

    private static final Path ACCOUNTS_FILE = Path.of("data", "accounts.json");

    private AccountService accountService;
    private AccountRepository accountRepository;
    private String originalAccountsJson;

    @BeforeEach
    void setUp() throws IOException {
        // 원본 파일 백업
        originalAccountsJson = Files.exists(ACCOUNTS_FILE)
                ? Files.readString(ACCOUNTS_FILE, StandardCharsets.UTF_8)
                : null;
        // 파일 초기화 후 서비스 생성 → admin 자동 생성됨
        Files.createDirectories(ACCOUNTS_FILE.getParent());
        Files.writeString(ACCOUNTS_FILE, "[]", StandardCharsets.UTF_8);
        accountRepository = new JsonAccountRepository();
        AccountValidator accountValidator = new AccountValidator(accountRepository);
        accountService = new AccountService(accountRepository, accountValidator);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (originalAccountsJson == null) {
            Files.deleteIfExists(ACCOUNTS_FILE);
        } else {
            Files.writeString(ACCOUNTS_FILE, originalAccountsJson, StandardCharsets.UTF_8);
        }
    }

    // ─── 초기화 ──────────────────────────────────────────────

    @Test
    @DisplayName("서비스 초기화 시 admin 계정이 자동 생성된다")
    void adminAccountAutoCreatedOnInit() {
        Account admin = accountRepository.findByUsername("admin");
        assertNotNull(admin);
        assertEquals(Role.ADMIN, admin.getRole());
    }

    // ─── login ───────────────────────────────────────────────

    @Test
    @DisplayName("로그인 성공: 올바른 username/password")
    void loginSucceedsWithCorrectCredentials() {
        Response<Account> result = accountService.login("admin", "admin123");

        assertTrue(result.isSuccess());
        assertEquals("admin", result.getData().getUsername());
    }

    @Test
    @DisplayName("로그인 실패: 존재하지 않는 username")
    void loginFailsWithUnknownUsername() {
        Response<Account> result = accountService.login("nobody", "admin123");

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("로그인 실패: 틀린 password")
    void loginFailsWithWrongPassword() {
        Response<Account> result = accountService.login("admin", "wrongpassword");

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("로그인 실패: null 입력")
    void loginFailsWithNullInput() {
        assertFalse(accountService.login(null, "admin123").isSuccess());
        assertFalse(accountService.login("admin", null).isSuccess());
    }

    // ─── createAccount ────────────────────────────────────────

    @Test
    @DisplayName("계정 생성 성공: 유효한 입력")
    void createAccountSucceedsWithValidInput() {
        Response<Account> result = accountService.createAccount("dev1", "1234", Role.DEV);

        assertTrue(result.isSuccess());
        assertEquals("dev1", result.getData().getUsername());
        assertEquals(Role.DEV, result.getData().getRole());
        assertNotNull(accountRepository.findByUsername("dev1"));
    }

    @Test
    @DisplayName("계정 생성 실패: null 입력")
    void createAccountFailsWithNullInput() {
        assertFalse(accountService.createAccount(null, "1234", Role.DEV).isSuccess());
        assertFalse(accountService.createAccount("dev1", null, Role.DEV).isSuccess());
        assertFalse(accountService.createAccount("dev1", "1234", null).isSuccess());
    }

    @Test
    @DisplayName("계정 생성 실패: 빈 username")
    void createAccountFailsWithBlankUsername() {
        Response<Account> result = accountService.createAccount("   ", "1234", Role.DEV);

        assertFalse(result.isSuccess());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("계정 생성 실패: 빈 password")
    void createAccountFailsWithBlankPassword() {
        Response<Account> result = accountService.createAccount("dev1", "   ", Role.DEV);

        assertFalse(result.isSuccess());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("계정 생성 실패: 중복 username")
    void createAccountFailsWithDuplicateUsername() {
        accountService.createAccount("dev1", "1234", Role.DEV);

        Response<Account> result = accountService.createAccount("dev1", "5678", Role.DEV);

        assertFalse(result.isSuccess());
        assertEquals(1, accountRepository.findAll().stream()
                .filter(a -> a.getUsername().equals("dev1"))
                .count());
    }

    // ─── getAccountIdByUsername ───────────────────────────────

    @Test
    @DisplayName("getAccountIdByUsername 성공: 존재하는 username")
    void getAccountIdSucceedsForExistingUser() {
        Response<Long> result = accountService.getAccountIdByUsername("admin");

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
    }

    @Test
    @DisplayName("getAccountIdByUsername 실패: 존재하지 않는 username")
    void getAccountIdFailsForUnknownUser() {
        Response<Long> result = accountService.getAccountIdByUsername("nobody");

        assertFalse(result.isSuccess());
        assertNull(result.getData());
    }
}
