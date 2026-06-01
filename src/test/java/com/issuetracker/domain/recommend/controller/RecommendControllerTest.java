package com.issuetracker.domain.recommend.controller;

import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.repository.AccountRepository;
import com.issuetracker.domain.account.repository.JsonAccountRepository;
import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.domain.issue.enums.Priority;
import com.issuetracker.domain.issue.repository.IssueRepository;
import com.issuetracker.domain.issue.repository.JsonIssueRepository;
import com.issuetracker.domain.recommend.service.RecommendService;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecommendControllerTest {

    private static final Path ACCOUNTS_FILE = Path.of("data", "accounts.json");
    private static final Path ISSUES_FILE = Path.of("data", "issues.json");

    private static final Long PROJECT_ID = 1L;

    private IssueRepository issueRepository;
    private AccountRepository accountRepository;
    private SessionManager sessionManager;
    private RecommendController recommendController;

    private String originalAccountsJson;
    private String originalIssuesJson;

    @BeforeEach
    void setUp() throws IOException {
        originalAccountsJson = readOriginal(ACCOUNTS_FILE);
        originalIssuesJson = readOriginal(ISSUES_FILE);
        resetJsonFile(ACCOUNTS_FILE);
        resetJsonFile(ISSUES_FILE);

        issueRepository = new JsonIssueRepository();
        accountRepository = new JsonAccountRepository();
        sessionManager = new SessionManager();
        RecommendService recommendService = new RecommendService(issueRepository);
        recommendController = new RecommendController(recommendService, accountRepository, sessionManager);

        Account admin = new Account("admin", "1234", true);
        accountRepository.save(admin);
        sessionManager.login(accountRepository.findByUsername("admin"));
    }

    @AfterEach
    void tearDown() throws IOException {
        restoreJsonFile(ACCOUNTS_FILE, originalAccountsJson);
        restoreJsonFile(ISSUES_FILE, originalIssuesJson);
    }

    @Test
    @DisplayName("추천 성공: 이슈 이력 있는 fixer 계정 반환")
    void getRecommendedAssigneesReturnsMatchingAccounts() {
        accountRepository.save(new Account("dev1", "1234", false));
        Long devId = accountRepository.findByUsername("dev1").getAccountId();

        addResolvedIssue("login bug", "button error", devId);

        Response<List<Account>> response = recommendController.getRecommendedAssignees(PROJECT_ID, "login bug", "button error");

        assertTrue(response.isSuccess());
        assertEquals(1, response.getData().size());
        assertEquals("dev1", response.getData().get(0).getUsername());
    }

    @Test
    @DisplayName("추천 결과: 이력 없으면 빈 리스트 반환")
    void getRecommendedAssigneesReturnsEmptyWhenNoHistory() {
        Response<List<Account>> response = recommendController.getRecommendedAssignees(PROJECT_ID, "login bug", "button error");

        assertTrue(response.isSuccess());
        assertTrue(response.getData().isEmpty());
    }

    @Test
    @DisplayName("추천 결과: fixerId가 계정 저장소에 없으면 필터링")
    void getRecommendedAssigneesFiltersNonExistentAccounts() {
        addResolvedIssue("login bug", "button error", 999L);

        Response<List<Account>> response = recommendController.getRecommendedAssignees(PROJECT_ID, "login bug", "button error");

        assertTrue(response.isSuccess());
        assertTrue(response.getData().isEmpty());
    }

    @Test
    @DisplayName("추천 결과: 최대 3명까지만 반환")
    void getRecommendedAssigneesLimitsToThree() {
        for (int i = 1; i <= 4; i++) {
            accountRepository.save(new Account("dev" + i, "1234", false));
            Long devId = accountRepository.findByUsername("dev" + i).getAccountId();
            addResolvedIssue("login bug", "button error", devId);
        }

        Response<List<Account>> response = recommendController.getRecommendedAssignees(PROJECT_ID, "login bug", "button error");

        assertTrue(response.isSuccess());
        assertTrue(response.getData().size() <= 3);
    }

    @Test
    @DisplayName("추천 성공: 여러 fixer 중 유사도 높은 계정이 먼저 반환")
    void getRecommendedAssigneesReturnsMostSimilarFirst() {
        accountRepository.save(new Account("dev1", "1234", false));
        accountRepository.save(new Account("dev2", "1234", false));
        Long devId1 = accountRepository.findByUsername("dev1").getAccountId();
        Long devId2 = accountRepository.findByUsername("dev2").getAccountId();

        addResolvedIssue("login bug", "button error minor", devId1);
        addResolvedIssue("login bug", "button error critical crash", devId2);

        Response<List<Account>> response = recommendController.getRecommendedAssignees(PROJECT_ID, "login bug", "button error critical");

        assertTrue(response.isSuccess());
        assertFalse(response.getData().isEmpty());
        assertEquals("dev2", response.getData().get(0).getUsername());
    }

    @Test
    @DisplayName("비로그인 상태에서 추천 요청 시 실패 반환")
    void getRecommendedAssigneesFailsWhenNotLoggedIn() {
        sessionManager.logout();

        Response<List<Account>> response = recommendController.getRecommendedAssignees(PROJECT_ID, "login bug", "button error");

        assertFalse(response.isSuccess());
        assertEquals("You are not logged in.", response.getMessage());
    }

    private void addResolvedIssue(String title, String desc, Long fixerId) {
        Issue issue = new Issue(PROJECT_ID, title, desc, Priority.MAJOR, 99L);
        issueRepository.save(issue);
        issue = issueRepository.findByIssueId(issue.getIssueId());
        issue.markAsFixed(fixerId);
        issue.markAsResolved();
        issueRepository.update(issue);
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
