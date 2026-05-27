package com.issuetracker.domain.issue.controller;

import com.google.gson.reflect.TypeToken;
import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.domain.issue.enums.Priority;
import com.issuetracker.domain.issue.repository.IssueRepository;
import com.issuetracker.domain.issue.repository.JsonIssueRepository;
import com.issuetracker.domain.issue.service.IssueService;
import com.issuetracker.domain.issue.service.IssueValidator;
import com.issuetracker.domain.project.entity.Project;
import com.issuetracker.domain.project.entity.ProjectMember;
import com.issuetracker.domain.project.repository.JsonProjectMemberRepository;
import com.issuetracker.domain.project.repository.JsonProjectRepository;
import com.issuetracker.domain.project.repository.ProjectMemberRepository;
import com.issuetracker.domain.project.repository.ProjectRepository;
import com.issuetracker.global.common.JsonFileManager;
import com.issuetracker.global.common.Response;
import com.issuetracker.global.common.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IssueControllerTest {

    private static final Path PROJECTS_FILE = Path.of("data", "projects.json");
    private static final Path PROJECT_MEMBERS_FILE = Path.of("data", "project_members.json");
    private static final Path ISSUES_FILE = Path.of("data", "issues.json");

    private static final Long PROJECT_ID = 1L;
    private static final Long PL_ID = 10L;
    private static final Long DEV_ID = 20L;
    private static final Long TESTER_ID = 30L;

    private IssueRepository issueRepository;
    private SessionManager sessionManager;
    private IssueController issueController;

    private String originalProjectsJson;
    private String originalProjectMembersJson;
    private String originalIssuesJson;

    @BeforeEach
    void setUp() throws IOException {
        originalProjectsJson = readOriginal(PROJECTS_FILE);
        originalProjectMembersJson = readOriginal(PROJECT_MEMBERS_FILE);
        originalIssuesJson = readOriginal(ISSUES_FILE);
        resetJsonFile(PROJECTS_FILE);
        resetJsonFile(PROJECT_MEMBERS_FILE);
        resetJsonFile(ISSUES_FILE);

        ProjectRepository projectRepository = new JsonProjectRepository();
        ProjectMemberRepository projectMemberRepository = new JsonProjectMemberRepository();
        issueRepository = new JsonIssueRepository();
        IssueValidator issueValidator = new IssueValidator(projectMemberRepository, projectRepository);
        IssueService issueService = new IssueService(issueRepository, issueValidator);

        sessionManager = new SessionManager();
        issueController = new IssueController(issueService, sessionManager);

        seedProject(PROJECT_ID);
        seedProjectMember(PROJECT_ID, PL_ID, Role.PL);
        seedProjectMember(PROJECT_ID, DEV_ID, Role.DEV);
        seedProjectMember(PROJECT_ID, TESTER_ID, Role.TESTER);

        sessionManager.logout();
    }

    @AfterEach
    void tearDown() throws IOException {
        restoreJsonFile(PROJECTS_FILE, originalProjectsJson);
        restoreJsonFile(PROJECT_MEMBERS_FILE, originalProjectMembersJson);
        restoreJsonFile(ISSUES_FILE, originalIssuesJson);
    }

    @Test
    @DisplayName("이슈 생성 성공: tester1이 이슈를 생성하면 reporter는 tester1, 상태는 NEW")
    void createIssueSucceedsByTester() {
        loginAs(TESTER_ID, Role.TESTER);

        Response<Issue> result = issueController.createIssue(PROJECT_ID, "title", "desc", Priority.MAJOR);

        assertTrue(result.isSuccess());
        assertEquals(TESTER_ID, result.getData().getReporterId());
    }

    @Test
    @DisplayName("이슈 생성 실패: 비로그인 상태")
    void createIssueFailsWhenNotLoggedIn() {
        Response<Issue> result = issueController.createIssue(PROJECT_ID, "title", "desc", Priority.MAJOR);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("not logged in"));
    }

    @Test
    @DisplayName("이슈 상세 조회 성공: PL1이 tester1이 만든 이슈를 브라우즈")
    void getIssueDetailSucceeds() {
        Issue issue = createNewIssueAsTester();

        loginAs(PL_ID, Role.PL);
        Response<Issue> result = issueController.getIssueDetail(issue.getIssueId());

        assertTrue(result.isSuccess());
        assertEquals(issue.getIssueId(), result.getData().getIssueId());
        assertEquals(TESTER_ID, result.getData().getReporterId());
    }

    @Test
    @DisplayName("이슈 상세 조회 실패: 비로그인 상태")
    void getIssueDetailFailsWhenNotLoggedIn() {
        Response<Issue> result = issueController.getIssueDetail(1L);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("not logged in"));
    }

    @Test
    @DisplayName("이슈 ASSIGN 성공: PL1이 tester1의 이슈에 dev1을 assignee로 지정")
    void assignIssueSucceeds() {
        Issue issue = createNewIssueAsTester();

        loginAs(PL_ID, Role.PL);
        Response<Issue> result = issueController.assignIssue(issue.getIssueId(), DEV_ID);

        assertTrue(result.isSuccess());
        assertEquals(DEV_ID, result.getData().getAssigneeId());
    }

    @Test
    @DisplayName("이슈 ASSIGN 실패: 비로그인 상태")
    void assignIssueFailsWhenNotLoggedIn() {
        Response<Issue> result = issueController.assignIssue(1L, DEV_ID);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("not logged in"));
    }

    @Test
    @DisplayName("이슈 FIX 성공: assignee인 dev1이 이슈를 fixed로 변경하고 fixer로 등록")
    void fixIssueSucceeds() {
        Issue issue = createNewIssueAsTester();
        loginAs(PL_ID, Role.PL);
        issueController.assignIssue(issue.getIssueId(), DEV_ID);

        loginAs(DEV_ID, Role.DEV);
        Response<Issue> result = issueController.fixIssue(issue.getIssueId());

        assertTrue(result.isSuccess());
        assertEquals(DEV_ID, result.getData().getFixerId());
    }

    @Test
    @DisplayName("이슈 FIX 실패: 비로그인 상태")
    void fixIssueFailsWhenNotLoggedIn() {
        Response<Issue> result = issueController.fixIssue(1L);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("not logged in"));
    }

    @Test
    @DisplayName("이슈 RESOLVE 성공: reporter인 tester1이 fixed된 이슈를 resolved로 변경")
    void resolveIssueSucceeds() {
        Issue issue = createNewIssueAsTester();
        loginAs(PL_ID, Role.PL);
        issueController.assignIssue(issue.getIssueId(), DEV_ID);
        loginAs(DEV_ID, Role.DEV);
        issueController.fixIssue(issue.getIssueId());

        loginAs(TESTER_ID, Role.TESTER);
        Response<Issue> result = issueController.resolveIssue(issue.getIssueId());

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("이슈 RESOLVE 실패: 비로그인 상태")
    void resolveIssueFailsWhenNotLoggedIn() {
        Response<Issue> result = issueController.resolveIssue(1L);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("not logged in"));
    }

    @Test
    @DisplayName("이슈 CLOSE 성공: PL1이 resolved 상태의 이슈를 closed로 변경")
    void closeIssueSucceeds() {
        Issue issue = createNewIssueAsTester();
        loginAs(PL_ID, Role.PL);
        issueController.assignIssue(issue.getIssueId(), DEV_ID);
        loginAs(DEV_ID, Role.DEV);
        issueController.fixIssue(issue.getIssueId());
        loginAs(TESTER_ID, Role.TESTER);
        issueController.resolveIssue(issue.getIssueId());

        loginAs(PL_ID, Role.PL);
        Response<Issue> result = issueController.closeIssue(issue.getIssueId());

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("이슈 CLOSE 실패: 비로그인 상태")
    void closeIssueFailsWhenNotLoggedIn() {
        Response<Issue> result = issueController.closeIssue(1L);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("not logged in"));
    }

    private void loginAs(Long accountId, Role role) {
        Account account = new Account("user" + accountId, "pw", role);
        account.setAccountId(accountId);
        sessionManager.login(account);
    }

    private Issue createNewIssueAsTester() {
        loginAs(TESTER_ID, Role.TESTER);
        Response<Issue> created = issueController.createIssue(PROJECT_ID, "title", "desc", Priority.MAJOR);
        assertTrue(created.isSuccess(), "fixture failed: " + created.getMessage());
        sessionManager.logout();
        return created.getData();
    }

    private void seedProject(Long projectId) {
        List<Project> projects = JsonFileManager.readList(PROJECTS_FILE.toString(),
                new TypeToken<List<Project>>(){}.getType());
        if (projects == null) projects = new ArrayList<>();
        Project project = new Project("Project-" + projectId, 1L);
        project.setProjectId(projectId);
        projects.add(project);
        JsonFileManager.writeList(PROJECTS_FILE.toString(), projects);
    }

    private void seedProjectMember(Long projectId, Long accountId, Role role) {
        List<ProjectMember> members = JsonFileManager.readList(PROJECT_MEMBERS_FILE.toString(),
                new TypeToken<List<ProjectMember>>(){}.getType());
        if (members == null) members = new ArrayList<>();
        members.add(new ProjectMember(projectId, accountId, role));
        JsonFileManager.writeList(PROJECT_MEMBERS_FILE.toString(), members);
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
