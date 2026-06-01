package com.issuetracker.domain.issue.service;

import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.domain.issue.enums.Priority;
import com.issuetracker.domain.issue.enums.Status;
import com.issuetracker.domain.issue.repository.IssueRepository;
import com.issuetracker.global.common.Response;
import lombok.RequiredArgsConstructor;

import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class IssueStatisticsService {
    private final IssueRepository issueRepository;
    private final IssueStatisticsValidator statisticsValidator;

    public Response<Map<YearMonth, Long>> getMonthlyReportedTrend(Long projectId, int months) {
        String missingParams = statisticsValidator.checkNonNull(projectId);
        if (missingParams != null) {
            return Response.fail(missingParams);
        }
        String invalidMonths = statisticsValidator.checkMonths(months);
        if (invalidMonths != null) {
            return Response.fail(invalidMonths);
        }

        YearMonth end = YearMonth.now();
        YearMonth start = end.minusMonths(months - 1);

        List<Issue> issues = issueRepository.findByProjectId(projectId);

        Map<YearMonth, Long> result = new HashMap<>();
        for (YearMonth ym = start; !ym.isAfter(end); ym = ym.plusMonths(1)) {
            result.put(ym, 0L);
        }

        for (Issue issue : issues) {
            if (issue.getReportedDate() == null) continue;
            YearMonth ym = YearMonth.from(issue.getReportedDate());
            if (result.containsKey(ym)) {
                result.merge(ym, 1L, Long::sum);
            }
        }
        return Response.success("Monthly reported trend retrieved.", result);
    }

    public Response<Map<LocalDate, Long>> getDailyReportedTrend(Long projectId) {
        String missingParams = statisticsValidator.checkNonNull(projectId);
        if (missingParams != null) {
            return Response.fail(missingParams);
        }

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);

        List<Issue> issues = issueRepository.findByProjectId(projectId);

        Map<LocalDate, Long> result = new HashMap<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            result.put(d, 0L);
        }

        for (Issue issue : issues) {
            if (issue.getReportedDate() == null) continue;
            LocalDate day = issue.getReportedDate().toLocalDate();
            if (result.containsKey(day)) {
                result.merge(day, 1L, Long::sum);
            }
        }
        return Response.success("Daily reported trend retrieved.", result);
    }

    // 일별 이슈 해결 트랜드 (최근 7일, resolvedDate 기준)
    public Response<Map<LocalDate, Long>> getDailyResolvedTrend(Long projectId) {
        String missingParams = statisticsValidator.checkNonNull(projectId);
        if (missingParams != null) {
            return Response.fail(missingParams);
        }

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);

        List<Issue> issues = issueRepository.findByProjectId(projectId);

        Map<LocalDate, Long> result = new HashMap<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            result.put(d, 0L);
        }

        for (Issue issue : issues) {
            if (issue.getResolvedDate() == null) continue;
            LocalDate day = issue.getResolvedDate().toLocalDate();
            if (result.containsKey(day)) {
                result.merge(day, 1L, Long::sum);
            }
        }
        return Response.success("Daily resolved trend retrieved.", result);
    }

    public Response<Map<YearMonth, Long>> getMonthlyResolvedTrend(Long projectId, int months) {
        String missingParams = statisticsValidator.checkNonNull(projectId);
        if (missingParams != null) {
            return Response.fail(missingParams);
        }
        String invalidMonths = statisticsValidator.checkMonths(months);
        if (invalidMonths != null) {
            return Response.fail(invalidMonths);
        }

        YearMonth end = YearMonth.now();
        YearMonth start = end.minusMonths(months - 1);

        List<Issue> issues = issueRepository.findByProjectId(projectId);

        Map<YearMonth, Long> result = new HashMap<>();
        for (YearMonth ym = start; !ym.isAfter(end); ym = ym.plusMonths(1)) {
            result.put(ym, 0L);
        }

        for (Issue issue : issues) {
            if (issue.getResolvedDate() == null) continue;
            YearMonth ym = YearMonth.from(issue.getResolvedDate());
            if (result.containsKey(ym)) {
                result.merge(ym, 1L, Long::sum);
            }
        }
        return Response.success("Monthly resolved trend retrieved.", result);
    }

    public Response<Map<LocalDate, Map<Priority, Long>>> getDailyPriorityDistribution(Long projectId) {
        String missingParams = statisticsValidator.checkNonNull(projectId);
        if (missingParams != null) {
            return Response.fail(missingParams);
        }

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);

        List<Issue> issues = issueRepository.findByProjectId(projectId);

        Map<LocalDate, Map<Priority, Long>> distribution = new HashMap<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            distribution.put(d, newPriorityCounts());
        }

        for (Issue issue : issues) {
            if (issue.getReportedDate() == null) continue;
            LocalDate day = issue.getReportedDate().toLocalDate();
            Map<Priority, Long> counts = distribution.get(day);
            if (counts != null) {
                counts.merge(issue.getPriority(), 1L, Long::sum);
            }
        }
        return Response.success("Daily priority distribution retrieved.", distribution);
    }

    public Response<Map<YearMonth, Map<Priority, Long>>> getMonthlyPriorityDistribution(Long projectId, int months) {
        String missingParams = statisticsValidator.checkNonNull(projectId);
        if (missingParams != null) {
            return Response.fail(missingParams);
        }
        String invalidMonths = statisticsValidator.checkMonths(months);
        if (invalidMonths != null) {
            return Response.fail(invalidMonths);
        }

        YearMonth end = YearMonth.now();
        YearMonth start = end.minusMonths(months - 1);

        List<Issue> issues = issueRepository.findByProjectId(projectId);

        Map<YearMonth, Map<Priority, Long>> distribution = new HashMap<>();
        for (YearMonth ym = start; !ym.isAfter(end); ym = ym.plusMonths(1)) {
            distribution.put(ym, newPriorityCounts());
        }

        for (Issue issue : issues) {
            if (issue.getReportedDate() == null) continue;
            YearMonth ym = YearMonth.from(issue.getReportedDate());
            Map<Priority, Long> counts = distribution.get(ym);
            if (counts != null) {
                counts.merge(issue.getPriority(), 1L, Long::sum);
            }
        }
        return Response.success("Monthly priority distribution retrieved.", distribution);
    }

    private Map<Priority, Long> newPriorityCounts() {
        Map<Priority, Long> counts = new HashMap<>();
        for (Priority priority : Priority.values()) {
            counts.put(priority, 0L);
        }
        return counts;
    }

    public Response<Map<YearMonth, Double>> getMonthlyAverageClosedDays(Long projectId, int months){
        String missingParams = statisticsValidator.checkNonNull(projectId);
        if (missingParams != null) {
            return Response.fail(missingParams);
        }
        String invalidMonths = statisticsValidator.checkMonths(months);
        if (invalidMonths != null) {
            return Response.fail(invalidMonths);
        }

        YearMonth end = YearMonth.now();
        YearMonth start = end.minusMonths(months - 1);
        List<Issue> issues = issueRepository.findByProjectId(projectId);

        Map<YearMonth, long[]> statistics = new HashMap<>();
        for (YearMonth ym = start; !ym.isAfter(end); ym = ym.plusMonths(1)) {
            statistics.put(ym, new long[2]);
        }

        for(Issue issue : issues){
            if(issue.getStatus() != Status.CLOSED) continue;
            if(issue.getClosedDate() == null || issue.getReportedDate() == null) continue;

            YearMonth closedMonth = YearMonth.from(issue.getClosedDate());
            if(!statistics.containsKey(closedMonth)) continue;

            long[] stats = statistics.get(closedMonth);
            stats[0] += ChronoUnit.DAYS.between(issue.getReportedDate(), issue.getClosedDate());
            stats[1] += 1;
        }

        Map<YearMonth, Double> result = new HashMap<>();
        for(Map.Entry<YearMonth, long[]> entry : statistics.entrySet()){
            long[] stats = entry.getValue();
            result.put(entry.getKey(), stats[1] == 0 ? 0.0 : (double) stats[0] / stats[1]);
        }
        return Response.success("Monthly average closed days retrieved.", result);
    }
}
