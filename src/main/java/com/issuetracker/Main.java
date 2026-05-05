package com.issuetracker;

import com.issuetracker.domain.account.controller.AccountController;
import com.issuetracker.domain.account.enums.Role;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        AccountController controller = new AccountController();

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
    }
}