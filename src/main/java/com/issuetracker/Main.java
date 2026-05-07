package com.issuetracker;

import com.issuetracker.domain.account.controller.AccountController;
import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.issue.controller.IssueController;
import com.issuetracker.domain.project.controller.ProjectController;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        AccountController controller = new AccountController();
        ProjectController projectController = new ProjectController(controller);

        // 1. 초기화된 admin으로 로그인 시도
        System.out.println("--- 1. login test ---");
        boolean loginResult = controller.login("admin", "admin123");

        if (loginResult) {
            // 2. 관리자 권한으로 다른 계정들 생성 테스트
            System.out.println("\n--- 2. Account Creation Test ---");
            controller.createAccount("pl1", "1234", Role.PL);
            controller.createAccount("dev1", "1234", Role.DEV);
            controller.createAccount("tester1", "1234", Role.TESTER);

            // 3. 로그아웃 후 생성된 계정으로 재접속 테스트
            System.out.println("\n--- 3. General Account Login Test ---");
            controller.logout();
            controller.login("pl1", "1234");
        }

        // 4. admin으로 프로젝트 생성 및 자기 자신이 멤버로 추가됐는지 확인
        System.out.println("\n--- 4. Admin Create Project Test ---");
        controller.logout();
        controller.login("admin", "admin123");
        projectController.createProject("Project-A");
        projectController.printProjectMembers(1L);

        // 5. admin이 다른 멤버(dev1) 추가
        System.out.println("\n--- 5. Admin Add Member Test ---");
        projectController.addProjectMember(1L, "dev1", Role.DEV);
        projectController.printProjectMembers(1L);

        // 5.1 admin이 존재하지 않는 멤버 추가 (실패해야 함)
        System.out.println("\n--- 5.1 Admin Adds Not Existing Member Test ---");
        projectController.addProjectMember(1L, "kkkkkk", Role.DEV);
        controller.logout();

        // 6. admin이 아닌 유저(pl1)로 프로젝트 생성 시도 (실패해야 함)
        System.out.println("\n--- 6. Non-Admin Create Project Test (should fail) ---");
        controller.login("pl1", "1234");
        projectController.createProject("Project-B");

        // 7. admin이 아닌 유저(pl1)로 멤버 추가 시도 (실패해야 함)
        System.out.println("\n--- 7. Non-Admin Add Member Test (should fail) ---");
        projectController.addProjectMember(1L, "tester1", Role.TESTER);
        controller.logout();

        // 8. 로그인 안 한 상태로 프로젝트 생성 시도 (실패해야 함)
        System.out.println("\n--- 8. Not Logged In Create Project Test (should fail) ---");
        projectController.createProject("Project-C");

        // 9. 로그인 안 한 상태로 멤버 추가 시도 (실패해야 함)
        System.out.println("\n--- 9. Not Logged In Add Member Test (should fail) ---");
        projectController.addProjectMember(1L, "dev1", Role.DEV);

        IssueController issueController = new IssueController(controller);

        // 10. 로그인 안 한 상태로 이슈 생성 시도 (실패해야 함)
        System.out.println("\n--- 10. Not Logged In Create Issue Test (should fail) ---");
        issueController.createIssue(1L, "Bug-1", "something is broken", 2L);

        // 11. 프로젝트 멤버인 dev1으로 이슈 생성 (성공해야 함)
        System.out.println("\n--- 11. Member Create Issue Test ---");
        controller.login("dev1", "1234");
        Long dev1Id = controller.getAccountIdByUsername("dev1");
        issueController.createIssue(1L, "Bug-1", "something is broken", dev1Id);

        // 12. reporterId를 다른 유저 ID로 전달 (실패해야 함 - not authorized)
        System.out.println("\n--- 12. Wrong reporterId Test (should fail) ---");
        issueController.createIssue(1L, "Bug-2", "another bug", dev1Id + 999L);

        // 13. 프로젝트 멤버가 아닌 유저(tester1)로 이슈 생성 시도 (실패해야 함)
        System.out.println("\n--- 13. Non-Member Create Issue Test (should fail) ---");
        controller.logout();
        controller.login("tester1", "1234");
        Long tester1Id = controller.getAccountIdByUsername("tester1");
        issueController.createIssue(1L, "Bug-3", "tester bug", tester1Id);
        controller.logout();
    }
}