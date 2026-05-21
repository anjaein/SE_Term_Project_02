package com.issuetracker.domain.account.repository;

import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.enums.Role;
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

    private final AccountRepository accountRepository = new AccountRepository();

    private String originalAccountsJson;

    @BeforeEach
    void setUp() throws IOException {
        originalAccountsJson = Files.exists(ACCOUNTS_FILE)
                ? Files.readString(ACCOUNTS_FILE, StandardCharsets.UTF_8)
                : null;

        Files.createDirectories(ACCOUNTS_FILE.getParent());
        Files.writeString(ACCOUNTS_FILE, "[]", StandardCharsets.UTF_8);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (originalAccountsJson == null) {
            Files.deleteIfExists(ACCOUNTS_FILE);
        } else {
            Files.writeString(ACCOUNTS_FILE, originalAccountsJson, StandardCharsets.UTF_8);
        }
    }

    @Test
    @DisplayName("계정을 저장하면 ID가 순차적으로 부여되고 저장소에 유지된다")
    void saveAssignsNextIdAndPersistsAccount() {
        // 판단근거: AccountRepository는 계정 ID 생성 책임을 가지므로 순차 ID 부여와 파일 저장 결과를 검증해야 한다.
        // given
        Account first = new Account("admin", "admin123", Role.ADMIN);
        Account second = new Account("dev1", "1234", Role.DEV);

        // when
        accountRepository.save(first);
        accountRepository.save(second);
        List<Account> accounts = accountRepository.findAll();

        // then
        assertEquals(2, accounts.size());
        assertEquals(1L, accounts.get(0).getAccountId());
        assertEquals(2L, accounts.get(1).getAccountId());
        assertEquals("admin", accounts.get(0).getUsername());
        assertEquals("dev1", accounts.get(1).getUsername());
    }

    @Test
    @DisplayName("username으로 계정을 조회하면 일치하는 계정만 반환한다")
    void findByUsernameReturnsMatchingAccount() {
        // 판단근거: 로그인은 username 조회에 의존하므로 정확한 계정 조회가 보장되어야 한다.
        // given
        accountRepository.save(new Account("tester1", "1234", Role.TESTER));
        accountRepository.save(new Account("dev1", "1234", Role.DEV));

        // when
        Account account = accountRepository.findByUsername("dev1");

        // then
        assertNotNull(account);
        assertEquals("dev1", account.getUsername());
        assertEquals(Role.DEV, account.getRole());
    }

    @Test
    @DisplayName("존재하지 않는 username으로 조회하면 null을 반환한다")
    void findByUsernameReturnsNullWhenAccountDoesNotExist() {
        // 판단근거: 로그인 실패 판단을 위해 저장소가 없는 username을 null로 표현하는 계약을 검증한다.
        // given
        accountRepository.save(new Account("tester1", "1234", Role.TESTER));

        // when
        Account account = accountRepository.findByUsername("missing");

        // then
        assertNull(account);
    }

    @Test
    @DisplayName("accountId로 계정을 조회하면 일치하는 계정을 반환한다")
    void findByIdReturnsMatchingAccount() {
        // 판단근거: 추천, 댓글, 프로젝트 멤버 등 다른 도메인이 accountId로 계정을 찾으므로 ID 조회를 검증해야 한다.
        // given
        accountRepository.save(new Account("pl1", "1234", Role.PL));
        accountRepository.save(new Account("dev1", "1234", Role.DEV));

        // when
        Account account = accountRepository.findById(2L);

        // then
        assertNotNull(account);
        assertEquals("dev1", account.getUsername());
        assertEquals(Role.DEV, account.getRole());
    }

    @Test
    @DisplayName("username으로 accountId를 조회하면 해당 계정 ID를 반환한다")
    void getAccountIdByUsernameReturnsMatchingId() {
        // 판단근거: UI와 프로젝트 멤버 추가 흐름에서 username을 accountId로 변환하므로 변환 결과를 검증해야 한다.
        // given
        accountRepository.save(new Account("pl1", "1234", Role.PL));
        accountRepository.save(new Account("tester1", "1234", Role.TESTER));

        // when
        Long accountId = accountRepository.getAccountIdByUsername("tester1");

        // then
        assertEquals(2L, accountId);
    }

    @Test
    @DisplayName("존재하지 않는 username의 accountId를 조회하면 null을 반환한다")
    void getAccountIdByUsernameReturnsNullWhenAccountDoesNotExist() {
        // 판단근거: 존재하지 않는 사용자를 다른 도메인에 잘못 연결하지 않도록 null 반환 계약을 검증한다.
        // given
        accountRepository.save(new Account("pl1", "1234", Role.PL));

        // when
        Long accountId = accountRepository.getAccountIdByUsername("missing");

        // then
        assertNull(accountId);
    }
}
