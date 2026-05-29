package com.issuetracker.domain.account.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    @DisplayName("계정 생성: username/password/isAdmin 초기화, accountId는 null")
    void createAccountInitializesFields() {
        Account account = new Account("dev1", "1234", false);

        assertEquals("dev1", account.getUsername());
        assertEquals("1234", account.getPassword());
        assertFalse(account.isAdmin());
        assertNull(account.getAccountId());
    }

    @Test
    @DisplayName("계정 ID 설정: setAccountId로 ID 저장")
    void setAccountIdUpdatesId() {
        Account account = new Account("dev1", "1234", false);

        account.setAccountId(42L);

        assertEquals(42L, account.getAccountId());
    }

    @Test
    @DisplayName("계정 필드 수정: setter로 username/password/isAdmin 변경")
    void settersUpdateFields() {
        Account account = new Account("dev1", "1234", false);

        account.setUsername("dev2");
        account.setPassword("5678");
        account.setAdmin(true);

        assertEquals("dev2", account.getUsername());
        assertEquals("5678", account.getPassword());
        assertTrue(account.isAdmin());
    }
}
