package com.issuetracker.domain.issue.repository;

import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.domain.issue.enums.Priority;
import com.issuetracker.domain.issue.enums.Status;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IssueRepositoryTest {

    private static final Path ISSUES_FILE = Path.of("data", "issues.json");
    private static final Long PROJECT_ID = 1L;
    private static final Long OTHER_PROJECT_ID = 2L;
    private static final Long REPORTER_ID = 10L;
    private static final Long OTHER_REPORTER_ID = 11L;
    private static final Long ASSIGNEE_ID = 20L;

    private final IssueRepository issueRepository = new JsonIssueRepository();

    private String originalJson;

    @BeforeEach
    void setUp() throws IOException {
        originalJson = Files.exists(ISSUES_FILE)
                ? Files.readString(ISSUES_FILE, StandardCharsets.UTF_8)
                : null;

        Files.createDirectories(ISSUES_FILE.getParent());
        Files.writeString(ISSUES_FILE, "[]", StandardCharsets.UTF_8);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (originalJson == null) {
            Files.deleteIfExists(ISSUES_FILE);
        } else {
            Files.writeString(ISSUES_FILE, originalJson, StandardCharsets.UTF_8);
        }
    }

    @Test
    @DisplayName("이슈 저장 성공: ID가 순차 부여되고 저장소에 유지")
    void saveAssignsSequentialIds() {
        assertTrue(issueRepository.save(new Issue(PROJECT_ID, "첫 번째", "설명1", Priority.MAJOR, REPORTER_ID)));
        assertTrue(issueRepository.save(new Issue(PROJECT_ID, "두 번째", "설명2", Priority.MAJOR, REPORTER_ID)));

        List<Issue> issues = issueRepository.findAll();
        assertEquals(2, issues.size());
        assertEquals(1L, issues.get(0).getIssueId());
        assertEquals(2L, issues.get(1).getIssueId());
    }

    @Test
    @DisplayName("이슈 단건 조회 성공: issueId로 정확히 반환")
    void findByIssueIdReturnsIssue() {
        Issue issue = new Issue(PROJECT_ID, "이슈 제목", "설명", Priority.MAJOR, REPORTER_ID);
        issueRepository.save(issue);

        Issue found = issueRepository.findByIssueId(issue.getIssueId());

        assertNotNull(found);
        assertEquals("이슈 제목", found.getTitle());
    }

    @Test
    @DisplayName("이슈 프로젝트별 조회 성공: 해당 프로젝트 이슈만 반환")
    void findByProjectIdReturnsOnlyThatProject() {
        issueRepository.save(new Issue(PROJECT_ID, "프로젝트1 이슈", "설명", Priority.MAJOR, REPORTER_ID));
        issueRepository.save(new Issue(OTHER_PROJECT_ID, "프로젝트2 이슈", "설명", Priority.MAJOR, REPORTER_ID));

        List<Issue> issues = issueRepository.findByProjectId(PROJECT_ID);

        assertEquals(1, issues.size());
        assertEquals(PROJECT_ID, issues.get(0).getProjectId());
    }

    @Test
    @DisplayName("이슈 담당자별 조회 성공: 해당 담당자 이슈만 반환")
    void findByAssigneeIdReturnsOnlyAssigned() {
        Issue assigned = new Issue(PROJECT_ID, "담당 이슈", "설명", Priority.MAJOR, REPORTER_ID);
        issueRepository.save(assigned);
        assigned = issueRepository.findByIssueId(assigned.getIssueId());
        assigned.assignTo(ASSIGNEE_ID);
        issueRepository.update(assigned);

        issueRepository.save(new Issue(PROJECT_ID, "미담당 이슈", "설명", Priority.MAJOR, REPORTER_ID));

        List<Issue> issues = issueRepository.findByAssigneeId(ASSIGNEE_ID);

        assertEquals(1, issues.size());
        assertEquals(ASSIGNEE_ID, issues.get(0).getAssigneeId());
    }

    @Test
    @DisplayName("이슈 보고자별 조회 성공: 해당 보고자 이슈만 반환")
    void findByReporterIdReturnsOnlyReporter() {
        issueRepository.save(new Issue(PROJECT_ID, "이슈1", "설명", Priority.MAJOR, REPORTER_ID));
        issueRepository.save(new Issue(PROJECT_ID, "이슈2", "설명", Priority.MAJOR, OTHER_REPORTER_ID));

        List<Issue> issues = issueRepository.findByReporterId(REPORTER_ID);

        assertEquals(1, issues.size());
        assertEquals(REPORTER_ID, issues.get(0).getReporterId());
    }

    @Test
    @DisplayName("이슈 상태별 조회 성공: 해당 상태 이슈만 반환")
    void findByStatusReturnsOnlyMatching() {
        issueRepository.save(new Issue(PROJECT_ID, "새 이슈", "설명", Priority.MAJOR, REPORTER_ID));

        Issue assignedIssue = new Issue(PROJECT_ID, "담당 이슈", "설명", Priority.MAJOR, REPORTER_ID);
        issueRepository.save(assignedIssue);
        assignedIssue = issueRepository.findByIssueId(assignedIssue.getIssueId());
        assignedIssue.assignTo(ASSIGNEE_ID);
        issueRepository.update(assignedIssue);

        List<Issue> newIssues = issueRepository.findByStatus(Status.NEW);
        List<Issue> assignedIssues = issueRepository.findByStatus(Status.ASSIGNED);

        assertEquals(1, newIssues.size());
        assertEquals(Status.NEW, newIssues.get(0).getStatus());
        assertEquals(1, assignedIssues.size());
        assertEquals(Status.ASSIGNED, assignedIssues.get(0).getStatus());
    }

    @Test
    @DisplayName("이슈 우선순위별 조회 성공: 해당 우선순위 이슈만 반환")
    void findByPriorityReturnsOnlyMatching() {
        issueRepository.save(new Issue(PROJECT_ID, "MAJOR 이슈", "설명", Priority.MAJOR, REPORTER_ID));
        issueRepository.save(new Issue(PROJECT_ID, "CRITICAL 이슈", "설명", Priority.CRITICAL, REPORTER_ID));

        List<Issue> majorIssues = issueRepository.findByPriority(Priority.MAJOR);
        List<Issue> criticalIssues = issueRepository.findByPriority(Priority.CRITICAL);

        assertEquals(1, majorIssues.size());
        assertEquals(Priority.MAJOR, majorIssues.get(0).getPriority());
        assertEquals(1, criticalIssues.size());
        assertEquals(Priority.CRITICAL, criticalIssues.get(0).getPriority());
    }

    @Test
    @DisplayName("이슈 수정 성공: 변경된 내용이 저장소에 반영")
    void updatePersistsChanges() {
        Issue issue = new Issue(PROJECT_ID, "제목", "설명", Priority.MAJOR, REPORTER_ID);
        issueRepository.save(issue);
        issue = issueRepository.findByIssueId(issue.getIssueId());
        issue.assignTo(ASSIGNEE_ID);

        assertTrue(issueRepository.update(issue));

        Issue updated = issueRepository.findByIssueId(issue.getIssueId());
        assertEquals(ASSIGNEE_ID, updated.getAssigneeId());
        assertEquals(Status.ASSIGNED, updated.getStatus());
    }

    @Test
    @DisplayName("이슈 전체 조회 성공: 저장된 모든 이슈 반환")
    void findAllReturnsAllIssues() {
        issueRepository.save(new Issue(PROJECT_ID, "이슈1", "설명", Priority.MAJOR, REPORTER_ID));
        issueRepository.save(new Issue(PROJECT_ID, "이슈2", "설명", Priority.MAJOR, REPORTER_ID));

        List<Issue> all = issueRepository.findAll();

        assertEquals(2, all.size());
    }
}
