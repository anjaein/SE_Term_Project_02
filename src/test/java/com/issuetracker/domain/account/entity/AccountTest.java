package com.issuetracker.domain.account.entity;

import com.issuetracker.domain.account.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    @DisplayName("Account 생성 시 username, password, role이 초기화되고 accountId는 아직 없다")
    void constructorSetsUsernamePasswordAndRole() {
        // 판단근거: Account는 인증과 권한 판단의 기본 데이터이므로 생성 직후 필드 상태가 정확해야 한다.
        // given
        String username = "dev1";
        String password = "1234";
        Role role = Role.DEV;

        // when
        Account account = new Account(username, password, role);

        // then
        assertNull(account.getAccountId());
        assertEquals(username, account.getUsername());
        assertEquals(password, account.getPassword());
        assertEquals(role, account.getRole());
    }

    @Test
    @DisplayName("Account의 ID와 기본 필드는 setter로 변경할 수 있다")
    void settersUpdateAccountFields() {
        // 판단근거: Repository가 ID를 부여하고 관리자 기능이 계정 정보를 다룰 수 있으므로 setter 동작을 검증한다.
        // given
        Account account = new Account("old", "old-password", Role.TESTER);

        // when
        account.setAccountId(10L);
        account.setUsername("new");
        account.setPassword("new-password");
        account.setRole(Role.PL);

        // then
        assertEquals(10L, account.getAccountId());
        assertEquals("new", account.getUsername());
        assertEquals("new-password", account.getPassword());
        assertEquals(Role.PL, account.getRole());
    }
}
