package com.issuetracker;

import com.issuetracker.domain.account.controller.AccountController;
import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.account.repository.AccountRepository;
import com.issuetracker.domain.account.repository.JsonAccountRepository;
import com.issuetracker.domain.account.service.AccountService;
import com.issuetracker.domain.account.service.AccountValidator;
import com.issuetracker.domain.comment.controller.CommentController;
import com.issuetracker.domain.comment.entity.Comment;
import com.issuetracker.domain.comment.repository.CommentRepository;
import com.issuetracker.domain.comment.repository.JsonCommentRepository;
import com.issuetracker.domain.comment.service.CommentService;
import com.issuetracker.domain.comment.service.CommentValidator;
import com.issuetracker.domain.issue.controller.IssueController;
import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.domain.issue.enums.Status;
import com.issuetracker.domain.issue.repository.IssueRepository;
import com.issuetracker.domain.issue.repository.JsonIssueRepository;
import com.issuetracker.domain.issue.service.IssueService;
import com.issuetracker.domain.issue.service.IssueValidator;
import com.issuetracker.domain.project.controller.ProjectController;
import com.issuetracker.domain.project.repository.JsonProjectMemberRepository;
import com.issuetracker.domain.project.repository.JsonProjectRepository;
import com.issuetracker.domain.project.repository.ProjectMemberRepository;
import com.issuetracker.domain.project.repository.ProjectRepository;
import com.issuetracker.domain.project.service.ProjectService;
import com.issuetracker.domain.project.service.ProjectValidator;
import com.issuetracker.global.common.SessionManager;
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

class IntegrationScenarioTest {

    private static final Path ACCOUNTS_FILE = Path.of("data", "accounts.json");
    private static final Path PROJECTS_FILE = Path.of("data", "projects.json");
    private static final Path PROJECT_MEMBERS_FILE = Path.of("data", "project_members.json");
    private static final Path ISSUES_FILE = Path.of("data", "issues.json");
    private static final Path COMMENTS_FILE = Path.of("data", "comments.json");
    private static final Path[] DATA_FILES = {
            ACCOUNTS_FILE, PROJECTS_FILE, PROJECT_MEMBERS_FILE, ISSUES_FILE, COMMENTS_FILE
    };

    private final String[] originalJson = new String[DATA_FILES.length];

    private AccountController accountController;
    private AccountService accountService;
    private ProjectController projectController;
    private IssueController issueController;
    private CommentController commentController;
    private IssueService issueService;
    private IssueRepository issueRepository;

    @BeforeEach
    void setUp() throws IOException {
        for (int i = 0; i < DATA_FILES.length; i++) {
            originalJson[i] = Files.exists(DATA_FILES[i])
                    ? Files.readString(DATA_FILES[i], StandardCharsets.UTF_8)
                    : null;
            Files.createDirectories(DATA_FILES[i].getParent());
            Files.writeString(DATA_FILES[i], "[]", StandardCharsets.UTF_8);
        }

        AccountRepository accountRepository = new JsonAccountRepository();
        ProjectRepository projectRepository = new JsonProjectRepository();
        ProjectMemberRepository projectMemberRepository = new JsonProjectMemberRepository();
        issueRepository = new JsonIssueRepository();
        CommentRepository commentRepository = new JsonCommentRepository();

        SessionManager sessionManager = new SessionManager();

        AccountValidator accountValidator = new AccountValidator(accountRepository);
        IssueValidator issueValidator = new IssueValidator(projectMemberRepository, projectRepository);
        CommentValidator commentValidator = new CommentValidator(issueRepository, projectMemberRepository);

        accountService = new AccountService(accountRepository, accountValidator);
        ProjectService projectService = new ProjectService(projectRepository, projectMemberRepository, new ProjectValidator(projectRepository));
        issueService = new IssueService(issueRepository, issueValidator);
        CommentService commentService = new CommentService(commentRepository, commentValidator);

        accountController = new AccountController(accountService, sessionManager);
        projectController = new ProjectController(projectService, accountService, sessionManager);
        issueController = new IssueController(issueService, sessionManager);
        commentController = new CommentController(commentService, sessionManager);
    }

    @AfterEach
    void tearDown() throws IOException {
        for (int i = 0; i < DATA_FILES.length; i++) {
            if (originalJson[i] == null) {
                Files.deleteIfExists(DATA_FILES[i]);
            } else {
                Files.writeString(DATA_FILES[i], originalJson[i], StandardCharsets.UTF_8);
            }
        }
    }

    @Test
    @DisplayName("과제 예제 시나리오 성공: admin 셋업 → tester 등록 → PL assign → dev fix → tester resolve → PL close, 단계별 검색·코멘트 포함")
    void fullScenario() {
        assertTrue(accountController.login("admin", "admin123").isSuccess());
        assertTrue(accountController.createAccount("pl1", "1234", Role.PL).isSuccess());
        assertTrue(accountController.createAccount("dev1", "1234", Role.DEV).isSuccess());
        assertTrue(accountController.createAccount("tester1", "1234", Role.TESTER).isSuccess());
        assertTrue(projectController.createProject("Project-A").isSuccess());
        Long projectId = 1L;
        assertTrue(projectController.addProjectMember(projectId, "pl1", Role.PL).isSuccess());
        assertTrue(projectController.addProjectMember(projectId, "dev1", Role.DEV).isSuccess());
        assertTrue(projectController.addProjectMember(projectId, "tester1", Role.TESTER).isSuccess());
        Long dev1Id = accountService.getAccountIdByUsername("dev1").getData();
        Long tester1Id = accountService.getAccountIdByUsername("tester1").getData();
        accountController.logout();

        assertTrue(accountController.login("tester1", "1234").isSuccess());
        Issue created = issueController.createIssue(projectId, "Login button error", "The login button does not respond.").getData();
        Long issueId = created.getIssueId();
        Issue afterCreate = issueRepository.findByIssueId(issueId);
        assertEquals(Status.NEW, afterCreate.getStatus());
        assertEquals(tester1Id, afterCreate.getReporterId());
        assertTrue(commentController.createComment(issueId, "first observation by tester1").isSuccess());
        accountController.logout();

        assertTrue(accountController.login("pl1", "1234").isSuccess());
        List<Issue> allIssues = issueService.getAllIssues().getData();
        assertTrue(allIssues.stream().anyMatch(i -> i.getIssueId().equals(issueId)));
        List<Issue> newIssues = issueService.getIssuesByStatus(Status.NEW).getData();
        assertTrue(newIssues.stream().anyMatch(i -> i.getIssueId().equals(issueId)));
        assertTrue(issueController.assignIssue(issueId, dev1Id).isSuccess());
        Issue afterAssign = issueRepository.findByIssueId(issueId);
        assertEquals(Status.ASSIGNED, afterAssign.getStatus());
        assertEquals(dev1Id, afterAssign.getAssigneeId());
        assertTrue(commentController.createComment(issueId, "assigning to dev1").isSuccess());
        accountController.logout();

        assertTrue(accountController.login("dev1", "1234").isSuccess());
        List<Issue> assignedToDev1 = issueService.getIssuesByAssigneeId(dev1Id).getData();
        assertTrue(assignedToDev1.stream().anyMatch(i -> i.getIssueId().equals(issueId)));
        assertTrue(commentController.createComment(issueId, "fix in commit abc").isSuccess());
        assertTrue(issueController.fixIssue(issueId).isSuccess());
        Issue afterFix = issueRepository.findByIssueId(issueId);
        assertEquals(Status.FIXED, afterFix.getStatus());
        assertEquals(dev1Id, afterFix.getFixerId());
        accountController.logout();

        assertTrue(accountController.login("tester1", "1234").isSuccess());
        List<Issue> reportedByTester1Fixed = issueService.getIssuesByReporterId(tester1Id).getData().stream()
                .filter(i -> i.getStatus() == Status.FIXED)
                .toList();
        assertTrue(reportedByTester1Fixed.stream().anyMatch(i -> i.getIssueId().equals(issueId)));
        assertTrue(issueController.resolveIssue(issueId).isSuccess());
        assertEquals(Status.RESOLVED, issueRepository.findByIssueId(issueId).getStatus());
        assertTrue(commentController.createComment(issueId, "verified by tester1").isSuccess());
        accountController.logout();

        assertTrue(accountController.login("pl1", "1234").isSuccess());
        assertTrue(issueController.closeIssue(issueId).isSuccess());
        assertEquals(Status.CLOSED, issueRepository.findByIssueId(issueId).getStatus());

        List<Comment> comments = commentController.listComments(issueId).getData();
        assertEquals(4, comments.size());
        assertEquals("first observation by tester1", comments.get(0).getContent());
        assertEquals("assigning to dev1", comments.get(1).getContent());
        assertEquals("fix in commit abc", comments.get(2).getContent());
        assertEquals("verified by tester1", comments.get(3).getContent());
        for (int i = 1; i < comments.size(); i++) {
            assertTrue(comments.get(i - 1).getCommentId() < comments.get(i).getCommentId());
        }
        accountController.logout();
    }
}
