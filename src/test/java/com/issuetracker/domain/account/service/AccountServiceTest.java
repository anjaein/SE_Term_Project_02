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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountServiceTest {

    private static final Path ACCOUNTS_FILE = Path.of("data", "accounts.json");

    private AccountService accountService;
    private AccountRepository accountRepository;
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
        assertEquals(Role.ADMIN, result.getData().getRole());
    }

    @Test
    @DisplayName("로그인 실패: 존재하지 않는 username")
    void loginFailsWithUnknownUsername() {
        Response<Account> result = accountService.login("nobody", "admin123");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Invalid username or password"));
    }

    @Test
    @DisplayName("로그인 실패: 틀린 password")
    void loginFailsWithWrongPassword() {
        Response<Account> result = accountService.login("admin", "wrongpassword");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Invalid username or password"));
    }

    @Test
    @DisplayName("로그인 실패: null 입력")
    void loginFailsWithNullInput() {
        Response<Account> nullUsername = accountService.login(null, "admin123");
        Response<Account> nullPassword = accountService.login("admin", null);

        assertFalse(nullUsername.isSuccess());
        assertTrue(nullUsername.getMessage().contains("Required parameter is missing"));
        assertFalse(nullPassword.isSuccess());
        assertTrue(nullPassword.getMessage().contains("Required parameter is missing"));
    }

    // ─── createAccount ────────────────────────────────────────

    @Test
    @DisplayName("계정 생성 성공: 유효한 입력")
    void createAccountSucceedsWithValidInput() {
        Response<Account> result = accountService.createAccount("dev1", "1234", Role.DEV);

        assertTrue(result.isSuccess());
        assertEquals("dev1", result.getData().getUsername());
        assertEquals(Role.DEV, result.getData().getRole());
        assertNotNull(result.getData().getAccountId());
        assertNotNull(accountRepository.findByUsername("dev1"));
    }

    @Test
    @DisplayName("계정 생성 실패: 필수 파라미터(username/password/role) 중 하나라도 null")
    void createAccountFailsWithNullInput() {
        Response<Account> nullUsername = accountService.createAccount(null, "1234", Role.DEV);
        Response<Account> nullPassword = accountService.createAccount("dev1", null, Role.DEV);
        Response<Account> nullRole = accountService.createAccount("dev1", "1234", null);

        assertFalse(nullUsername.isSuccess());
        assertTrue(nullUsername.getMessage().contains("Required parameter is missing"));
        assertFalse(nullPassword.isSuccess());
        assertTrue(nullPassword.getMessage().contains("Required parameter is missing"));
        assertFalse(nullRole.isSuccess());
        assertTrue(nullRole.getMessage().contains("Required parameter is missing"));
    }

    @Test
    @DisplayName("계정 생성 실패: 빈 username")
    void createAccountFailsWithBlankUsername() {
        Response<Account> result = accountService.createAccount("   ", "1234", Role.DEV);

        assertFalse(result.isSuccess());
        assertNull(result.getData());
        assertTrue(result.getMessage().contains("cannot be empty"));
    }

    @Test
    @DisplayName("계정 생성 실패: 빈 password")
    void createAccountFailsWithBlankPassword() {
        Response<Account> result = accountService.createAccount("dev1", "   ", Role.DEV);

        assertFalse(result.isSuccess());
        assertNull(result.getData());
        assertTrue(result.getMessage().contains("cannot be empty"));
    }

    @Test
    @DisplayName("계정 생성 실패: 중복 username")
    void createAccountFailsWithDuplicateUsername() {
        accountService.createAccount("dev1", "1234", Role.DEV);

        Response<Account> result = accountService.createAccount("dev1", "5678", Role.DEV);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Username already exists"));
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
        assertTrue(result.getMessage().contains("Account not found"));
    }

    // ─── getAllAccounts ───────────────────────────────────────

    @Test
    @DisplayName("전체 계정 조회 성공: 초기화 후 admin 계정만 반환")
    void getAllAccountsReturnsAdminOnInit() {
        Response<List<Account>> result = accountService.getAllAccounts();

        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertEquals("admin", result.getData().get(0).getUsername());
    }

    @Test
    @DisplayName("전체 계정 조회 성공: 추가 계정 생성 후 전체 반환")
    void getAllAccountsReturnsAllCreatedAccounts() {
        accountService.createAccount("dev1", "1234", Role.DEV);
        accountService.createAccount("tester1", "5678", Role.TESTER);

        Response<List<Account>> result = accountService.getAllAccounts();

        assertTrue(result.isSuccess());
        assertEquals(3, result.getData().size()); // admin + dev1 + tester1
        assertTrue(result.getData().stream().anyMatch(a -> a.getUsername().equals("dev1")));
        assertTrue(result.getData().stream().anyMatch(a -> a.getUsername().equals("tester1")));
    }
}
