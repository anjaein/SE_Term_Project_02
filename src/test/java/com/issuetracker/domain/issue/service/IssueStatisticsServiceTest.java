package com.issuetracker.domain.issue.service;

import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.domain.issue.enums.Priority;
import com.issuetracker.domain.issue.enums.Status;
import com.issuetracker.domain.issue.repository.IssueRepository;
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

/*
테스트 목록:
- 월별 발생 트랜드
- 월별 해결 트랜드
- 일별 발생 트랜드 (최근 7일)
- 일별 해결 트랜드 (최근 7일)
- 일별 우선순위 분포 (최근 7일)
- 월별 우선순위 분포
- 월별 평균 해결 일수 (CLOSED 이슈 기준)
*/

class IssueStatisticsServiceTest {
    private static final Long PROJECT_ID = 1L;
    private static final Long OTHER_PROJECT_ID = 2L;

    private FakeIssueRepository issueRepository;
    private IssueStatisticsService statisticsService;

    @BeforeEach
    void setUp() {
        issueRepository = new FakeIssueRepository();
        statisticsService = new IssueStatisticsService(issueRepository);
    }

    @Test
    @DisplayName("월별 발생 트랜드: 해당 월에 생성된 이슈 수 집계")
    void monthlyReportedTrend() {
        // given
        YearMonth thisMonth = YearMonth.now();
        YearMonth lastMonth = thisMonth.minusMonths(1);

        issueRepository.saveWithReportedDate(PROJECT_ID, thisMonth.atDay(1).atStartOfDay());
        issueRepository.saveWithReportedDate(PROJECT_ID, thisMonth.atDay(15).atStartOfDay());
        issueRepository.saveWithReportedDate(PROJECT_ID, lastMonth.atDay(10).atStartOfDay());

        // when
        Map<YearMonth, Long> trend = statisticsService.getMonthlyReportedTrend(PROJECT_ID, 3);

        // then
        assertEquals(2L, trend.get(thisMonth));
        assertEquals(1L, trend.get(lastMonth));
    }

    @Test
    @DisplayName("월별 발생 트랜드: 다른 프로젝트 이슈는 집계 제외")
    void monthlyReportedTrendExcludesOtherProject() {
        // given
        YearMonth thisMonth = YearMonth.now();
        issueRepository.saveWithReportedDate(PROJECT_ID, thisMonth.atDay(1).atStartOfDay());
        issueRepository.saveWithReportedDate(OTHER_PROJECT_ID, thisMonth.atDay(1).atStartOfDay());

        // when
        Map<YearMonth, Long> trend = statisticsService.getMonthlyReportedTrend(PROJECT_ID, 1);

        // then
        assertEquals(1L, trend.get(thisMonth));
    }

    @Test
    @DisplayName("월별 발생 트랜드: 범위 밖 이슈는 집계 제외")
    void monthlyReportedTrendExcludesOutOfRange() {
        // given
        YearMonth thisMonth = YearMonth.now();
        YearMonth veryOldMonth = thisMonth.minusMonths(10);

        issueRepository.saveWithReportedDate(PROJECT_ID, thisMonth.atDay(1).atStartOfDay());
        issueRepository.saveWithReportedDate(PROJECT_ID, veryOldMonth.atDay(1).atStartOfDay());

        // when
        Map<YearMonth, Long> trend = statisticsService.getMonthlyReportedTrend(PROJECT_ID, 3);

        // then
        assertEquals(1L, trend.get(thisMonth));
        assertFalse(trend.containsKey(veryOldMonth));
    }

    @Test
    @DisplayName("월별 해결 트랜드: resolvedDate 기준 집계")
    void monthlyResolvedTrend() {
        // given
        YearMonth thisMonth = YearMonth.now();

        Issue issue1 = issueRepository.saveWithReportedDate(PROJECT_ID, thisMonth.atDay(1).atStartOfDay());
        Issue issue2 = issueRepository.saveWithReportedDate(PROJECT_ID, thisMonth.atDay(5).atStartOfDay());
        issueRepository.saveWithReportedDate(PROJECT_ID, thisMonth.atDay(10).atStartOfDay());

        setField(issue1, "resolvedDate", thisMonth.atDay(20).atStartOfDay());
        setField(issue2, "resolvedDate", thisMonth.atDay(25).atStartOfDay());

        // when
        Map<YearMonth, Long> trend = statisticsService.getMonthlyResolvedTrend(PROJECT_ID, 1);

        // then
        assertEquals(2L, trend.get(thisMonth));
    }

    @Test
    @DisplayName("일별 발생 트랜드: 최근 7일 내 생성된 이슈 수 집계")
    void dailyReportedTrend() {
        // given
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        issueRepository.saveWithReportedDate(PROJECT_ID, today.atTime(9, 0));
        issueRepository.saveWithReportedDate(PROJECT_ID, today.atTime(15, 0));
        issueRepository.saveWithReportedDate(PROJECT_ID, yesterday.atTime(10, 0));

        // when
        Map<LocalDate, Long> trend = statisticsService.getDailyReportedTrend(PROJECT_ID);

        // then
        assertEquals(2L, trend.get(today));
        assertEquals(1L, trend.get(yesterday));
    }

    @Test
    @DisplayName("일별 발생 트랜드: 7일 범위 밖 이슈는 집계 제외")
    void dailyReportedTrendExcludesOutOfRange() {
        // given
        LocalDate today = LocalDate.now();
        LocalDate longAgo = today.minusDays(10);

        issueRepository.saveWithReportedDate(PROJECT_ID, today.atTime(9, 0));
        issueRepository.saveWithReportedDate(PROJECT_ID, longAgo.atTime(9, 0));

        // when
        Map<LocalDate, Long> trend = statisticsService.getDailyReportedTrend(PROJECT_ID);

        // then
        assertEquals(1L, trend.get(today));
        assertFalse(trend.containsKey(longAgo));
    }

    @Test
    @DisplayName("일별 해결 트랜드: resolvedDate 기준 최근 7일 집계")
    void dailyResolvedTrend() {
        // given
        LocalDate today = LocalDate.now();

        Issue issue1 = issueRepository.saveWithReportedDate(PROJECT_ID, today.minusDays(3).atTime(9, 0));
        Issue issue2 = issueRepository.saveWithReportedDate(PROJECT_ID, today.minusDays(5).atTime(9, 0));
        issueRepository.saveWithReportedDate(PROJECT_ID, today.atTime(9, 0));

        setField(issue1, "resolvedDate", today.atTime(10, 0));
        setField(issue2, "resolvedDate", today.atTime(11, 0));

        // when
        Map<LocalDate, Long> trend = statisticsService.getDailyResolvedTrend(PROJECT_ID);

        // then
        assertEquals(2L, trend.get(today));
    }

    @Test
    @DisplayName("일별 우선순위 분포: 최근 7일 각 날짜별 우선순위 집계")
    void dailyPriorityDistribution() {
        // given
        LocalDate today = LocalDate.now();
        Issue issue1 = issueRepository.saveWithReportedDate(PROJECT_ID, today.atTime(9, 0));
        issue1.setPriority(Priority.BLOCKER);
        Issue issue2 = issueRepository.saveWithReportedDate(PROJECT_ID, today.atTime(11, 0));
        issue2.setPriority(Priority.BLOCKER);
        Issue issue3 = issueRepository.saveWithReportedDate(PROJECT_ID, today.atTime(13, 0));
        issue3.setPriority(Priority.MINOR);

        // when
        Map<LocalDate, Map<Priority, Long>> distribution =
                statisticsService.getDailyPriorityDistribution(PROJECT_ID);

        // then
        Map<Priority, Long> todayCounts = distribution.get(today);
        assertEquals(2L, todayCounts.get(Priority.BLOCKER));
        assertEquals(1L, todayCounts.get(Priority.MINOR));
        assertEquals(0L, todayCounts.get(Priority.CRITICAL));
    }

    @Test
    @DisplayName("월별 우선순위 분포: 각 월별 우선순위 집계")
    void monthlyPriorityDistribution() {
        // given
        YearMonth thisMonth = YearMonth.now();
        Issue issue1 = issueRepository.saveWithReportedDate(PROJECT_ID, thisMonth.atDay(1).atStartOfDay());
        issue1.setPriority(Priority.BLOCKER);
        Issue issue2 = issueRepository.saveWithReportedDate(PROJECT_ID, thisMonth.atDay(10).atStartOfDay());
        issue2.setPriority(Priority.MINOR);

        // when
        Map<YearMonth, Map<Priority, Long>> distribution =
                statisticsService.getMonthlyPriorityDistribution(PROJECT_ID, 3);

        // then
        Map<Priority, Long> thisMonthCounts = distribution.get(thisMonth);
        assertEquals(1L, thisMonthCounts.get(Priority.BLOCKER));
        assertEquals(1L, thisMonthCounts.get(Priority.MINOR));
        assertEquals(0L, thisMonthCounts.get(Priority.CRITICAL));
    }

    @Test
    @DisplayName("월별 우선순위 분포: 다른 프로젝트 이슈 제외")
    void monthlyPriorityDistributionExcludesOtherProject() {
        // given
        YearMonth thisMonth = YearMonth.now();
        Issue mine = issueRepository.saveWithReportedDate(PROJECT_ID, thisMonth.atDay(1).atStartOfDay());
        mine.setPriority(Priority.BLOCKER);
        Issue other = issueRepository.saveWithReportedDate(OTHER_PROJECT_ID, thisMonth.atDay(1).atStartOfDay());
        other.setPriority(Priority.BLOCKER);

        // when
        Map<YearMonth, Map<Priority, Long>> distribution =
                statisticsService.getMonthlyPriorityDistribution(PROJECT_ID, 1);

        // then
        assertEquals(1L, distribution.get(thisMonth).get(Priority.BLOCKER));
    }

    @Test
    @DisplayName("월별 평균 해결 일수: 해당 월에 CLOSED된 이슈의 평균 일수 계산")
    void monthlyAverageClosedDays() {
        // given: 이번 달에 CLOSED된 이슈 2개 (10일, 20일 소요) → 평균 15일
        YearMonth thisMonth = YearMonth.now();
        LocalDateTime base = thisMonth.atDay(1).atStartOfDay();

        issueRepository.saveWithReportedAndClosedDate(PROJECT_ID, base, base.plusDays(10));
        issueRepository.saveWithReportedAndClosedDate(PROJECT_ID, base, base.plusDays(20));

        // when
        Map<YearMonth, Double> result = statisticsService.getMonthlyAverageClosedDays(PROJECT_ID, 1);

        // then
        assertEquals(15.0, result.get(thisMonth), 0.1);
    }

    @Test
    @DisplayName("월별 평균 해결 일수: CLOSED 이슈 없으면 해당 월 key가 없음")
    void monthlyAverageClosedDaysWithNoClosedIssues() {
        // given
        YearMonth thisMonth = YearMonth.now();
        issueRepository.saveWithReportedDate(PROJECT_ID, thisMonth.atDay(1).atStartOfDay());

        // when
        Map<YearMonth, Double> result = statisticsService.getMonthlyAverageClosedDays(PROJECT_ID, 1);

        // then
        assertFalse(result.containsKey(thisMonth));
    }

    @Test
    @DisplayName("월별 평균 해결 일수: 다른 달에 CLOSED된 이슈는 해당 월과 별도 집계")
    void monthlyAverageClosedDaysSeparatedByMonth() {
        // given
        YearMonth thisMonth = YearMonth.now();
        YearMonth lastMonth = thisMonth.minusMonths(1);

        LocalDateTime reportedThisMonth = thisMonth.atDay(1).atStartOfDay();
        LocalDateTime closedThisMonth = thisMonth.atDay(6).atStartOfDay(); // 5일 소요

        LocalDateTime reportedLastMonth = lastMonth.atDay(1).atStartOfDay();
        LocalDateTime closedLastMonth = lastMonth.atDay(11).atStartOfDay(); // 10일 소요

        issueRepository.saveWithReportedAndClosedDate(PROJECT_ID, reportedThisMonth, closedThisMonth);
        issueRepository.saveWithReportedAndClosedDate(PROJECT_ID, reportedLastMonth, closedLastMonth);

        // when
        Map<YearMonth, Double> result = statisticsService.getMonthlyAverageClosedDays(PROJECT_ID, 2);

        // then
        assertEquals(5.0, result.get(thisMonth), 0.1);
        assertEquals(10.0, result.get(lastMonth), 0.1);
    }

    @Test
    @DisplayName("월별 평균 해결 일수: 다른 프로젝트 CLOSED 이슈 제외")
    void monthlyAverageClosedDaysExcludesOtherProject() {
        // given
        YearMonth thisMonth = YearMonth.now();
        LocalDateTime base = thisMonth.atDay(1).atStartOfDay();

        issueRepository.saveWithReportedAndClosedDate(PROJECT_ID, base, base.plusDays(10));
        issueRepository.saveWithReportedAndClosedDate(OTHER_PROJECT_ID, base, base.plusDays(40));

        // when
        Map<YearMonth, Double> result = statisticsService.getMonthlyAverageClosedDays(PROJECT_ID, 1);

        // then
        assertEquals(10.0, result.get(thisMonth), 0.1);
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

    private static class FakeIssueRepository extends IssueRepository {
        private final List<Issue> issues = new ArrayList<>();
        private long nextId = 1L;

        Issue saveWithReportedDate(Long projectId, LocalDateTime reportedDate) {
            Issue issue = new Issue(projectId, "title", "description", 10L);
            issue.setIssueId(nextId++);
            setField(issue, "reportedDate", reportedDate);
            issues.add(issue);
            return issue;
        }

        Issue saveWithReportedAndClosedDate(Long projectId, LocalDateTime reportedDate, LocalDateTime closedDate) {
            Issue issue = new Issue(projectId, "title", "description", 10L);
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
    }
}