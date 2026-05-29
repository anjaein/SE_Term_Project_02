package com.issuetracker.domain.recommend.service;

import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.domain.issue.enums.Priority;
import com.issuetracker.domain.issue.enums.Status;
import com.issuetracker.domain.issue.repository.IssueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class RecommendServiceTest {
    private static final Long PROJECT_ID = 1L;
    private static final Long DEV_A = 10L;
    private static final Long DEV_B = 20L;
    private static final Long DEV_C = 30L;

    private FakeIssueRepository issueRepository;
    private RecommendService recommendService;

    @BeforeEach
    void setUp() {
        issueRepository = new FakeIssueRepository();
        recommendService = new RecommendService(issueRepository);
    }

    // ─── recommendAssignees ───────────────────────────────────

    @Test
    @DisplayName("추천 실패: 이력 없음")
    void returnsEmptyWhenNoHistory() {
        List<Long> result = recommendService.recommendAssignees(PROJECT_ID, "login bug", "button broken");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("추천 실패: null 입력")
    void returnsEmptyOnNullInput() {
        assertTrue(recommendService.recommendAssignees(null, "title", "desc").isEmpty());
        assertTrue(recommendService.recommendAssignees(PROJECT_ID, null, "desc").isEmpty());
        assertTrue(recommendService.recommendAssignees(PROJECT_ID, "title", null).isEmpty());
    }

    @Test
    @DisplayName("추천 성공: 일치 이슈 존재 시 fixer 반환")
    void recommendsFixerFromMatchingIssue() {
        addResolvedIssue(1L, "login bug", "button not responding", DEV_A);

        List<Long> result = recommendService.recommendAssignees(PROJECT_ID, "login error", "button broken");

        assertEquals(1, result.size());
        assertEquals(DEV_A, result.get(0));
    }

    @Test
    @DisplayName("추천 성공: 같은 fixer의 여러 이슈 스코어 합산 후 1위 반환")
    void aggregatesScoresForSameFixer() {
        // DEV_A가 유사한 이슈 2개, DEV_B가 1개
        addResolvedIssue(1L, "login bug", "button error", DEV_A);
        addClosedIssue(2L, "login crash", "button freeze", DEV_A);
        addResolvedIssue(3L, "database bug", "connection fail", DEV_B);

        List<Long> result = recommendService.recommendAssignees(PROJECT_ID, "login button crash", "button not responding");

        assertFalse(result.isEmpty());
        assertEquals(DEV_A, result.get(0));
    }

    @Test
    @DisplayName("추천 성공: 최대 3명 반환")
    void limitsToTopThreeRecommendations() {
        // 4명의 fixer가 각각 유사 이슈 보유
        addResolvedIssue(1L, "login bug", "button error", DEV_A);
        addResolvedIssue(2L, "login crash", "button freeze", DEV_B);
        addResolvedIssue(3L, "login timeout", "button slow", DEV_C);
        addResolvedIssue(4L, "login error", "button broken", 40L);

        List<Long> result = recommendService.recommendAssignees(PROJECT_ID, "login bug", "button error");

        assertTrue(result.size() <= 3);
    }

    @Test
    @DisplayName("추천 성공: 점수 높은 순으로 정렬")
    void returnsSortedByScoreDescending() {
        // DEV_B가 더 유사한 이슈 보유
        addResolvedIssue(1L, "login bug", "button error minor", DEV_A);
        addResolvedIssue(2L, "login bug", "button error critical crash", DEV_B);

        List<Long> result = recommendService.recommendAssignees(PROJECT_ID, "login bug", "button error critical");

        assertFalse(result.isEmpty());
        assertEquals(DEV_B, result.get(0));
    }

    @Test
    @DisplayName("추천 제외: RESOLVED/CLOSED 아닌 이슈는 포함되지 않음 (NEW 상태)")
    void ignoresNonResolvedIssues() {
        // given: NEW 상태(fixerId 없음) 이슈만 존재
        Issue issue = new Issue(PROJECT_ID, "login bug", "button error", Priority.MAJOR, 99L);
        issue.setIssueId(1L);
        issueRepository.save(issue);

        List<Long> result = recommendService.recommendAssignees(PROJECT_ID, "login bug", "button error");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("추천 제외: FIXED 상태(RESOLVED/CLOSED 아님) 이슈는 포함되지 않음")
    void ignoresFixedStatusIssues() {
        // given: FIXED 상태 이슈 (RESOLVED/CLOSED 아님)
        Issue issue = new Issue(PROJECT_ID, "login bug", "button error", Priority.MAJOR, 99L);
        issue.setIssueId(1L);
        issue.markAsFixed(DEV_A); // status = FIXED
        issueRepository.save(issue);

        List<Long> result = recommendService.recommendAssignees(PROJECT_ID, "login bug", "button error");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("추천 제외: 다른 프로젝트의 이슈는 포함되지 않음")
    void excludesIssuesFromOtherProjects() {
        // given: 다른 프로젝트(PROJECT_ID + 1)에만 일치 이슈 존재
        Long otherProjectId = PROJECT_ID + 1;
        Issue issue = new Issue(otherProjectId, "login bug", "button error", Priority.MAJOR, 99L);
        issue.setIssueId(1L);
        issue.markAsFixed(DEV_A);
        issue.markAsResolved();
        issueRepository.save(issue);

        List<Long> result = recommendService.recommendAssignees(PROJECT_ID, "login bug", "button error");

        assertTrue(result.isEmpty());
    }

    // ─── jaccardSimilarity ────────────────────────────────────

    @Test
    @DisplayName("Jaccard 유사도: 교집합/합집합 정확도")
    void jaccardSimilarityIsCorrect() {
        Set<String> a = Set.of("login", "button", "error");
        Set<String> b = Set.of("login", "button", "crash");

        double sim = recommendService.jaccardSimilarity(a, b);

        // 교집합={login,button}=2, 합집합={login,button,error,crash}=4
        assertEquals(2.0 / 4.0, sim, 0.0001);
    }

    @Test
    @DisplayName("Jaccard 유사도: 한쪽 집합이 빈 경우 0 반환")
    void jaccardSimilarityReturnsZeroForOneEmptySet() {
        assertEquals(0.0, recommendService.jaccardSimilarity(Set.of(), Set.of("login")));
        assertEquals(0.0, recommendService.jaccardSimilarity(Set.of("login"), Set.of()));
    }

    @Test
    @DisplayName("Jaccard 유사도: 두 집합 모두 빈 경우 0 반환")
    void jaccardSimilarityReturnsZeroForBothEmptySets() {
        assertEquals(0.0, recommendService.jaccardSimilarity(Set.of(), Set.of()));
    }

    // ─── tokenize ────────────────────────────────────────────

    @Test
    @DisplayName("토크나이징: 소문자 변환 및 1자 단어 필터")
    void tokenizeConvertsToLowercaseAndFiltersShortWords() {
        Set<String> tokens = recommendService.tokenize("Login Button ERROR a");

        assertTrue(tokens.contains("login"));
        assertTrue(tokens.contains("button"));
        assertTrue(tokens.contains("error"));
        assertFalse(tokens.contains("a"));
        assertFalse(tokens.contains("Login"));
    }

    @Test
    @DisplayName("토크나이징: null 입력 시 빈 집합 반환")
    void tokenizeReturnsEmptySetForNull() {
        assertTrue(recommendService.tokenize(null).isEmpty());
    }

    @Test
    @DisplayName("토크나이징: 빈 문자열 입력 시 빈 집합 반환")
    void tokenizeReturnsEmptySetForEmptyString() {
        assertTrue(recommendService.tokenize("").isEmpty());
    }

    // ─── helpers ─────────────────────────────────────────────

    private void addResolvedIssue(Long id, String title, String desc, Long fixerId) {
        Issue issue = new Issue(PROJECT_ID, title, desc, Priority.MAJOR, 99L);
        issue.setIssueId(id);
        issue.markAsFixed(fixerId);
        issue.markAsResolved();
        issueRepository.save(issue);
    }

    private void addClosedIssue(Long id, String title, String desc, Long fixerId) {
        Issue issue = new Issue(PROJECT_ID, title, desc, Priority.MAJOR, 99L);
        issue.setIssueId(id);
        issue.markAsFixed(fixerId);
        issue.markAsResolved();
        issue.markAsClosed();
        issueRepository.save(issue);
    }

    private static class FakeIssueRepository implements IssueRepository {
        private final List<Issue> issues = new ArrayList<>();

        @Override
        public List<Issue> findAll() {
            return issues;
        }

        @Override
        public List<Issue> findByProjectId(Long projectId) {
            return issues.stream()
                    .filter(i -> i.getProjectId().equals(projectId))
                    .collect(Collectors.toList());
        }

        @Override
        public boolean save(Issue issue) {
            issues.add(issue);
            return true;
        }

        @Override
        public boolean update(Issue issue) { return false; }

        @Override
        public List<Issue> findByAssigneeId(Long assigneeId) { return List.of(); }

        @Override
        public List<Issue> findByReporterId(Long reporterId) { return List.of(); }

        @Override
        public List<Issue> findByStatus(Status status) { return List.of(); }

        @Override
        public List<Issue> findByPriority(Priority priority) { return List.of(); }

        @Override
        public Issue findByIssueId(Long issueId) { return null; }
    }
}
