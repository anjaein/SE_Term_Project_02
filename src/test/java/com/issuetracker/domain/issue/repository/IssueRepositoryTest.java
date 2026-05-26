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

/*
테스트 목록:
- save: ID 자동 부여 및 저장
- findAll: 전체 조회
- findByIssueId: ID로 단건 조회
- findByProjectId: 프로젝트 ID로 조회
- findByAssigneeId: 담당자 ID로 조회
- findByReporterId: 보고자 ID로 조회
- findByStatus: 상태로 조회
- findByPriority: 우선순위로 조회
- update: 이슈 수정
- update: 존재하지 않는 이슈 수정 시 false 반환
*/

class IssueRepositoryTest {

    private static final Path ISSUES_FILE = Path.of("data", "issues.json");
    private static final Long PROJECT_ID = 1L;
    private static final Long REPORTER_ID = 10L;
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
    @DisplayName("이슈 저장 시 ID가 순차적으로 부여되고 저장소에 데이터가 유지된다")
    void saveAssignsIdAndPersists() {
        // given
        Issue issue1 = new Issue(PROJECT_ID, "첫 번째 이슈", "설명1", REPORTER_ID);
        Issue issue2 = new Issue(PROJECT_ID, "두 번째 이슈", "설명2", REPORTER_ID);

        // when
        assertTrue(issueRepository.save(issue1));
        assertTrue(issueRepository.save(issue2));

        // then
        List<Issue> issues = issueRepository.findAll();
        assertEquals(2, issues.size());
        assertEquals(1L, issues.get(0).getIssueId());
        assertEquals(2L, issues.get(1).getIssueId());
    }

    @Test
    @DisplayName("ID로 이슈 단건 조회")
    void findByIssueId() {
        // given
        Issue issue = new Issue(PROJECT_ID, "이슈 제목", "설명", REPORTER_ID);
        issueRepository.save(issue);

        // when
        Issue found = issueRepository.findByIssueId(issue.getIssueId());

        // then
        assertNotNull(found);
        assertEquals("이슈 제목", found.getTitle());
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 null 반환")
    void findByIssueIdReturnsNullWhenNotFound() {
        // when
        Issue found = issueRepository.findByIssueId(999L);

        // then
        assertNull(found);
    }

    @Test
    @DisplayName("프로젝트 ID로 조회 시 해당 프로젝트 이슈만 반환")
    void findByProjectId() {
        // given
        issueRepository.save(new Issue(PROJECT_ID, "프로젝트1 이슈", "설명", REPORTER_ID));
        issueRepository.save(new Issue(2L, "프로젝트2 이슈", "설명", REPORTER_ID));

        // when
        List<Issue> issues = issueRepository.findByProjectId(PROJECT_ID);

        // then
        assertEquals(1, issues.size());
        assertEquals(PROJECT_ID, issues.get(0).getProjectId());
    }

    @Test
    @DisplayName("담당자 ID로 조회 시 해당 담당자의 이슈만 반환")
    void findByAssigneeId() {
        // given
        Issue assigned = new Issue(PROJECT_ID, "담당 이슈", "설명", REPORTER_ID);
        issueRepository.save(assigned);
        assigned = issueRepository.findByIssueId(assigned.getIssueId());
        assigned.assignTo(ASSIGNEE_ID);
        issueRepository.update(assigned);

        issueRepository.save(new Issue(PROJECT_ID, "미담당 이슈", "설명", REPORTER_ID));

        // when
        List<Issue> issues = issueRepository.findByAssigneeId(ASSIGNEE_ID);

        // then
        assertEquals(1, issues.size());
        assertEquals(ASSIGNEE_ID, issues.get(0).getAssigneeId());
    }

    @Test
    @DisplayName("보고자 ID로 조회 시 해당 보고자의 이슈만 반환")
    void findByReporterId() {
        // given
        issueRepository.save(new Issue(PROJECT_ID, "이슈1", "설명", REPORTER_ID));
        issueRepository.save(new Issue(PROJECT_ID, "이슈2", "설명", 99L));

        // when
        List<Issue> issues = issueRepository.findByReporterId(REPORTER_ID);

        // then
        assertEquals(1, issues.size());
        assertEquals(REPORTER_ID, issues.get(0).getReporterId());
    }

    @Test
    @DisplayName("상태로 조회 시 해당 상태의 이슈만 반환")
    void findByStatus() {
        // given
        Issue newIssue = new Issue(PROJECT_ID, "새 이슈", "설명", REPORTER_ID);
        issueRepository.save(newIssue);

        Issue assignedIssue = new Issue(PROJECT_ID, "담당 이슈", "설명", REPORTER_ID);
        issueRepository.save(assignedIssue);
        assignedIssue = issueRepository.findByIssueId(assignedIssue.getIssueId());
        assignedIssue.assignTo(ASSIGNEE_ID);
        issueRepository.update(assignedIssue);

        // when
        List<Issue> newIssues = issueRepository.findByStatus(Status.NEW);
        List<Issue> assignedIssues = issueRepository.findByStatus(Status.ASSIGNED);

        // then
        assertEquals(1, newIssues.size());
        assertEquals(1, assignedIssues.size());
    }

    @Test
    @DisplayName("우선순위로 조회 시 해당 우선순위의 이슈만 반환")
    void findByPriority() {
        // given
        Issue majorIssue = new Issue(PROJECT_ID, "MAJOR 이슈", "설명", REPORTER_ID);
        issueRepository.save(majorIssue);

        Issue criticalIssue = new Issue(PROJECT_ID, "CRITICAL 이슈", "설명", REPORTER_ID);
        issueRepository.save(criticalIssue);
        criticalIssue = issueRepository.findByIssueId(criticalIssue.getIssueId());
        criticalIssue.setPriority(Priority.CRITICAL);
        issueRepository.update(criticalIssue);

        // when
        List<Issue> majorIssues = issueRepository.findByPriority(Priority.MAJOR);
        List<Issue> criticalIssues = issueRepository.findByPriority(Priority.CRITICAL);

        // then
        assertEquals(1, majorIssues.size());
        assertEquals(1, criticalIssues.size());
    }

    @Test
    @DisplayName("이슈 수정 시 변경된 내용이 저장소에 반영된다")
    void updatePersistsChanges() {
        // given
        Issue issue = new Issue(PROJECT_ID, "원래 제목", "설명", REPORTER_ID);
        issueRepository.save(issue);
        issue = issueRepository.findByIssueId(issue.getIssueId());

        // when
        issue.setTitle("수정된 제목");
        assertTrue(issueRepository.update(issue));

        // then
        Issue updated = issueRepository.findByIssueId(issue.getIssueId());
        assertEquals("수정된 제목", updated.getTitle());
    }

    @Test
    @DisplayName("존재하지 않는 이슈 수정 시 false 반환")
    void updateReturnsFalseWhenNotFound() {
        // given
        Issue nonExistent = new Issue(PROJECT_ID, "없는 이슈", "설명", REPORTER_ID);
        nonExistent.setIssueId(999L);

        // when
        boolean result = issueRepository.update(nonExistent);

        // then
        assertFalse(result);
    }
}