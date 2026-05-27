package com.issuetracker.domain.project.entity;

import com.issuetracker.domain.account.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class ProjectMemberTest {

    @Test
    @DisplayName("ProjectMember 생성 시 projectId, accountId, role이 저장")
    void constructorSetsProjectAccountAndRole() {
        //when
        ProjectMember member = new ProjectMember(1L, 10L, Role.PL);

        //then
        assertEquals(1L, member.getProjectId());
        assertEquals(10L, member.getAccountId());
        assertEquals(Role.PL, member.getRole());
    }
}