package com.issuetracker;

import com.issuetracker.domain.account.controller.AccountController;
import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.account.repository.AccountRepository;
import com.issuetracker.domain.account.service.AccountService;
import com.issuetracker.domain.comment.controller.CommentController;
import com.issuetracker.domain.comment.repository.CommentRepository;
import com.issuetracker.domain.comment.service.CommentService;
import com.issuetracker.domain.issue.controller.IssueController;
import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.domain.issue.repository.IssueRepository;
import com.issuetracker.domain.issue.service.IssueService;
import com.issuetracker.domain.project.controller.ProjectController;
import com.issuetracker.domain.project.repository.ProjectMemberRepository;
import com.issuetracker.domain.project.repository.ProjectRepository;
import com.issuetracker.domain.project.service.ProjectService;
import com.issuetracker.domain.recommend.controller.RecommendController;
import com.issuetracker.domain.recommend.service.RecommendService;
import com.issuetracker.global.common.SessionManager;

import java.util.Comparator;

public class Main {
    public static void main(String[] args) {

        AccountRepository accountRepository = new AccountRepository();
        ProjectRepository projectRepository = new ProjectRepository();
        ProjectMemberRepository projectMemberRepository = new ProjectMemberRepository();
        IssueRepository issueRepository = new IssueRepository();
        CommentRepository commentRepository = new CommentRepository();

        SessionManager sessionManager = new SessionManager();

        AccountService accountService = new AccountService(accountRepository);
        ProjectService projectService = new ProjectService(projectRepository, projectMemberRepository);
        IssueService issueService = new IssueService(issueRepository, projectMemberRepository);
        CommentService commentService = new CommentService(commentRepository, accountRepository, issueRepository);

        RecommendService recommendService = new RecommendService(issueRepository);
        RecommendController recommendController = new RecommendController(recommendService, accountRepository);

        AccountController accountController = new AccountController(accountService, sessionManager);
        ProjectController projectController = new ProjectController(projectService, accountController, sessionManager);
        IssueController issueController = new IssueController(issueService, sessionManager, recommendService);
        CommentController commentController = new CommentController(commentService, sessionManager);

        // 1. 초기화된 admin으로 로그인 시도
        System.out.println("--- 1. login test ---");
        boolean loginResult = accountController.login("admin", "admin123");

        if (loginResult) {
            // 2. 관리자 권한으로 다른 계정들 생성 테스트
            System.out.println("\n--- 2. Account Creation Test ---");
            accountController.createAccount("pl1", "1234", Role.PL);
            accountController.createAccount("dev1", "1234", Role.DEV);
            accountController.createAccount("tester1", "1234", Role.TESTER);

            // 3. 로그아웃 후 생성된 계정으로 재접속 테스트
            System.out.println("\n--- 3. General Account Login Test ---");
            accountController.logout();
            accountController.login("pl1", "1234");
        }

        // 4. admin으로 프로젝트 생성 및 자기 자신이 멤버로 추가됐는지 확인
        System.out.println("\n--- 4. Admin Create Project Test ---");
        accountController.logout();
        accountController.login("admin", "admin123");
        projectController.createProject("Project-A");
        projectController.printProjectMembers(1L);

        // 5. admin이 다른 멤버(dev1) 추가
        System.out.println("\n--- 5. Admin Add Member Test ---");
        projectController.addProjectMember(1L, "dev1", Role.DEV);
        projectController.printProjectMembers(1L);

        // 5.1 admin이 존재하지 않는 멤버 추가 (실패해야 함)
        System.out.println("\n--- 5.1 Admin Adds Not Existing Member Test ---");
        projectController.addProjectMember(1L, "kkkkkk", Role.DEV);
        accountController.logout();

        // 6. admin이 아닌 유저(pl1)로 프로젝트 생성 시도 (실패해야 함)
        System.out.println("\n--- 6. Non-Admin Create Project Test (should fail) ---");
        accountController.login("pl1", "1234");
        projectController.createProject("Project-B");

        // 7. admin이 아닌 유저(pl1)로 멤버 추가 시도 (실패해야 함)
        System.out.println("\n--- 7. Non-Admin Add Member Test (should fail) ---");
        projectController.addProjectMember(1L, "tester1", Role.TESTER);
        accountController.logout();

        // 8. 로그인 안 한 상태로 프로젝트 생성 시도 (실패해야 함)
        System.out.println("\n--- 8. Not Logged In Create Project Test (should fail) ---");
        projectController.createProject("Project-C");

        // 9. 로그인 안 한 상태로 멤버 추가 시도 (실패해야 함)
        System.out.println("\n--- 9. Not Logged In Add Member Test (should fail) ---");
        projectController.addProjectMember(1L, "dev1", Role.DEV);

        // 10. 로그인 안 한 상태로 이슈 생성 시도 (실패해야 함)
        System.out.println("\n--- 10. Not Logged In Create Issue Test (should fail) ---");
        issueController.createIssue(1L, "Bug-1", "something is broken", 2L);

        // 11. 프로젝트 멤버인 dev1으로 이슈 생성 (성공해야 함)
        System.out.println("\n--- 11. Member Create Issue Test ---");
        accountController.login("dev1", "1234");
        Long dev1Id = accountController.getAccountIdByUsername("dev1");
        issueController.createIssue(1L, "Bug-1", "something is broken", dev1Id);

        // 12. reporterId를 다른 유저 ID로 전달 (실패해야 함 - not authorized)
        System.out.println("\n--- 12. Wrong reporterId Test (should fail) ---");
        issueController.createIssue(1L, "Bug-2", "another bug", dev1Id + 999L);

        // 13. 프로젝트 멤버가 아닌 유저(tester1)로 이슈 생성 시도 (실패해야 함)
        System.out.println("\n--- 13. Non-Member Create Issue Test (should fail) ---");
        accountController.logout();
        accountController.login("tester1", "1234");
        Long tester1Id = accountController.getAccountIdByUsername("tester1");
        issueController.createIssue(1L, "Bug-3", "tester bug", tester1Id);
        accountController.logout();

        // 14. PDF 예제 시나리오: issue 등록 -> assign -> fixed -> resolved -> closed
        System.out.println("\n--- 14. PDF Issue Scenario Test ---");

        // 14.1 admin이 PL과 tester를 프로젝트 멤버로 추가
        accountController.login("admin", "admin123");
        projectController.addProjectMember(1L, "pl1", Role.PL);
        projectController.addProjectMember(1L, "tester1", Role.TESTER);
        projectController.printProjectMembers(1L);
        accountController.logout();

        // 14.2 tester1이 이슈 등록
        accountController.login("tester1", "1234");
        issueController.createIssue(1L, "Login button error", "The login button does not respond.", tester1Id);
        Issue scenarioIssue = getLatestIssue(issueRepository);
        Long scenarioIssueId = scenarioIssue.getIssueId();
        issueController.printIssueDetail(scenarioIssueId);
        accountController.logout();

        // 14.3 PL이 NEW 이슈를 dev1에게 배정
        accountController.login("pl1", "1234");
        issueController.assignIssue(scenarioIssueId, dev1Id);
        issueController.printIssueDetail(scenarioIssueId);
        accountController.logout();

        // 14.4 dev1이 수정 완료 처리
        accountController.login("dev1", "1234");
        issueController.fixIssue(scenarioIssueId);
        issueController.printIssueDetail(scenarioIssueId);
        accountController.logout();

        // 14.5 reporter인 tester1이 수정 확인 후 resolved 처리
        accountController.login("tester1", "1234");
        issueController.resolveIssue(scenarioIssueId);
        issueController.printIssueDetail(scenarioIssueId);
        accountController.logout();

        // 14.6 PL이 resolved 이슈를 closed 처리
        accountController.login("pl1", "1234");
        issueController.closeIssue(scenarioIssueId);
        issueController.printIssueDetail(scenarioIssueId);
        accountController.logout();

        // 15. 프로젝트 name, 이슈 title이 null이면 생성 못함 (실패해야 함)
        System.out.println("\n--- 15. project name, issue title null test (should fail) ---");
        issueController.createIssue(1L, "", "another bug", dev1Id + 999L);
        accountController.logout();
        accountController.login("admin", "admin123");
        projectController.createProject("");
        accountController.logout();

        // 16. 댓글 작성 테스트
        System.out.println("\n--- 16. Create Comment Test ---");
        accountController.login("dev1", "1234");
        commentController.createComment(1L, "This is a test comment from dev1");

        // 17. 동일 사용자가 다시 댓글 작성
        System.out.println("\n--- 17. Create Another Comment Test ---");
        commentController.createComment(1L, "This is another comment from dev1");

        // 18. 댓글 조회 테스트
        System.out.println("\n--- 18. List Comments Test ---");
        commentController.listComments(1L);

        // 19. 댓글 수정 테스트 (자신의 댓글만 가능)
        System.out.println("\n--- 19. Update Comment Test ---");
        commentController.updateComment(1L, "Updated comment content from dev1");

        // 20. 댓글 조회 (수정 확인)
        System.out.println("\n--- 20. List Comments After Update ---");
        commentController.listComments(1L);

        // 21. 다른 사용자가 댓글 수정 시도 (실패)
        System.out.println("\n--- 21. Update Other's Comment Test (should fail) ---");
        accountController.logout();
        accountController.login("tester1", "1234");
        commentController.updateComment(1L, "Hacked comment");

        // 22. 댓글 삭제 테스트 (자신의 댓글만 가능)
        System.out.println("\n--- 22. Delete Comment Test ---");
        accountController.logout();
        accountController.login("dev1", "1234");
        commentController.deleteComment(1L);

        // 23. 삭제 후 댓글 조회
        System.out.println("\n--- 23. List Comments After Delete ---");
        commentController.listComments(1L);

        // 24. admin이 다른 사용자의 댓글 삭제 (성공)
        System.out.println("\n--- 24. Admin Delete Other's Comment Test ---");
        accountController.logout();
        accountController.login("admin", "admin123");
        commentController.deleteComment(2L);

        // 25. 최종 댓글 조회
        System.out.println("\n--- 25. Final List Comments ---");
        commentController.listComments(1L);

        accountController.logout();
    }

    private static Issue getLatestIssue(IssueRepository issueRepository) {
        return issueRepository.findAll().stream()
                .max(Comparator.comparing(Issue::getIssueId))
                .orElseThrow();
    }
}