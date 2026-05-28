package com.issuetracker.domain.account.entity;

import com.issuetracker.domain.account.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    @DisplayName("계정 생성: username/password/role 초기화, accountId는 null")
    void createAccountInitializesFields() {
        Account account = new Account("dev1", "1234", Role.DEV);

        assertEquals("dev1", account.getUsername());
        assertEquals("1234", account.getPassword());
        assertEquals(Role.DEV, account.getRole());
        assertNull(account.getAccountId());
    }

    @Test
    @DisplayName("계정 ID 설정: setAccountId로 ID 저장")
    void setAccountIdUpdatesId() {
        Account account = new Account("dev1", "1234", Role.DEV);

        account.setAccountId(42L);

        assertEquals(42L, account.getAccountId());
    }

    @Test
    @DisplayName("계정 필드 수정: setter로 username/password/role 변경")
    void settersUpdateFields() {
        Account account = new Account("dev1", "1234", Role.DEV);

        account.setUsername("dev2");
        account.setPassword("5678");
        account.setRole(Role.TESTER);

        assertEquals("dev2", account.getUsername());
        assertEquals("5678", account.getPassword());
        assertEquals(Role.TESTER, account.getRole());
    }
}
