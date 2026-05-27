package com.issuetracker.domain.project.controller;

import com.issuetracker.domain.account.controller.AccountController;
import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.account.repository.AccountRepository;
import com.issuetracker.domain.account.repository.JsonAccountRepository;
import com.issuetracker.domain.account.service.AccountService;
import com.issuetracker.domain.account.service.AccountValidator;
import com.issuetracker.domain.project.entity.Project;
import com.issuetracker.domain.project.entity.ProjectMember;
import com.issuetracker.domain.project.repository.JsonProjectMemberRepository;
import com.issuetracker.domain.project.repository.JsonProjectRepository;
import com.issuetracker.domain.project.repository.ProjectMemberRepository;
import com.issuetracker.domain.project.repository.ProjectRepository;
import com.issuetracker.domain.project.service.ProjectService;
import com.issuetracker.domain.project.service.ProjectValidator;
import com.issuetracker.global.common.Response;
import com.issuetracker.global.common.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectControllerTest {

    private static final Path ACCOUNTS_FILE = Path.of("data", "accounts.json");
    private static final Path PROJECTS_FILE = Path.of("data", "projects.json");
    private static final Path PROJECT_MEMBERS_FILE = Path.of("data", "project_members.json");

    private AccountRepository accountRepository;
    private SessionManager sessionManager;
    private AccountController accountController;
    private ProjectController projectController;

    private String originalAccountsJson;
    private String originalProjectsJson;
    private String originalProjectMembersJson;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() throws IOException {
        originalAccountsJson = readOriginal(ACCOUNTS_FILE);
        originalProjectsJson = readOriginal(PROJECTS_FILE);
        originalProjectMembersJson = readOriginal(PROJECT_MEMBERS_FILE);
        resetJsonFile(ACCOUNTS_FILE);
        resetJsonFile(PROJECTS_FILE);
        resetJsonFile(PROJECT_MEMBERS_FILE);

        originalOut = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream()));

        accountRepository = new JsonAccountRepository();
        AccountValidator accountValidator = new AccountValidator(accountRepository);
        AccountService accountService = new AccountService(accountRepository, accountValidator);
        sessionManager = new SessionManager();
        accountController = new AccountController(accountService, sessionManager);

        ProjectRepository projectRepository = new JsonProjectRepository();
        ProjectMemberRepository projectMemberRepository = new JsonProjectMemberRepository();
        ProjectService projectService = new ProjectService(
                projectRepository,
                projectMemberRepository,
                new ProjectValidator(projectRepository)
        );
        projectController = new ProjectController(projectService, accountService, sessionManager);
        sessionManager.logout();
    }

    @AfterEach
    void tearDown() throws IOException {
        System.setOut(originalOut);
        restoreJsonFile(ACCOUNTS_FILE, originalAccountsJson);
        restoreJsonFile(PROJECTS_FILE, originalProjectsJson);
        restoreJsonFile(PROJECT_MEMBERS_FILE, originalProjectMembersJson);
    }

    @Test
    @DisplayName("프로젝트 생성 성공: ADMIN 로그인 후 유효한 이름으로 생성")
    void createProjectSucceedsForAdmin() {
        loginAs(Role.ADMIN);

        Response<Project> result = projectController.createProject("Project-A");

        assertTrue(result.isSuccess());
        assertNotNull(result.getData().getProjectId());
    }

    @Test
    @DisplayName("프로젝트 생성 실패: 비로그인 상태")
    void createProjectFailsWhenNotLoggedIn() {
        Response<Project> result = projectController.createProject("Project-A");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("not logged in"));
    }

    @Test
    @DisplayName("프로젝트 생성 실패: ADMIN이 아닌 사용자")
    void createProjectFailsForNonAdmin() {
        loginAs(Role.DEV);

        Response<Project> result = projectController.createProject("Project-A");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("not allowed"));
    }

    @Test
    @DisplayName("프로젝트 멤버 추가 성공: ADMIN 로그인 후 기존 사용자 추가")
    void addProjectMemberSucceedsForAdmin() {
        accountController.login("admin", "admin123");
        accountController.createAccount("dev1", "1234", Role.DEV);
        Long projectId = projectController.createProject("Project-A").getData().getProjectId();

        Response<ProjectMember> result =
                projectController.addProjectMember(projectId, "dev1", Role.DEV);

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("프로젝트 멤버 추가 실패: 비로그인 상태")
    void addProjectMemberFailsWhenNotLoggedIn() {
        Response<ProjectMember> result =
                projectController.addProjectMember(1L, "dev1", Role.DEV);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("not logged in"));
    }

    @Test
    @DisplayName("프로젝트 멤버 추가 실패: ADMIN이 아닌 사용자")
    void addProjectMemberFailsForNonAdmin() {
        loginAs(Role.PL);

        Response<ProjectMember> result =
                projectController.addProjectMember(1L, "dev1", Role.DEV);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("not allowed"));
    }

    @Test
    @DisplayName("프로젝트 멤버 추가 실패: 존재하지 않는 사용자")
    void addProjectMemberFailsForUnknownUser() {
        accountController.login("admin", "admin123");
        Long projectId = projectController.createProject("Project-A").getData().getProjectId();

        Response<ProjectMember> result =
                projectController.addProjectMember(projectId, "ghost", Role.DEV);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("does not exist"));
    }

    @Test
    @DisplayName("프로젝트 멤버 조회 성공: 로그인 상태에서 멤버 목록 반환")
    void listProjectMembersSucceeds() {
        accountController.login("admin", "admin123");
        Long projectId = projectController.createProject("Project-A").getData().getProjectId();

        Response<List<ProjectMember>> result = projectController.listProjectMembers(projectId);

        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
    }

    @Test
    @DisplayName("프로젝트 멤버 조회 실패: 비로그인 상태")
    void listProjectMembersFailsWhenNotLoggedIn() {
        Response<List<ProjectMember>> result = projectController.listProjectMembers(1L);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("not logged in"));
    }

    private void loginAs(Role role) {
        Account account = new Account("sessionUser", "pw", role);
        account.setAccountId(99L);
        sessionManager.login(account);
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
