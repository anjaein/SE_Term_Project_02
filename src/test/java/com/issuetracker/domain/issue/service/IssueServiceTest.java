package com.issuetracker.domain.issue.service;

import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.domain.issue.enums.Priority;
import com.issuetracker.domain.issue.enums.Status;
import com.issuetracker.domain.issue.repository.IssueRepository;
import com.issuetracker.domain.issue.repository.JsonIssueRepository;
import com.issuetracker.domain.project.entity.Project;
import com.issuetracker.domain.project.entity.ProjectMember;
import com.issuetracker.domain.project.repository.JsonProjectMemberRepository;
import com.issuetracker.domain.project.repository.JsonProjectRepository;
import com.issuetracker.domain.project.repository.ProjectMemberRepository;
import com.issuetracker.domain.project.repository.ProjectRepository;
import com.issuetracker.global.common.JsonFileManager;
import com.issuetracker.global.common.Response;
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

class IssueServiceTest {

    private static final Path PROJECTS_FILE = Path.of("data", "projects.json");
    private static final Path PROJECT_MEMBERS_FILE = Path.of("data", "project_members.json");
    private static final Path ISSUES_FILE = Path.of("data", "issues.json");

    private static final Long PROJECT_ID = 1L;
    private static final Long PL_ID = 10L;
    private static final Long DEV_ID = 20L;
    private static final Long TESTER_ID = 30L;
    private static final Long REPORTER_ID = TESTER_ID;

    private IssueRepository issueRepository;
    private ProjectMemberRepository projectMemberRepository;
    private ProjectRepository projectRepository;
    private IssueService issueService;

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

        projectRepository = new JsonProjectRepository();
        projectMemberRepository = new JsonProjectMemberRepository();
        issueRepository = new JsonIssueRepository();
        issueService = new IssueService(
                issueRepository,
                new IssueValidator(projectMemberRepository, projectRepository)
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        restoreJsonFile(PROJECTS_FILE, originalProjectsJson);
        restoreJsonFile(PROJECT_MEMBERS_FILE, originalProjectMembersJson);
        restoreJsonFile(ISSUES_FILE, originalIssuesJson);
    }

    @Test
    @DisplayName("이슈 생성 성공: 프로젝트 멤버가 유효한 입력으로 생성")
    void createIssueSucceeds() {
        seedProject(PROJECT_ID);
        seedProjectMember(PROJECT_ID, REPORTER_ID, Role.TESTER);

        Response<Issue> result = issueService.createIssue(PROJECT_ID, "title", "description", Priority.MAJOR, REPORTER_ID);

        assertTrue(result.isSuccess());
        Issue saved = result.getData();
        assertNotNull(saved.getIssueId());
        assertEquals(Status.NEW, saved.getStatus());
        assertEquals(Priority.MAJOR, saved.getPriority());
        assertEquals(REPORTER_ID, saved.getReporterId());
    }

    @Test
    @DisplayName("이슈 생성 실패: 필수 파라미터(projectId/title/description/priority/reporterId) 중 하나라도 null")
    void createIssueFailsWhenRequiredParamIsNull() {
        Response<Issue> nullProject = issueService.createIssue(null, "title", "desc", Priority.MAJOR, REPORTER_ID);
        Response<Issue> nullTitle = issueService.createIssue(PROJECT_ID, null, "desc", Priority.MAJOR, REPORTER_ID);
        Response<Issue> nullDesc = issueService.createIssue(PROJECT_ID, "title", null, Priority.MAJOR, REPORTER_ID);
        Response<Issue> nullPriority = issueService.createIssue(PROJECT_ID, "title", "desc", null, REPORTER_ID);
        Response<Issue> nullReporter = issueService.createIssue(PROJECT_ID, "title", "desc", Priority.MAJOR, null);

        assertFalse(nullProject.isSuccess());
        assertTrue(nullProject.getMessage().contains("Required parameter is missing"));
        assertFalse(nullTitle.isSuccess());
        assertTrue(nullTitle.getMessage().contains("Required parameter is missing"));
        assertFalse(nullDesc.isSuccess());
        assertTrue(nullDesc.getMessage().contains("Required parameter is missing"));
        assertFalse(nullPriority.isSuccess());
        assertTrue(nullPriority.getMessage().contains("Required parameter is missing"));
        assertFalse(nullReporter.isSuccess());
        assertTrue(nullReporter.getMessage().contains("Required parameter is missing"));
    }

    @Test
    @DisplayName("이슈 생성 실패: title이 blank")
    void createIssueFailsWhenTitleIsBlank() {
        Response<Issue> result = issueService.createIssue(PROJECT_ID, "   ", "desc", Priority.MAJOR, REPORTER_ID);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("title cannot be empty"));
    }

    @Test
    @DisplayName("이슈 생성 실패: description이 blank")
    void createIssueFailsWhenDescriptionIsBlank() {
        Response<Issue> result = issueService.createIssue(PROJECT_ID, "title", "   ", Priority.MAJOR, REPORTER_ID);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("description cannot be empty"));
    }

    @Test
    @DisplayName("이슈 생성 실패: project 존재하지 않음")
    void createIssueFailsWhenProjectDoesNotExist() {
        Response<Issue> result = issueService.createIssue(999L, "title", "desc", Priority.MAJOR, REPORTER_ID);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Project does not exist"));
    }

    @Test
    @DisplayName("이슈 생성 실패: reporter가 프로젝트 멤버 아님")
    void createIssueFailsWhenReporterIsNotMember() {
        seedProject(PROJECT_ID);

        Response<Issue> result = issueService.createIssue(PROJECT_ID, "title", "desc", Priority.MAJOR, REPORTER_ID);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("not a member"));
    }

    @Test
    @DisplayName("이슈 단건 조회 성공: 존재하는 issueId")
    void getIssueByIdSucceeds() {
        Issue issue = seedNewIssue();

        Response<Issue> result = issueService.getIssueById(issue.getIssueId());

        assertTrue(result.isSuccess());
        assertEquals(issue.getIssueId(), result.getData().getIssueId());
    }

    @Test
    @DisplayName("이슈 단건 조회 실패: issueId가 null")
    void getIssueByIdFailsWhenIdIsNull() {
        Response<Issue> result = issueService.getIssueById(null);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Required parameter is missing"));
    }

    @Test
    @DisplayName("이슈 단건 조회 실패: 존재하지 않는 issueId")
    void getIssueByIdFailsWhenIssueDoesNotExist() {
        Response<Issue> result = issueService.getIssueById(999L);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Issue not found"));
    }

    @Test
    @DisplayName("이슈 ASSIGN 성공: PL이 DEV에게 할당")
    void assignIssueSucceeds() {
        seedProjectMember(PROJECT_ID, PL_ID, Role.PL);
        seedProjectMember(PROJECT_ID, DEV_ID, Role.DEV);
        Issue issue = seedNewIssue();

        Response<Issue> result = issueService.assignIssue(issue.getIssueId(), DEV_ID, PL_ID);

        assertTrue(result.isSuccess());
        Issue updated = issueRepository.findByIssueId(issue.getIssueId());
        assertEquals(DEV_ID, updated.getAssigneeId());
        assertEquals(Status.ASSIGNED, updated.getStatus());
    }

    @Test
    @DisplayName("이슈 ASSIGN 실패: 필수 파라미터(issueId/assigneeId/requesterId) 중 하나라도 null")
    void assignIssueFailsWhenRequiredParamIsNull() {
        assertFalse(issueService.assignIssue(null, DEV_ID, PL_ID).isSuccess());
        assertFalse(issueService.assignIssue(1L, null, PL_ID).isSuccess());
        assertFalse(issueService.assignIssue(1L, DEV_ID, null).isSuccess());
    }

    @Test
    @DisplayName("이슈 ASSIGN 실패: 존재하지 않는 issue")
    void assignIssueFailsWhenIssueDoesNotExist() {
        Response<Issue> result = issueService.assignIssue(999L, DEV_ID, PL_ID);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Issue not found"));
    }

    @Test
    @DisplayName("이슈 ASSIGN 실패: 상태가 NEW가 아님")
    void assignIssueFailsWhenStatusIsNotNew() {
        seedProjectMember(PROJECT_ID, PL_ID, Role.PL);
        seedProjectMember(PROJECT_ID, DEV_ID, Role.DEV);
        Issue issue = seedAssignedIssue();

        Response<Issue> result = issueService.assignIssue(issue.getIssueId(), DEV_ID, PL_ID);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("not in NEW status"));
    }

    @Test
    @DisplayName("이슈 ASSIGN 실패: 요청자가 PL이 아님")
    void assignIssueFailsWhenRequesterIsNotPL() {
        seedProjectMember(PROJECT_ID, TESTER_ID, Role.TESTER);
        seedProjectMember(PROJECT_ID, DEV_ID, Role.DEV);
        Issue issue = seedNewIssue();

        Response<Issue> result = issueService.assignIssue(issue.getIssueId(), DEV_ID, TESTER_ID);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Only a PL can perform this action"));
    }

    @Test
    @DisplayName("이슈 ASSIGN 실패: assignee가 DEV가 아님")
    void assignIssueFailsWhenAssigneeIsNotDev() {
        seedProjectMember(PROJECT_ID, PL_ID, Role.PL);
        seedProjectMember(PROJECT_ID, TESTER_ID, Role.TESTER);
        Issue issue = seedNewIssue();

        Response<Issue> result = issueService.assignIssue(issue.getIssueId(), TESTER_ID, PL_ID);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("must be a DEV"));
    }

    @Test
    @DisplayName("이슈 FIX 성공: assignee가 FIX 처리")
    void fixIssueSucceeds() {
        Issue issue = seedAssignedIssue();

        Response<Issue> result = issueService.fixIssue(issue.getIssueId(), DEV_ID);

        assertTrue(result.isSuccess());
        Issue updated = issueRepository.findByIssueId(issue.getIssueId());
        assertEquals(Status.FIXED, updated.getStatus());
        assertEquals(DEV_ID, updated.getFixerId());
        assertNotNull(updated.getFixedDate());
    }

    @Test
    @DisplayName("이슈 FIX 실패: 필수 파라미터(issueId/requesterId) 중 하나라도 null")
    void fixIssueFailsWhenRequiredParamIsNull() {
        assertFalse(issueService.fixIssue(null, DEV_ID).isSuccess());
        assertFalse(issueService.fixIssue(1L, null).isSuccess());
    }

    @Test
    @DisplayName("이슈 FIX 실패: 존재하지 않는 issue")
    void fixIssueFailsWhenIssueDoesNotExist() {
        Response<Issue> result = issueService.fixIssue(999L, DEV_ID);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Issue not found"));
    }

    @Test
    @DisplayName("이슈 FIX 실패: 상태가 ASSIGNED가 아님")
    void fixIssueFailsWhenStatusIsNotAssigned() {
        Issue issue = seedNewIssue();

        Response<Issue> result = issueService.fixIssue(issue.getIssueId(), DEV_ID);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("not in ASSIGNED status"));
    }

    @Test
    @DisplayName("이슈 FIX 실패: 요청자가 assignee가 아님")
    void fixIssueFailsWhenRequesterIsNotAssignee() {
        Issue issue = seedAssignedIssue();

        Response<Issue> result = issueService.fixIssue(issue.getIssueId(), TESTER_ID);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Only the assignee"));
    }

    @Test
    @DisplayName("이슈 RESOLVE 성공: reporter가 RESOLVE 처리")
    void resolveIssueSucceeds() {
        Issue issue = seedFixedIssue();

        Response<Issue> result = issueService.resolveIssue(issue.getIssueId(), REPORTER_ID);

        assertTrue(result.isSuccess());
        Issue updated = issueRepository.findByIssueId(issue.getIssueId());
        assertEquals(Status.RESOLVED, updated.getStatus());
        assertNotNull(updated.getResolvedDate());
    }

    @Test
    @DisplayName("이슈 RESOLVE 실패: 필수 파라미터(issueId/requesterId) 중 하나라도 null")
    void resolveIssueFailsWhenRequiredParamIsNull() {
        assertFalse(issueService.resolveIssue(null, REPORTER_ID).isSuccess());
        assertFalse(issueService.resolveIssue(1L, null).isSuccess());
    }

    @Test
    @DisplayName("이슈 RESOLVE 실패: 존재하지 않는 issue")
    void resolveIssueFailsWhenIssueDoesNotExist() {
        Response<Issue> result = issueService.resolveIssue(999L, REPORTER_ID);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Issue not found"));
    }

    @Test
    @DisplayName("이슈 RESOLVE 실패: 상태가 FIXED가 아님")
    void resolveIssueFailsWhenStatusIsNotFixed() {
        Issue issue = seedNewIssue();

        Response<Issue> result = issueService.resolveIssue(issue.getIssueId(), REPORTER_ID);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("not in FIXED status"));
    }

    @Test
    @DisplayName("이슈 RESOLVE 실패: 요청자가 reporter가 아님")
    void resolveIssueFailsWhenRequesterIsNotReporter() {
        Issue issue = seedFixedIssue();

        Response<Issue> result = issueService.resolveIssue(issue.getIssueId(), PL_ID);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Only the reporter"));
    }

    @Test
    @DisplayName("이슈 CLOSE 성공: PL이 CLOSE 처리")
    void closeIssueSucceeds() {
        seedProjectMember(PROJECT_ID, PL_ID, Role.PL);
        Issue issue = seedResolvedIssue();

        Response<Issue> result = issueService.closeIssue(issue.getIssueId(), PL_ID);

        assertTrue(result.isSuccess());
        Issue updated = issueRepository.findByIssueId(issue.getIssueId());
        assertEquals(Status.CLOSED, updated.getStatus());
        assertNotNull(updated.getClosedDate());
    }

    @Test
    @DisplayName("이슈 CLOSE 실패: 필수 파라미터(issueId/requesterId) 중 하나라도 null")
    void closeIssueFailsWhenRequiredParamIsNull() {
        assertFalse(issueService.closeIssue(null, PL_ID).isSuccess());
        assertFalse(issueService.closeIssue(1L, null).isSuccess());
    }

    @Test
    @DisplayName("이슈 CLOSE 실패: 존재하지 않는 issue")
    void closeIssueFailsWhenIssueDoesNotExist() {
        Response<Issue> result = issueService.closeIssue(999L, PL_ID);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Issue not found"));
    }

    @Test
    @DisplayName("이슈 CLOSE 실패: 상태가 RESOLVED가 아님")
    void closeIssueFailsWhenStatusIsNotResolved() {
        seedProjectMember(PROJECT_ID, PL_ID, Role.PL);
        Issue issue = seedFixedIssue();

        Response<Issue> result = issueService.closeIssue(issue.getIssueId(), PL_ID);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("not in RESOLVED status"));
    }

    @Test
    @DisplayName("이슈 CLOSE 실패: 요청자가 PL이 아님")
    void closeIssueFailsWhenRequesterIsNotPL() {
        seedProjectMember(PROJECT_ID, TESTER_ID, Role.TESTER);
        Issue issue = seedResolvedIssue();

        Response<Issue> result = issueService.closeIssue(issue.getIssueId(), TESTER_ID);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Only a PL can perform this action"));
    }

    @Test
    @DisplayName("이슈 프로젝트별 조회 성공: projectId가 null이면 빈 리스트")
    void getIssuesByProjectIdReturnsEmptyWhenNull() {
        Response<List<Issue>> result = issueService.getIssuesByProjectId(null);

        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    @DisplayName("이슈 담당자별 조회 성공: assigneeId가 null이면 빈 리스트")
    void getIssuesByAssigneeIdReturnsEmptyWhenNull() {
        Response<List<Issue>> result = issueService.getIssuesByAssigneeId(null);

        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    @DisplayName("이슈 보고자별 조회 성공: reporterId가 null이면 빈 리스트")
    void getIssuesByReporterIdReturnsEmptyWhenNull() {
        Response<List<Issue>> result = issueService.getIssuesByReporterId(null);

        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    @DisplayName("이슈 상태별 조회 성공: status가 null이면 빈 리스트")
    void getIssuesByStatusReturnsEmptyWhenNull() {
        Response<List<Issue>> result = issueService.getIssuesByStatus(null);

        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    @DisplayName("이슈 우선순위별 조회 성공: priority가 null이면 빈 리스트")
    void getIssuesByPriorityReturnsEmptyWhenNull() {
        Response<List<Issue>> result = issueService.getIssuesByPriority(null);

        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    @DisplayName("이슈 전체 조회 성공: 저장된 이슈 반환")
    void getAllIssuesReturnsSavedIssues() {
        seedNewIssue();

        Response<List<Issue>> result = issueService.getAllIssues();

        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
    }

    private Issue seedNewIssue() {
        seedProject(PROJECT_ID);
        seedProjectMember(PROJECT_ID, REPORTER_ID, Role.TESTER);
        Issue issue = new Issue(PROJECT_ID, "title", "description", Priority.MAJOR, REPORTER_ID);
        issueRepository.save(issue);
        return issueRepository.findByIssueId(issue.getIssueId());
    }

    private Issue seedAssignedIssue() {
        Issue issue = seedNewIssue();
        issue.assignTo(DEV_ID);
        issueRepository.update(issue);
        return issueRepository.findByIssueId(issue.getIssueId());
    }

    private Issue seedFixedIssue() {
        Issue issue = seedAssignedIssue();
        issue.markAsFixed(DEV_ID);
        issueRepository.update(issue);
        return issueRepository.findByIssueId(issue.getIssueId());
    }

    private Issue seedResolvedIssue() {
        Issue issue = seedFixedIssue();
        issue.markAsResolved();
        issueRepository.update(issue);
        return issueRepository.findByIssueId(issue.getIssueId());
    }

    private void seedProject(Long projectId) {
        List<Project> projects = JsonFileManager.readList(PROJECTS_FILE.toString(),
                new com.google.gson.reflect.TypeToken<List<Project>>(){}.getType());
        if (projects == null) projects = new ArrayList<>();
        Project project = new Project("Project-" + projectId, 1L);
        project.setProjectId(projectId);
        projects.add(project);
        JsonFileManager.writeList(PROJECTS_FILE.toString(), projects);
    }

    private void seedProjectMember(Long projectId, Long accountId, Role role) {
        List<ProjectMember> members = JsonFileManager.readList(PROJECT_MEMBERS_FILE.toString(),
                new com.google.gson.reflect.TypeToken<List<ProjectMember>>(){}.getType());
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
