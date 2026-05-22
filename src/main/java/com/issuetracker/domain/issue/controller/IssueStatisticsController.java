package com.issuetracker.domain.issue.controller;

import com.issuetracker.domain.issue.enums.Priority;
import com.issuetracker.domain.issue.service.IssueStatisticsService;
import com.issuetracker.global.common.Response;
import com.issuetracker.global.common.SessionManager;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

@RequiredArgsConstructor
public class IssueStatisticsController {
    private final IssueStatisticsService issueStatisticsService;
    private final SessionManager sessionManager;

    public Response<Map<YearMonth, Long>> getMonthlyReportedTrend(Long projectId, int months) {
        if (sessionManager.getLoggedInAccount() == null) {
            return Response.fail("You are not logged in.");
        }
        return issueStatisticsService.getMonthlyReportedTrend(projectId, months);
    }

    public Response<Map<YearMonth, Long>> getMonthlyResolvedTrend(Long projectId, int months) {
        if (sessionManager.getLoggedInAccount() == null) {
            return Response.fail("You are not logged in.");
        }
        return issueStatisticsService.getMonthlyResolvedTrend(projectId, months);
    }

    public Response<Map<LocalDate, Long>> getDailyReportedTrend(Long projectId) {
        if (sessionManager.getLoggedInAccount() == null) {
            return Response.fail("You are not logged in.");
        }
        return issueStatisticsService.getDailyReportedTrend(projectId);
    }

    public Response<Map<LocalDate, Long>> getDailyResolvedTrend(Long projectId) {
        if (sessionManager.getLoggedInAccount() == null) {
            return Response.fail("You are not logged in.");
        }
        return issueStatisticsService.getDailyResolvedTrend(projectId);
    }

    public Response<Map<LocalDate, Map<Priority, Long>>> getDailyPriorityDistribution(Long projectId) {
        if (sessionManager.getLoggedInAccount() == null) {
            return Response.fail("You are not logged in.");
        }
        return issueStatisticsService.getDailyPriorityDistribution(projectId);
    }

    public Response<Map<YearMonth, Map<Priority, Long>>> getMonthlyPriorityDistribution(Long projectId, int months) {
        if (sessionManager.getLoggedInAccount() == null) {
            return Response.fail("You are not logged in.");
        }
        return issueStatisticsService.getMonthlyPriorityDistribution(projectId, months);
    }

    public Response<Map<YearMonth, Double>> getMonthlyAverageClosedDays(Long projectId, int months) {
        if (sessionManager.getLoggedInAccount() == null) {
            return Response.fail("You are not logged in.");
        }
        return issueStatisticsService.getMonthlyAverageClosedDays(projectId, months);
    }
}