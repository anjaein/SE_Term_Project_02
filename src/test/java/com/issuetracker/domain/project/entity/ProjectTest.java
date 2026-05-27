package com.issuetracker.domain.project.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProjectTest {

    @Test
    @DisplayName("프로젝트 생성 성공: projectId는 null이고 name/createdBy 저장")
    void constructorLeavesIdNullAndStoresFields() {
        Project project = new Project("Project-A", 1L);

        assertNull(project.getProjectId());
    }

    @Test
    @DisplayName("프로젝트 ID 부여 성공: setProjectId로 projectId 저장")
    void setProjectIdAssignsId() {
        Project project = new Project("Project-A", 1L);

        project.setProjectId(7L);

        assertEquals(7L, project.getProjectId());
    }
}
