package com.issuetracker.domain.issue.controller;

import com.issuetracker.domain.issue.enums.Priority;
import com.issuetracker.domain.issue.service.IssueStatisticsService;
import com.issuetracker.global.common.SessionManager;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

@RequiredArgsConstructor
public class IssueStatisticsController {
    private final IssueStatisticsService issueStatisticsService;
    private final SessionManager sessionManager;

    public Map<YearMonth, Long> getMonthlyReportedTrend(Long projectId, int months) {
        if (sessionManager.getLoggedInAccount() == null) {
            notifyError("You are not logged in.");
            return null;
        }

        return issueStatisticsService.getMonthlyReportedTrend(projectId, months);
    }

    public Map<YearMonth, Long> getMonthlyResolvedTrend(Long projectId, int months) {
        if (sessionManager.getLoggedInAccount() == null) {
            notifyError("You are not logged in.");
            return null;
        }

        return issueStatisticsService.getMonthlyResolvedTrend(projectId, months);
    }

    public Map<LocalDate, Long> getDailyReportedTrend(Long projectId) {
        if (sessionManager.getLoggedInAccount() == null) {
            notifyError("You are not logged in.");
            return null;
        }

        return issueStatisticsService.getDailyReportedTrend(projectId);
    }

    public Map<LocalDate, Long> getDailyResolvedTrend(Long projectId) {
        if (sessionManager.getLoggedInAccount() == null) {
            notifyError("You are not logged in.");
            return null;
        }

        return issueStatisticsService.getDailyResolvedTrend(projectId);
    }

    public Map<LocalDate, Map<Priority, Long>> getDailyPriorityDistribution(Long projectId) {
        if (sessionManager.getLoggedInAccount() == null) {
            notifyError("You are not logged in.");
            return null;
        }

        return issueStatisticsService.getDailyPriorityDistribution(projectId);
    }

    public Map<YearMonth, Map<Priority, Long>> getMonthlyPriorityDistribution(Long projectId, int months) {
        if (sessionManager.getLoggedInAccount() == null) {
            notifyError("You are not logged in.");
            return null;
        }

        return issueStatisticsService.getMonthlyPriorityDistribution(projectId, months);
    }

    public Map<YearMonth, Double> getMonthlyAverageClosedDays(Long projectId, int months) {
        if (sessionManager.getLoggedInAccount() == null) {
            notifyError("You are not logged in.");
            return null;
        }

        return issueStatisticsService.getMonthlyAverageClosedDays(projectId, months);
    }

    private void notifyError(String message) {
        System.out.println("[ERROR] " + message);
    }
}
