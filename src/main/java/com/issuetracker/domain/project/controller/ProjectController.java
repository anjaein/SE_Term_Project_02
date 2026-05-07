package com.issuetracker.domain.project.controller;

import com.issuetracker.domain.account.controller.AccountController;
import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.project.entity.ProjectMember;
import com.issuetracker.domain.project.service.ProjectService;

import java.util.List;

public class ProjectController {
    private ProjectService projectService = new ProjectService();
    private AccountController accountController;

    public ProjectController(AccountController accountController) {
        this.accountController = accountController;
    }

    public void createProject(String name){
        Account currentUser = accountController.getLoggedInAccount();
        if(currentUser == null){
            notifyError("You are not logged in.");
            return;
        }
        Role currentUserRole =  currentUser.getRole();
        if(currentUserRole != Role.ADMIN){
            notifyError("You are not allowed to create the Project.");
            return;
        }
        if(projectService.createProject(name, currentUser.getAccountId(), currentUserRole)){
            notifySuccess("Your project has been created.");
        } else{
            notifyError("Failed to create the Project.");
        }
    }

    public void addProjectMember(Long projectId, String username, Role role){
        Account currentUser = accountController.getLoggedInAccount();
        if(currentUser == null){
            notifyError("You are not logged in.");
            return;
        }
        Role currentUserRole =  currentUser.getRole();
        if(currentUserRole != Role.ADMIN){
            notifyError("You are not allowed to add the Member.");
            return;
        }
        Long accountId = accountController.getAccountIdByUsername(username);
        if(accountId == null){
            notifyError("User does not exist.");
        } else{
            if(projectService.addProjectMember(projectId, accountId, role)){
                notifySuccess("User has been added to the Project.");
            } else{
                notifyError("Failed to add the user to the Project.");
            }
        }
    }

    // 메인 함수 테스트용 임시 출력 함수
    public void printProjectMembers(Long projectId) {
        List<ProjectMember> members = projectService.getProjectMembers(projectId);
        System.out.println("[INFO] Members of project " + projectId + ": " + members.size() + " member(s)");
        for (ProjectMember member : members) {
            System.out.println("  - accountId: " + member.getAccountId() + ", role: " + member.getRole());
        }
    }

    private void notifySuccess(String message) {
        System.out.println("[SUCCESS] " + message);
    }

    private void notifyError(String message) {
        System.out.println("[ERROR] " + message);
    }
}
