package com.issuetracker.domain.issue.service;

import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.domain.issue.enums.Priority;
import com.issuetracker.domain.issue.enums.Status;
import com.issuetracker.domain.issue.repository.IssueRepository;
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

    public Map<YearMonth, Long> getMonthlyReportedTrend(Long projectId, int months) {
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
        return result;
    }

    public Map<LocalDate, Long> getDailyReportedTrend(Long projectId) {
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
        return result;
    }

    // 일별 이슈 해결 트랜드 (최근 7일, resolvedDate 기준)
    public Map<LocalDate, Long> getDailyResolvedTrend(Long projectId) {
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
        return result;
    }

    public Map<YearMonth, Long> getMonthlyResolvedTrend(Long projectId, int months) {
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
        return result;
    }

    public Map<LocalDate, Map<Priority, Long>> getDailyPriorityDistribution(Long projectId) {
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
        return distribution;
    }

    public Map<YearMonth, Map<Priority, Long>> getMonthlyPriorityDistribution(Long projectId, int months) {
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
        return distribution;
    }

    private Map<Priority, Long> newPriorityCounts() {
        Map<Priority, Long> counts = new HashMap<>();
        for (Priority priority : Priority.values()) {
            counts.put(priority, 0L);
        }
        return counts;
    }

    public Map<YearMonth, Double> getMonthlyAverageClosedDays(Long projectId, int months){
        YearMonth end = YearMonth.now();
        YearMonth start = end.minusMonths(months - 1);
        List<Issue> issues = issueRepository.findByProjectId(projectId);

        Map<YearMonth, long[]> statistics = new HashMap<>();

        for(Issue issue : issues){
            if(issue.getStatus() != Status.CLOSED) continue;
            if(issue.getClosedDate() == null || issue.getReportedDate() == null) continue;

            YearMonth closedMonth = YearMonth.from(issue.getClosedDate());
            if(closedMonth.isBefore(start) || closedMonth.isAfter(end)) continue;

            long[] stats = statistics.computeIfAbsent(closedMonth, k -> new long[2]);
            stats[0] += ChronoUnit.DAYS.between(issue.getReportedDate(), issue.getClosedDate());
            stats[1] += 1;
        }

        Map<YearMonth, Double> result = new HashMap<>();
        for(Map.Entry<YearMonth, long[]> entry : statistics.entrySet()){
            long[] stats = entry.getValue();
            double average = (double) stats[0] / stats[1];
            result.put(entry.getKey(), average);
        }
        return result;
    }
}
