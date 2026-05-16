package com.issuetracker.domain.issue.entity;

import com.issuetracker.domain.issue.enums.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IssueTest {
    private static final Long PROJECT_ID = 1L;
    private static final Long REPORTER_ID = 10L;
    private static final Long ASSIGNEE_ID = 20L;
    private static final Long FIXER_ID = 30L;

    @Test
    @DisplayName("assignTo : assigneeId가 저장되고 ASSIGNED 상태가 된다")
    void assignToChangesStatusToAssigned() {
        // given
        Issue issue = createIssue();

        // when
        issue.assignTo(ASSIGNEE_ID);

        // then
        assertEquals(ASSIGNEE_ID, issue.getAssigneeId());
        assertEquals(Status.ASSIGNED, issue.getStatus());
    }

    @Test
    @DisplayName("markAsFixed : fixerId와 fixedDate가 자동 기록")
    void markAsFixedRecordsFixerAndFixedDate() {
        // given
        Issue issue = createIssue();

        // when
        issue.markAsFixed(FIXER_ID);

        // then
        assertEquals(Status.FIXED, issue.getStatus());
        assertEquals(FIXER_ID, issue.getFixerId());
        assertNotNull(issue.getFixedDate());
    }

    @Test
    @DisplayName("markAsResolved : resolvedDate가 자동 기록")
    void markAsResolvedRecordsResolvedDate() {
        // given
        Issue issue = createIssue();

        // when
        issue.markAsResolved();

        // then
        assertEquals(Status.RESOLVED, issue.getStatus());
        assertNotNull(issue.getResolvedDate());
    }

    @Test
    @DisplayName("markAsClosed : closedDate가  자동 기록")
    void markAsClosedRecordsClosedDate() {
        // given
        Issue issue = createIssue();

        // when
        issue.markAsClosed();

        // then
        assertEquals(Status.CLOSED, issue.getStatus());
        assertNotNull(issue.getClosedDate());
    }

    private Issue createIssue() {
        return new Issue(PROJECT_ID, "title", "description", REPORTER_ID);
    }
}
