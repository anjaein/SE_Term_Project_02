package com.issuetracker.domain.issue.controller;

import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.domain.issue.enums.Priority;
import com.issuetracker.domain.issue.enums.Status;
import com.issuetracker.domain.issue.repository.IssueRepository;
import com.issuetracker.domain.issue.service.IssueStatisticsService;
import com.issuetracker.global.common.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IssueStatisticsControllerTest {
    private static final Long PROJECT_ID = 1L;

    private FakeIssueRepository issueRepository;
    private SessionManager sessionManager;
    private IssueStatisticsController controller;

    @BeforeEach
    void setUp() {
        issueRepository = new FakeIssueRepository();
        sessionManager = new SessionManager();
        IssueStatisticsService service = new IssueStatisticsService(issueRepository, new com.issuetracker.domain.issue.service.IssueStatisticsValidator());
        controller = new IssueStatisticsController(service, sessionManager);
    }

    @Test
    @DisplayName("비로그인: getMonthlyReportedTrend는 null 반환")
    void monthlyReportedTrendRequiresLogin() {
        assertNull(controller.getMonthlyReportedTrend(PROJECT_ID, 3).getData());
    }

    @Test
    @DisplayName("비로그인: getMonthlyResolvedTrend는 null 반환")
    void monthlyResolvedTrendRequiresLogin() {
        assertNull(controller.getMonthlyResolvedTrend(PROJECT_ID, 3).getData());
    }

    @Test
    @DisplayName("비로그인: getDailyReportedTrend는 null 반환")
    void dailyReportedTrendRequiresLogin() {
        assertNull(controller.getDailyReportedTrend(PROJECT_ID).getData());
    }

    @Test
    @DisplayName("비로그인: getDailyResolvedTrend는 null 반환")
    void dailyResolvedTrendRequiresLogin() {
        assertNull(controller.getDailyResolvedTrend(PROJECT_ID).getData());
    }

    @Test
    @DisplayName("비로그인: getDailyPriorityDistribution는 null 반환")
    void dailyPriorityDistributionRequiresLogin() {
        assertNull(controller.getDailyPriorityDistribution(PROJECT_ID).getData());
    }

    @Test
    @DisplayName("비로그인: getMonthlyPriorityDistribution는 null 반환")
    void monthlyPriorityDistributionRequiresLogin() {
        assertNull(controller.getMonthlyPriorityDistribution(PROJECT_ID, 3).getData());
    }

    @Test
    @DisplayName("비로그인: getMonthlyAverageClosedDays는 null 반환")
    void monthlyAverageClosedDaysRequiresLogin() {
        assertNull(controller.getMonthlyAverageClosedDays(PROJECT_ID, 3).getData());
    }

    @Test
    @DisplayName("로그인: getMonthlyReportedTrend는 Map 반환")
    void monthlyReportedTrendWhenLoggedIn() {
        // given
        login();
        YearMonth thisMonth = YearMonth.now();
        issueRepository.saveWithReportedDate(PROJECT_ID, thisMonth.atDay(1).atStartOfDay());

        // when
        Map<YearMonth, Long> result = controller.getMonthlyReportedTrend(PROJECT_ID, 1).getData();

        // then
        assertNotNull(result);
        assertEquals(1L, result.get(thisMonth));
    }

    @Test
    @DisplayName("로그인: getMonthlyResolvedTrend는 Map 반환")
    void monthlyResolvedTrendWhenLoggedIn() {
        // given
        login();
        YearMonth thisMonth = YearMonth.now();
        Issue issue = issueRepository.saveWithReportedDate(PROJECT_ID, thisMonth.atDay(1).atStartOfDay());
        setField(issue, "resolvedDate", thisMonth.atDay(20).atStartOfDay());

        // when
        Map<YearMonth, Long> result = controller.getMonthlyResolvedTrend(PROJECT_ID, 1).getData();

        // then
        assertNotNull(result);
        assertEquals(1L, result.get(thisMonth));
    }

    @Test
    @DisplayName("로그인: getDailyReportedTrend는 Map 반환")
    void dailyReportedTrendWhenLoggedIn() {
        // given
        login();
        LocalDate today = LocalDate.now();
        issueRepository.saveWithReportedDate(PROJECT_ID, today.atTime(9, 0));

        // when
        Map<LocalDate, Long> result = controller.getDailyReportedTrend(PROJECT_ID).getData();

        // then
        assertNotNull(result);
        assertEquals(1L, result.get(today));
    }

    @Test
    @DisplayName("로그인: getDailyPriorityDistribution는 Map 반환")
    void dailyPriorityDistributionWhenLoggedIn() {
        // given
        login();
        LocalDate today = LocalDate.now();
        issueRepository.saveWithReportedDate(PROJECT_ID, today.atTime(9, 0), Priority.BLOCKER);

        // when
        Map<LocalDate, Map<Priority, Long>> result = controller.getDailyPriorityDistribution(PROJECT_ID).getData();

        // then
        assertNotNull(result);
        assertEquals(1L, result.get(today).get(Priority.BLOCKER));
    }

    @Test
    @DisplayName("로그인: getMonthlyAverageClosedDays는 Map 반환")
    void monthlyAverageClosedDaysWhenLoggedIn() {
        // given
        login();
        YearMonth thisMonth = YearMonth.now();
        LocalDateTime base = thisMonth.atDay(1).atStartOfDay();
        issueRepository.saveWithReportedAndClosedDate(PROJECT_ID, base, base.plusDays(10));

        // when
        Map<YearMonth, Double> result = controller.getMonthlyAverageClosedDays(PROJECT_ID, 1).getData();

        // then
        assertNotNull(result);
        assertEquals(10.0, result.get(thisMonth), 0.1);
    }

    private void login() {
        sessionManager.login(new Account("tester", "pw", false));
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class FakeIssueRepository implements IssueRepository {
        private final List<Issue> issues = new ArrayList<>();
        private long nextId = 1L;

        Issue saveWithReportedDate(Long projectId, LocalDateTime reportedDate) {
            return saveWithReportedDate(projectId, reportedDate, Priority.MAJOR);
        }

        Issue saveWithReportedDate(Long projectId, LocalDateTime reportedDate, Priority priority) {
            Issue issue = new Issue(projectId, "title", "description", priority, 10L);
            issue.setIssueId(nextId++);
            setField(issue, "reportedDate", reportedDate);
            issues.add(issue);
            return issue;
        }

        Issue saveWithReportedAndClosedDate(Long projectId, LocalDateTime reportedDate, LocalDateTime closedDate) {
            Issue issue = new Issue(projectId, "title", "description", Priority.MAJOR, 10L);
            issue.setIssueId(nextId++);
            setField(issue, "reportedDate", reportedDate);
            setField(issue, "status", Status.CLOSED);
            setField(issue, "closedDate", closedDate);
            issues.add(issue);
            return issue;
        }

        private void setField(Object target, String fieldName, Object value) {
            try {
                Field field = target.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public List<Issue> findAll() {
            return issues;
        }

        @Override
        public List<Issue> findByProjectId(Long projectId) {
            return issues.stream()
                    .filter(issue -> issue.getProjectId().equals(projectId))
                    .collect(java.util.stream.Collectors.toList());
        }

        @Override
        public boolean save(Issue issue) {
            issue.setIssueId(nextId++);
            issues.add(issue);
            return true;
        }

        @Override
        public boolean update(Issue updatedIssue) {
            for (int i = 0; i < issues.size(); i++) {
                if (issues.get(i).getIssueId().equals(updatedIssue.getIssueId())) {
                    issues.set(i, updatedIssue);
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<Issue> findByAssigneeId(Long assigneeId) { return List.of(); }

        @Override
        public List<Issue> findByReporterId(Long reporterId) { return List.of(); }

        @Override
        public List<Issue> findByStatus(com.issuetracker.domain.issue.enums.Status status) { return List.of(); }

        @Override
        public List<Issue> findByPriority(com.issuetracker.domain.issue.enums.Priority priority) { return List.of(); }

        @Override
        public Issue findByIssueId(Long issueId) { return null; }
    }
}