package com.issuetracker.domain.issue.entity;

import com.issuetracker.domain.issue.enums.Priority;
import com.issuetracker.domain.issue.enums.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class IssueTest {
    private static final Long PROJECT_ID = 1L;
    private static final Long REPORTER_ID = 10L;
    private static final Long ASSIGNEE_ID = 20L;
    private static final Long FIXER_ID = 30L;

    @Test
    @DisplayName("이슈 생성 성공: status=NEW, priority=MAJOR, reportedDate 자동 추가")
    void createIssueInitializesDefaults() {
        LocalDateTime before = LocalDateTime.now();

        Issue issue = new Issue(PROJECT_ID, "title", "description", Priority.MAJOR, REPORTER_ID);

        LocalDateTime after = LocalDateTime.now();
        assertEquals(PROJECT_ID, issue.getProjectId());
        assertEquals("title", issue.getTitle());
        assertEquals("description", issue.getDescription());
        assertEquals(REPORTER_ID, issue.getReporterId());
        assertEquals(Status.NEW, issue.getStatus());
        assertEquals(Priority.MAJOR, issue.getPriority());
        assertNull(issue.getAssigneeId());
        assertNull(issue.getFixerId());
        assertNull(issue.getFixedDate());
        assertNull(issue.getResolvedDate());
        assertNull(issue.getClosedDate());
        assertFalse(issue.getReportedDate().isBefore(before));
        assertFalse(issue.getReportedDate().isAfter(after));
    }

    @Test
    @DisplayName("이슈 ASSIGN 성공: assigneeId 추가 및 ASSIGNED 상태 변경")
    void assignToChangesStatusToAssigned() {
        Issue issue = createIssue();

        issue.assignTo(ASSIGNEE_ID);

        assertEquals(ASSIGNEE_ID, issue.getAssigneeId());
        assertEquals(Status.ASSIGNED, issue.getStatus());
    }

    @Test
    @DisplayName("이슈 FIX 성공: fixerId/fixedDate 추가 및 FIXED 상태 변경")
    void markAsFixedRecordsFixerAndFixedDate() {
        Issue issue = createIssue();

        issue.markAsFixed(FIXER_ID);

        assertEquals(Status.FIXED, issue.getStatus());
        assertEquals(FIXER_ID, issue.getFixerId());
        assertNotNull(issue.getFixedDate());
    }

    @Test
    @DisplayName("이슈 RESOLVE 성공: resolvedDate 추가 및 RESOLVED 상태 변경")
    void markAsResolvedRecordsResolvedDate() {
        Issue issue = createIssue();

        issue.markAsResolved();

        assertEquals(Status.RESOLVED, issue.getStatus());
        assertNotNull(issue.getResolvedDate());
    }

    @Test
    @DisplayName("이슈 CLOSE 성공: closedDate 추가 및 CLOSED 상태 변경")
    void markAsClosedRecordsClosedDate() {
        Issue issue = createIssue();

        issue.markAsClosed();

        assertEquals(Status.CLOSED, issue.getStatus());
        assertNotNull(issue.getClosedDate());
    }

    private Issue createIssue() {
        return new Issue(PROJECT_ID, "title", "description", Priority.MAJOR, REPORTER_ID);
    }
}