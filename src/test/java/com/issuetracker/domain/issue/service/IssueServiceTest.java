package com.issuetracker.domain.issue.service;

import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.domain.issue.enums.Status;
import com.issuetracker.domain.issue.repository.IssueRepository;
import com.issuetracker.domain.project.entity.ProjectMember;
import com.issuetracker.domain.project.repository.ProjectMemberRepository;
import com.issuetracker.global.common.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IssueServiceTest {
    private static final Long PROJECT_ID = 1L;
    private static final Long PL_ID = 10L;
    private static final Long DEV_ID = 20L;
    private static final Long TESTER_ID = 30L;

    private FakeIssueRepository issueRepository;
    private FakeProjectMemberRepository fakeProjectMemberRepository;
    private IssueService issueService;

    @BeforeEach
    void setUp() {
        issueRepository = new FakeIssueRepository();
        fakeProjectMemberRepository = new FakeProjectMemberRepository();
        FakeProjectRepository projectRepository = new FakeProjectRepository();
        
        IssueValidator validator = new IssueValidator(fakeProjectMemberRepository, projectRepository);
        issueService = new IssueService(issueRepository, fakeProjectMemberRepository, validator);
    }

    @Test
    @DisplayName("이슈 생성 실패: title blank")
    void createIssueWithBlankTitle() {
        fakeProjectMemberRepository.addMember(PROJECT_ID, TESTER_ID, Role.TESTER);
        Response<Issue> result = issueService.createIssue(PROJECT_ID, " ", "description", TESTER_ID);
        assertFalse(result.isSuccess());
        assertTrue(issueRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("이슈 생성 실패: description blank")
    void createIssueWithBlankDescription() {
        fakeProjectMemberRepository.addMember(PROJECT_ID, TESTER_ID, Role.TESTER);
        Response<Issue> result = issueService.createIssue(PROJECT_ID, "title", " ", TESTER_ID);
        assertFalse(result.isSuccess());
        assertTrue(issueRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("이슈 생성 실패: 프로젝트 멤버 아님")
    void createIssueByNonProjectMember() {
        Response<Issue> result = issueService.createIssue(PROJECT_ID, "title", "description", TESTER_ID);
        assertFalse(result.isSuccess());
        assertTrue(issueRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("ASSIGN 성공: PL이 DEV에게 assign")
    void assignIssueByPL() {
        fakeProjectMemberRepository.addMember(PROJECT_ID, PL_ID, Role.PL);
        fakeProjectMemberRepository.addMember(PROJECT_ID, DEV_ID, Role.DEV);
        Issue issue = saveNewIssue();
        Response<Issue> result = issueService.assignIssue(issue.getIssueId(), DEV_ID, PL_ID);
        assertTrue(result.isSuccess());
        assertEquals(DEV_ID, issue.getAssigneeId());
        assertEquals(Status.ASSIGNED, issue.getStatus());
    }

    @Test
    @DisplayName("ASSIGN 실패: PL 아님")
    void assignIssueByNonPL() {
        fakeProjectMemberRepository.addMember(PROJECT_ID, DEV_ID, Role.DEV);
        fakeProjectMemberRepository.addMember(PROJECT_ID, TESTER_ID, Role.TESTER);
        Issue issue = saveNewIssue();
        Response<Issue> result = issueService.assignIssue(issue.getIssueId(), DEV_ID, TESTER_ID);
        assertFalse(result.isSuccess());
        assertNull(issue.getAssigneeId());
        assertEquals(Status.NEW, issue.getStatus());
    }

    @Test
    @DisplayName("ASSIGN 실패: assignee가 DEV 아님")
    void assignIssueToNonDev() {
        fakeProjectMemberRepository.addMember(PROJECT_ID, PL_ID, Role.PL);
        fakeProjectMemberRepository.addMember(PROJECT_ID, TESTER_ID, Role.TESTER);
        Issue issue = saveNewIssue();
        Response<Issue> result = issueService.assignIssue(issue.getIssueId(), TESTER_ID, PL_ID);
        assertFalse(result.isSuccess());
        assertNull(issue.getAssigneeId());
        assertEquals(Status.NEW, issue.getStatus());
    }

    @Test
    @DisplayName("FIXED 성공: assignee가 fixed 처리")
    void fixIssueByAssignee() {
        Issue issue = saveAssignedIssue();
        Response<Issue> result = issueService.fixIssue(issue.getIssueId(), DEV_ID);
        assertTrue(result.isSuccess());
        assertEquals(Status.FIXED, issue.getStatus());
        assertEquals(DEV_ID, issue.getFixerId());
        assertNotNull(issue.getFixedDate());
    }

    @Test
    @DisplayName("FIXED 실패: assignee 아님")
    void fixIssueByNonAssignee() {
        Issue issue = saveAssignedIssue();
        Response<Issue> result = issueService.fixIssue(issue.getIssueId(), TESTER_ID);
        assertFalse(result.isSuccess());
        assertEquals(Status.ASSIGNED, issue.getStatus());
        assertNull(issue.getFixerId());
        assertNull(issue.getFixedDate());
    }

    @Test
    @DisplayName("FIXED 실패: ASSIGNED 상태가 아님")
    void fixIssueWhenStatusIsNotAssigned() {
        Issue issue = saveNewIssue();
        Response<Issue> result = issueService.fixIssue(issue.getIssueId(), DEV_ID);
        assertFalse(result.isSuccess());
        assertEquals(Status.NEW, issue.getStatus());
        assertNull(issue.getFixerId());
        assertNull(issue.getFixedDate());
    }

    @Test
    @DisplayName("CLOSED 성공: PL이 closed 처리")
    void closeIssueByPL() {
        fakeProjectMemberRepository.addMember(PROJECT_ID, PL_ID, Role.PL);
        Issue issue = saveResolvedIssue();
        Response<Issue> result = issueService.closeIssue(issue.getIssueId(), PL_ID);
        assertTrue(result.isSuccess());
        assertEquals(Status.CLOSED, issue.getStatus());
        assertNotNull(issue.getClosedDate());
    }

    @Test
    @DisplayName("CLOSED 실패: PL 아님")
    void closeIssueByNonPL() {
        fakeProjectMemberRepository.addMember(PROJECT_ID, TESTER_ID, Role.TESTER);
        Issue issue = saveResolvedIssue();
        Response<Issue> result = issueService.closeIssue(issue.getIssueId(), TESTER_ID);
        assertFalse(result.isSuccess());
        assertEquals(Status.RESOLVED, issue.getStatus());
        assertNull(issue.getClosedDate());
    }

    @Test
    @DisplayName("CLOSED 실패: RESOLVED 상태가 아님")
    void closeIssueWhenStatusIsNotResolved() {
        fakeProjectMemberRepository.addMember(PROJECT_ID, PL_ID, Role.PL);
        Issue issue = saveFixedIssue();
        Response<Issue> result = issueService.closeIssue(issue.getIssueId(), PL_ID);
        assertFalse(result.isSuccess());
        assertEquals(Status.FIXED, issue.getStatus());
        assertNull(issue.getClosedDate());
    }

    private Issue saveNewIssue() {
        Issue issue = new Issue(PROJECT_ID, "title", "description", TESTER_ID);
        issueRepository.save(issue);
        return issue;
    }

    private Issue saveAssignedIssue() {
        Issue issue = saveNewIssue();
        issue.assignTo(DEV_ID);
        issueRepository.update(issue);
        return issue;
    }

    private Issue saveFixedIssue() {
        Issue issue = saveAssignedIssue();
        issue.markAsFixed(DEV_ID);
        issueRepository.update(issue);
        return issue;
    }

    private Issue saveResolvedIssue() {
        Issue issue = saveFixedIssue();
        issue.markAsResolved();
        issueRepository.update(issue);
        return issue;
    }

    private static class FakeIssueRepository implements IssueRepository {
        private final List<Issue> issues = new ArrayList<>();
        private long nextId = 1L;

        @Override
        public List<Issue> findAll() { return issues; }

        @Override
        public boolean save(Issue issue) {
            issue.setIssueId(nextId++);
            issues.add(issue);
            return true;
        }

        @Override
        public Issue findByIssueId(Long issueId) {
            return issues.stream()
                    .filter(issue -> issue.getIssueId().equals(issueId))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<Issue> findByProjectId(Long projectId) { return List.of(); }
        @Override
        public List<Issue> findByAssigneeId(Long assigneeId) { return List.of(); }
        @Override
        public List<Issue> findByReporterId(Long reporterId) { return List.of(); }
        @Override
        public List<Issue> findByStatus(com.issuetracker.domain.issue.enums.Status status) { return List.of(); }
        @Override
        public List<Issue> findByPriority(com.issuetracker.domain.issue.enums.Priority priority) { return List.of(); }

        @Override
        public boolean update(Issue updatedIssue) {
            for(int i = 0; i < issues.size(); i++){
                if(issues.get(i).getIssueId().equals(updatedIssue.getIssueId())){
                    issues.set(i, updatedIssue);
                    return true;
                }
            }
            return false;
        }
    }

    private static class FakeProjectMemberRepository implements ProjectMemberRepository {
        private final List<ProjectMember> members = new ArrayList<>();

        void addMember(Long projectId, Long accountId, Role role) {
            members.add(new ProjectMember(projectId, accountId, role));
        }

        @Override
        public List<ProjectMember> findAll() { return members; }
        @Override
        public List<ProjectMember> findByProjectId(Long projectId) { return List.of(); }
        @Override
        public ProjectMember findByProjectIdAndAccountId(Long projectId, Long accountId) {
            return members.stream()
                    .filter(member -> member.getProjectId().equals(projectId))
                    .filter(member -> member.getAccountId().equals(accountId))
                    .findFirst()
                    .orElse(null);
        }
        @Override
        public boolean save(ProjectMember projectMember) { return true; }
    }

    private static class FakeProjectRepository implements com.issuetracker.domain.project.repository.ProjectRepository {
        @Override
        public List<com.issuetracker.domain.project.entity.Project> findAll() { return List.of(); }
        @Override
        public com.issuetracker.domain.project.entity.Project findByProjectId(Long projectId) { 
            return new com.issuetracker.domain.project.entity.Project("title", 1L);
        }
        @Override
        public boolean save(com.issuetracker.domain.project.entity.Project project) { return true; }
    }
}
