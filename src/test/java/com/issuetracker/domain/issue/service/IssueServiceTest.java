package com.issuetracker.domain.issue.service;

import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.domain.issue.enums.Status;
import com.issuetracker.domain.issue.repository.IssueRepository;
import com.issuetracker.domain.project.entity.ProjectMember;
import com.issuetracker.domain.project.repository.ProjectMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
테스트 목록 : 
- 입력 검증
- 권한 검증
- 상태 전이 검증
- 상태 변경 결과
*/


class IssueServiceTest {
    private static final Long PROJECT_ID = 1L;
    private static final Long PL_ID = 10L;
    private static final Long DEV_ID = 20L;
    private static final Long TESTER_ID = 30L;

    private FakeIssueRepository issueRepository;
    private FakeProjectMemberRepository projectMemberRepository;
    private IssueService issueService;

    @BeforeEach
    void setUp() {
        //테스트용 가짜 이슈 레포 만들고
        issueRepository = new FakeIssueRepository();
        //테스트용 가짜 멤버 정보
        projectMemberRepository = new FakeProjectMemberRepository();
        // 테스트 대상.
        issueService = new IssueService(issueRepository, projectMemberRepository);
    }

    @Test
    @DisplayName("이슈 생성 실패: title blank")
    void createIssueWithBlankTitle() {
        // given
        projectMemberRepository.addMember(PROJECT_ID, TESTER_ID, Role.TESTER);

        // when
        boolean result = issueService.createIssue(PROJECT_ID, " ", "description", TESTER_ID);

        // then
        assertFalse(result);
        assertTrue(issueRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("이슈 생성 실패: description blank")
    void createIssueWithBlankDescription() {
        // given
        projectMemberRepository.addMember(PROJECT_ID, TESTER_ID, Role.TESTER);

        // when
        boolean result = issueService.createIssue(PROJECT_ID, "title", " ", TESTER_ID);

        // then
        assertFalse(result);
        assertTrue(issueRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("이슈 생성 실패: 프로젝트 멤버 아님")
    void createIssueByNonProjectMember() {
        // given: reporter가 프로젝트 멤버가 아니라면

        // when
        boolean result = issueService.createIssue(PROJECT_ID, "title", "description", TESTER_ID);

        // then
        assertFalse(result);
        assertTrue(issueRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("ASSIGN 성공: PL이 DEV에게 assign")
    void assignIssueByPL() {
        // given
        projectMemberRepository.addMember(PROJECT_ID, PL_ID, Role.PL);
        projectMemberRepository.addMember(PROJECT_ID, DEV_ID, Role.DEV);
        Issue issue = saveNewIssue();

        // when
        boolean result = issueService.assignIssue(issue.getIssueId(), DEV_ID, PL_ID);

        // then
        assertTrue(result);
        assertEquals(DEV_ID, issue.getAssigneeId());
        assertEquals(Status.ASSIGNED, issue.getStatus());
    }

    @Test
    @DisplayName("ASSIGN 실패: PL 아님")
    void assignIssueByNonPL() {
        // given
        projectMemberRepository.addMember(PROJECT_ID, DEV_ID, Role.DEV);
        projectMemberRepository.addMember(PROJECT_ID, TESTER_ID, Role.TESTER);
        Issue issue = saveNewIssue();

        // when
        boolean result = issueService.assignIssue(issue.getIssueId(), DEV_ID, TESTER_ID);

        // then
        assertFalse(result);
        assertNull(issue.getAssigneeId());
        assertEquals(Status.NEW, issue.getStatus());
    }

    @Test
    @DisplayName("ASSIGN 실패: assignee가 DEV 아님")
    void assignIssueToNonDev() {
        // given
        projectMemberRepository.addMember(PROJECT_ID, PL_ID, Role.PL);
        projectMemberRepository.addMember(PROJECT_ID, TESTER_ID, Role.TESTER);
        Issue issue = saveNewIssue();

        // when
        boolean result = issueService.assignIssue(issue.getIssueId(), TESTER_ID, PL_ID);

        // then
        assertFalse(result);
        assertNull(issue.getAssigneeId());
        assertEquals(Status.NEW, issue.getStatus());
    }

    @Test
    @DisplayName("FIXED 성공: assignee가 fixed 처리")
    void fixIssueByAssignee() {
        // given
        Issue issue = saveAssignedIssue();

        // when
        boolean result = issueService.fixIssue(issue.getIssueId(), DEV_ID);

        // then
        assertTrue(result);
        assertEquals(Status.FIXED, issue.getStatus());
        assertEquals(DEV_ID, issue.getFixerId());
        assertNotNull(issue.getFixedDate());
    }

    @Test
    @DisplayName("FIXED 실패: assignee 아님")
    void fixIssueByNonAssignee() {
        // given
        Issue issue = saveAssignedIssue();

        // when
        boolean result = issueService.fixIssue(issue.getIssueId(), TESTER_ID);

        // then
        assertFalse(result);
        assertEquals(Status.ASSIGNED, issue.getStatus());
        assertNull(issue.getFixerId());
        assertNull(issue.getFixedDate());
    }

    @Test
    @DisplayName("FIXED 실패: ASSIGNED 상태가 아님")
    void fixIssueWhenStatusIsNotAssigned() {
        // given
        Issue issue = saveNewIssue();

        // when
        boolean result = issueService.fixIssue(issue.getIssueId(), DEV_ID);

        // then
        assertFalse(result);
        assertEquals(Status.NEW, issue.getStatus());
        assertNull(issue.getFixerId());
        assertNull(issue.getFixedDate());
    }

    @Test
    @DisplayName("CLOSED 성공: PL이 closed 처리")
    void closeIssueByPL() {
        // given
        projectMemberRepository.addMember(PROJECT_ID, PL_ID, Role.PL);
        Issue issue = saveResolvedIssue();

        // when
        boolean result = issueService.closeIssue(issue.getIssueId(), PL_ID);

        // then
        assertTrue(result);
        assertEquals(Status.CLOSED, issue.getStatus());
        assertNotNull(issue.getClosedDate());
    }

    @Test
    @DisplayName("CLOSED 실패: PL 아님")
    void closeIssueByNonPL() {
        // given
        projectMemberRepository.addMember(PROJECT_ID, TESTER_ID, Role.TESTER);
        Issue issue = saveResolvedIssue();

        // when
        boolean result = issueService.closeIssue(issue.getIssueId(), TESTER_ID);

        // then
        assertFalse(result);
        assertEquals(Status.RESOLVED, issue.getStatus());
        assertNull(issue.getClosedDate());
    }

    @Test
    @DisplayName("CLOSED 실패: RESOLVED 상태가 아님")
    void closeIssueWhenStatusIsNotResolved() {
        // given
        projectMemberRepository.addMember(PROJECT_ID, PL_ID, Role.PL);
        Issue issue = saveFixedIssue();

        // when
        boolean result = issueService.closeIssue(issue.getIssueId(), PL_ID);

        // then
        assertFalse(result);
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

    //테스트 더블
    private static class FakeIssueRepository extends IssueRepository {
        private final List<Issue> issues = new ArrayList<>();
        private long nextId = 1L;

        @Override
        public List<Issue> findAll() {
            return issues;
        }

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

    //테스트 더블
    private static class FakeProjectMemberRepository extends ProjectMemberRepository {
        private final List<ProjectMember> members = new ArrayList<>();

        void addMember(Long projectId, Long accountId, Role role) {
            members.add(new ProjectMember(projectId, accountId, role));
        }

        @Override
        public ProjectMember findByProjectIdAndAccountId(Long projectId, Long accountId) {
            return members.stream()
                    .filter(member -> member.getProjectId().equals(projectId))
                    .filter(member -> member.getAccountId().equals(accountId))
                    .findFirst()
                    .orElse(null);
        }
    }
}
