package com.issuetracker.domain.recommend.service;

import com.issuetracker.domain.issue.entity.Issue;
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
        issueRepository = new FakeIssueRepository(); //실험용 저장소
        recommendService = new RecommendService(issueRepository); //테스트용 서비스
    }

    @Test
    @DisplayName("추천 실패: 이력 없음")
    void returnsEmptyWhenNoHistory() {
        // when
        List<Long> result = recommendService.recommendAssignees(PROJECT_ID, "login bug", "button broken");

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("추천 실패: null 입력")
    void returnsEmptyOnNullInput() {
        // when & then
        assertTrue(recommendService.recommendAssignees(null, "title", "desc").isEmpty());
        assertTrue(recommendService.recommendAssignees(PROJECT_ID, null, "desc").isEmpty());
        assertTrue(recommendService.recommendAssignees(PROJECT_ID, "title", null).isEmpty());
    }

    @Test
    @DisplayName("추천 성공: 일치 이슈 존재 시 fixer 반환")
    void recommendsFixerFromMatchingIssue() {
        // given
        addResolvedIssue(1L, "login bug", "button not responding", DEV_A);

        // when
        List<Long> result = recommendService.recommendAssignees(PROJECT_ID, "login error", "button broken");

        // then
        assertEquals(1, result.size());
        assertEquals(DEV_A, result.get(0));
    }

    @Test
    @DisplayName("추천 성공: 같은 fixer의 여러 이슈 스코어 합산 후 1위 반환")
    void aggregatesScoresForSameFixer() {
        // given: DEV_A가 유사한 이슈 2개, DEV_B가 1개
        addResolvedIssue(1L, "login bug", "button error", DEV_A);
        addClosedIssue(2L, "login crash", "button freeze", DEV_A);
        addResolvedIssue(3L, "database bug", "connection fail", DEV_B);

        // when
        List<Long> result = recommendService.recommendAssignees(PROJECT_ID, "login button crash", "button not responding");

        // then
        assertFalse(result.isEmpty());
        assertEquals(DEV_A, result.get(0));
    }

    @Test
    @DisplayName("추천 성공: 최대 3명 반환")
    void limitsToTopThreeRecommendations() {
        // given: 4명의 fixer가 각각 유사 이슈 보유
        addResolvedIssue(1L, "login bug", "button error", DEV_A);
        addResolvedIssue(2L, "login crash", "button freeze", DEV_B);
        addResolvedIssue(3L, "login timeout", "button slow", DEV_C);
        addResolvedIssue(4L, "login error", "button broken", 40L);

        // when
        List<Long> result = recommendService.recommendAssignees(PROJECT_ID, "login bug", "button error");

        // then
        assertTrue(result.size() <= 3);
    }

    @Test
    @DisplayName("추천 실패: RESOLVED/CLOSED 아닌 이슈는 제외")
    void ignoresNonResolvedIssues() {
        // given: NEW 상태(fixerId 없음) 이슈만 존재
        Issue issue = new Issue(PROJECT_ID, "login bug", "button error", 99L);
        issue.setIssueId(1L);
        issueRepository.save(issue);

        // when
        List<Long> result = recommendService.recommendAssignees(PROJECT_ID, "login bug", "button error");

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("추천 성공: 점수 높은 순으로 정렬")
    void returnsSortedByScoreDescending() {
        // given: DEV_B가 더 유사한 이슈 보유
        addResolvedIssue(1L, "login bug", "button error minor", DEV_A);
        addResolvedIssue(2L, "login bug", "button error critical crash", DEV_B);

        // when
        List<Long> result = recommendService.recommendAssignees(PROJECT_ID, "login bug", "button error critical");

        // then
        assertFalse(result.isEmpty());
        assertEquals(DEV_B, result.get(0));
    }

    @Test
    @DisplayName("Jaccard 유사도: 교집합/합집합 정확도")
    void jaccardSimilarityIsCorrect() {
        // given
        Set<String> a = Set.of("login", "button", "error");
        Set<String> b = Set.of("login", "button", "crash");

        // when
        double sim = recommendService.jaccardSimilarity(a, b);

        // then: 교집합={login,button}=2, 합집합={login,button,error,crash}=4
        assertEquals(2.0 / 4.0, sim, 0.0001);
    }

    @Test
    @DisplayName("Jaccard 유사도: 빈 집합이면 0 반환")
    void jaccardSimilarityReturnsZeroForEmptySets() {
        // when & then
        assertEquals(0.0, recommendService.jaccardSimilarity(Set.of(), Set.of("login")));
        assertEquals(0.0, recommendService.jaccardSimilarity(Set.of("login"), Set.of()));
    }

    @Test
    @DisplayName("토크나이징: 소문자 변환 및 1자 단어 필터")
    void tokenizeConvertsToLowercaseAndFiltersShortWords() {
        // when
        Set<String> tokens = recommendService.tokenize("Login Button ERROR a");

        // then
        assertTrue(tokens.contains("login"));
        assertTrue(tokens.contains("button"));
        assertTrue(tokens.contains("error"));
        assertFalse(tokens.contains("a"));
        assertFalse(tokens.contains("Login"));
    }

    @Test
    @DisplayName("토크나이징: null 입력 시 빈 집합 반환")
    void tokenizeReturnsEmptySetForNull() {
        // when & then
        assertTrue(recommendService.tokenize(null).isEmpty());
    }

    private void addResolvedIssue(Long id, String title, String desc, Long fixerId) {
        Issue issue = new Issue(PROJECT_ID, title, desc, 99L);
        issue.setIssueId(id);
        issue.markAsFixed(fixerId);
        issue.markAsResolved();
        issueRepository.save(issue);
    }

    private void addClosedIssue(Long id, String title, String desc, Long fixerId) {
        Issue issue = new Issue(PROJECT_ID, title, desc, 99L);
        issue.setIssueId(id);
        issue.markAsFixed(fixerId);
        issue.markAsResolved();
        issue.markAsClosed();
        issueRepository.save(issue);
    }

    private static class FakeIssueRepository extends IssueRepository {
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
    }
}
