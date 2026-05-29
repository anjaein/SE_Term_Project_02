package com.issuetracker.domain.account.repository;

import com.issuetracker.domain.account.entity.Account;
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

class AccountRepositoryTest {

    private static final Path ACCOUNTS_FILE = Path.of("data", "accounts.json");

    private AccountRepository accountRepository;
    private String originalJson;

    @BeforeEach
    void setUp() throws IOException {
        originalJson = Files.exists(ACCOUNTS_FILE)
                ? Files.readString(ACCOUNTS_FILE, StandardCharsets.UTF_8)
                : null;
        Files.createDirectories(ACCOUNTS_FILE.getParent());
        Files.writeString(ACCOUNTS_FILE, "[]", StandardCharsets.UTF_8);
        accountRepository = new JsonAccountRepository();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (originalJson == null) {
            Files.deleteIfExists(ACCOUNTS_FILE);
        } else {
            Files.writeString(ACCOUNTS_FILE, originalJson, StandardCharsets.UTF_8);
        }
    }

    @Test
    @DisplayName("계정 저장 성공: ID가 순차 부여되고 저장소에 유지")
    void saveAssignsSequentialIds() {
        assertTrue(accountRepository.save(new Account("dev1", "1234", false)));
        assertTrue(accountRepository.save(new Account("dev2", "5678", false)));

        List<Account> accounts = accountRepository.findAll();
        assertEquals(2, accounts.size());
        assertEquals(1L, accounts.get(0).getAccountId());
        assertEquals(2L, accounts.get(1).getAccountId());
    }

    @Test
    @DisplayName("username으로 계정 조회 성공: 저장된 계정 반환")
    void findByUsernameReturnsAccount() {
        accountRepository.save(new Account("dev1", "1234", false));

        Account found = accountRepository.findByUsername("dev1");

        assertNotNull(found);
        assertEquals("dev1", found.getUsername());
        assertFalse(found.isAdmin());
    }

    @Test
    @DisplayName("username으로 계정 조회 실패: 존재하지 않는 username → null 반환")
    void findByUsernameReturnsNullForUnknown() {
        Account found = accountRepository.findByUsername("nobody");

        assertNull(found);
    }

    @Test
    @DisplayName("ID로 계정 조회 성공: 저장된 계정 반환")
    void findByIdReturnsAccount() {
        accountRepository.save(new Account("dev1", "1234", false));
        Long savedId = accountRepository.findByUsername("dev1").getAccountId();

        Account found = accountRepository.findById(savedId);

        assertNotNull(found);
        assertEquals(savedId, found.getAccountId());
        assertEquals("dev1", found.getUsername());
    }

    @Test
    @DisplayName("ID로 계정 조회 실패: 존재하지 않는 ID → null 반환")
    void findByIdReturnsNullForUnknown() {
        Account found = accountRepository.findById(999L);

        assertNull(found);
    }

    @Test
    @DisplayName("username으로 ID 조회 성공: 저장된 계정의 ID 반환")
    void getAccountIdByUsernameReturnsId() {
        accountRepository.save(new Account("dev1", "1234", false));

        Long id = accountRepository.getAccountIdByUsername("dev1");

        assertNotNull(id);
    }

    @Test
    @DisplayName("username으로 ID 조회 실패: 존재하지 않는 username → null 반환")
    void getAccountIdByUsernameReturnsNullForUnknown() {
        Long id = accountRepository.getAccountIdByUsername("nobody");

        assertNull(id);
    }

    @Test
    @DisplayName("전체 계정 조회 성공: 저장된 모든 계정 반환")
    void findAllReturnsAllAccounts() {
        accountRepository.save(new Account("dev1", "1234", false));
        accountRepository.save(new Account("tester1", "5678", false));

        List<Account> all = accountRepository.findAll();

        assertEquals(2, all.size());
    }
}
