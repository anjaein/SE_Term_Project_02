package com.issuetracker.domain.project.service;

import com.issuetracker.domain.project.enums.Role;
import com.issuetracker.domain.project.entity.Project;
import com.issuetracker.domain.project.entity.ProjectMember;
import com.issuetracker.domain.project.repository.JsonProjectMemberRepository;
import com.issuetracker.domain.project.repository.JsonProjectRepository;
import com.issuetracker.domain.project.repository.ProjectMemberRepository;
import com.issuetracker.domain.project.repository.ProjectRepository;
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

class ProjectServiceTest {

    private static final Path PROJECTS_FILE = Path.of("data", "projects.json");
    private static final Path PROJECT_MEMBERS_FILE = Path.of("data", "project_members.json");

    private static final Long ADMIN_ID = 1L;
    private static final Long DEV_ID = 2L;

    private ProjectRepository projectRepository;
    private ProjectMemberRepository projectMemberRepository;
    private ProjectService projectService;

    private String originalProjectsJson;
    private String originalProjectMembersJson;

    @BeforeEach
    void setUp() throws IOException {
        originalProjectsJson = readOriginal(PROJECTS_FILE);
        originalProjectMembersJson = readOriginal(PROJECT_MEMBERS_FILE);
        resetJsonFile(PROJECTS_FILE);
        resetJsonFile(PROJECT_MEMBERS_FILE);

        projectRepository = new JsonProjectRepository();
        projectMemberRepository = new JsonProjectMemberRepository();
        projectService = new ProjectService(
                projectRepository,
                projectMemberRepository,
                new ProjectValidator(projectRepository)
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        restoreJsonFile(PROJECTS_FILE, originalProjectsJson);
        restoreJsonFile(PROJECT_MEMBERS_FILE, originalProjectMembersJson);
    }

    @Test
    @DisplayName("프로젝트 생성 성공: 생성자 멤버 추가")
    void createProjectSucceeds() {
        Response<Project> result = projectService.createProject("Project-A", ADMIN_ID, Role.ADMIN);

        assertTrue(result.isSuccess());
        Project created = result.getData();
        assertNotNull(created.getProjectId());
        List<ProjectMember> members = projectMemberRepository.findByProjectId(created.getProjectId());
        assertEquals(1, members.size());
        assertEquals(ADMIN_ID, members.get(0).getAccountId());
    }

    @Test
    @DisplayName("프로젝트 생성 실패: 필수 파라미터(name/accountId/role) 중 하나라도 null")
    void createProjectFailsWhenRequiredParamIsNull() {
        Response<Project> nullName = projectService.createProject(null, ADMIN_ID, Role.ADMIN);
        Response<Project> nullAccountId = projectService.createProject("Project-A", null, Role.ADMIN);
        Response<Project> nullRole = projectService.createProject("Project-A", ADMIN_ID, null);

        assertFalse(nullName.isSuccess());
        assertTrue(nullName.getMessage().contains("Required parameter is missing"));
        assertFalse(nullAccountId.isSuccess());
        assertTrue(nullAccountId.getMessage().contains("Required parameter is missing"));
        assertFalse(nullRole.isSuccess());
        assertTrue(nullRole.getMessage().contains("Required parameter is missing"));
    }

    @Test
    @DisplayName("프로젝트 생성 실패: name이 blank")
    void createProjectFailsWhenNameIsBlank() {
        Response<Project> result = projectService.createProject("   ", ADMIN_ID, Role.ADMIN);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("cannot be empty"));
    }

    @Test
    @DisplayName("프로젝트 멤버 추가 성공: 존재하는 프로젝트에 새 멤버 추가")
    void addProjectMemberSucceeds() {
        Project project = projectService.createProject("Project-A", ADMIN_ID, Role.ADMIN).getData();

        Response<ProjectMember> result =
                projectService.addProjectMember(project.getProjectId(), DEV_ID, Role.DEV);

        assertTrue(result.isSuccess());
        assertEquals(2, projectMemberRepository.findByProjectId(project.getProjectId()).size());
    }

    @Test
    @DisplayName("프로젝트 멤버 추가 실패: 필수 파라미터(projectId/accountId/role) 중 하나라도 null")
    void addProjectMemberFailsWhenRequiredParamIsNull() {
        Response<ProjectMember> nullProjectId = projectService.addProjectMember(null, DEV_ID, Role.DEV);
        Response<ProjectMember> nullAccountId = projectService.addProjectMember(1L, null, Role.DEV);
        Response<ProjectMember> nullRole = projectService.addProjectMember(1L, DEV_ID, null);

        assertFalse(nullProjectId.isSuccess());
        assertTrue(nullProjectId.getMessage().contains("Required parameter is missing"));
        assertFalse(nullAccountId.isSuccess());
        assertTrue(nullAccountId.getMessage().contains("Required parameter is missing"));
        assertFalse(nullRole.isSuccess());
        assertTrue(nullRole.getMessage().contains("Required parameter is missing"));
    }

    @Test
    @DisplayName("프로젝트 멤버 추가 실패: 존재하지 않는 프로젝트")
    void addProjectMemberFailsForNonExistentProject() {
        Response<ProjectMember> result =
                projectService.addProjectMember(999L, DEV_ID, Role.DEV);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Project does not exist"));
    }

    @Test
    @DisplayName("프로젝트 멤버 조회 성공: 등록된 멤버 목록 반환")
    void getProjectMembersReturnsMembers() {
        Project project = projectService.createProject("Project-A", ADMIN_ID, Role.ADMIN).getData();
        projectService.addProjectMember(project.getProjectId(), DEV_ID, Role.DEV);

        Response<List<ProjectMember>> result = projectService.getProjectMembers(project.getProjectId());

        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
    }

    private String readOriginal(Path path) throws IOException {
        return Files.exists(path) ? Files.readString(path, StandardCharsets.UTF_8) : null;
    }

    private void resetJsonFile(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, "[]", StandardCharsets.UTF_8);
    }

    private void restoreJsonFile(Path path, String originalJson) throws IOException {
        if (originalJson == null) {
            Files.deleteIfExists(path);
        } else {
            Files.writeString(path, originalJson, StandardCharsets.UTF_8);
        }
    }
}
