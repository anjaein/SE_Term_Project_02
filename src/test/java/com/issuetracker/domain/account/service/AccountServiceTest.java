package com.issuetracker.domain.account.service;

import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.account.repository.AccountRepository;
import com.issuetracker.global.common.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AccountServiceTest {

    @Test
    @DisplayName("저장된 계정이 없으면 최초 관리자 계정을 자동 생성한다")
    void createsInitialAdminWhenRepositoryIsEmpty() {
        // 판단근거: 시스템 최초 실행 시 관리자 계정이 없으면 계정 관리 기능을 시작할 수 없으므로 초기 admin 생성이 보장되어야 한다.
        // given
        FakeAccountRepository accountRepository = new FakeAccountRepository();
        AccountValidator accountValidator = new AccountValidator(accountRepository);

        // when
        AccountService accountService = new AccountService(accountRepository, accountValidator);

        // then
        Response<Account> adminResp = accountService.login("admin", "admin123");
        assertTrue(adminResp.isSuccess());
        Account admin = adminResp.getData();
        assertNotNull(admin);
        assertEquals(Role.ADMIN, admin.getRole());
        assertEquals(1L, admin.getAccountId());
    }

    @Test
    @DisplayName("아이디와 비밀번호가 일치하면 로그인한 계정을 반환한다")
    void loginReturnsAccountWhenCredentialsMatch() {
        // 판단근거: 로그인 성공은 이후 권한 검사와 세션 생성의 출발점이므로 정상 인증 결과를 검증해야 한다.
        // given
        FakeAccountRepository accountRepository = new FakeAccountRepository();
        AccountValidator accountValidator = new AccountValidator(accountRepository);
        AccountService accountService = new AccountService(accountRepository, accountValidator);
        accountService.createAccount("dev1", "1234", Role.DEV);

        // when
        Response<Account> result = accountService.login("dev1", "1234");

        // then
        assertTrue(result.isSuccess());
        assertEquals("dev1", result.getData().getUsername());
        assertEquals(Role.DEV, result.getData().getRole());
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 로그인에 실패한다")
    void loginReturnsNullWhenPasswordDoesNotMatch() {
        // 판단근거: 잘못된 비밀번호를 허용하면 모든 역할 기반 기능이 우회될 수 있으므로 실패 경로를 고정해야 한다.
        // given
        FakeAccountRepository accountRepository = new FakeAccountRepository();
        AccountValidator accountValidator = new AccountValidator(accountRepository);
        AccountService accountService = new AccountService(accountRepository, accountValidator);
        accountService.createAccount("tester1", "1234", Role.TESTER);

        // when
        Response<Account> result = accountService.login("tester1", "wrong-password");

        // then
        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("이미 존재하는 username으로 계정을 생성하면 추가 저장하지 않는다")
    void createAccountDoesNotSaveDuplicateUsername() {
        // 판단근거: username은 로그인 식별자로 사용되므로 중복 저장을 막아야 어떤 계정으로 인증되는지 모호해지지 않는다.
        // given
        FakeAccountRepository accountRepository = new FakeAccountRepository();
        AccountValidator accountValidator = new AccountValidator(accountRepository);
        AccountService accountService = new AccountService(accountRepository, accountValidator);
        accountService.createAccount("dev1", "1234", Role.DEV);
        int beforeCount = accountService.getAllAccounts().getData().size();

        // when
        accountService.createAccount("dev1", "5678", Role.TESTER);

        // then
        assertEquals(beforeCount, accountService.getAllAccounts().getData().size());
        assertTrue(accountService.login("dev1", "1234").isSuccess());
        assertEquals(Role.DEV, accountService.login("dev1", "1234").getData().getRole());
        assertFalse(accountService.login("dev1", "5678").isSuccess());
    }

    @Test
    @DisplayName("username으로 계정 ID를 조회한다")
    void getAccountIdByUsernameReturnsMatchingAccountId() {
        // 판단근거: 프로젝트 멤버 추가 등 다른 도메인이 username을 accountId로 변환해 사용하므로 조회 계약을 검증해야 한다.
        // given
        FakeAccountRepository accountRepository = new FakeAccountRepository();
        AccountValidator accountValidator = new AccountValidator(accountRepository);
        AccountService accountService = new AccountService(accountRepository, accountValidator);
        accountService.createAccount("pl1", "1234", Role.PL);

        // when
        Response<Long> accountIdResp = accountService.getAccountIdByUsername("pl1");

        // then
        assertTrue(accountIdResp.isSuccess());
        Long accountId = accountIdResp.getData();
        assertNotNull(accountId);
        assertEquals(accountService.login("pl1", "1234").getData().getAccountId(), accountId);
    }

    private static class FakeAccountRepository implements AccountRepository {
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
        public boolean save(Account account) {
            account.setAccountId(nextId++);
            accounts.add(account);
            return true;
        }

        @Override
        public Long getAccountIdByUsername(String username) {
            return Optional.ofNullable(findByUsername(username))
                    .map(Account::getAccountId)
                    .orElse(null);
        }
    }
}
