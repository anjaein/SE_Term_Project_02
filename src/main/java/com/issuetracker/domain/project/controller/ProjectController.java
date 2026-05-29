package com.issuetracker.domain.project.controller;

import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.service.AccountService;
import com.issuetracker.domain.project.entity.Project;
import com.issuetracker.domain.project.entity.ProjectMember;
import com.issuetracker.domain.project.enums.Role;
import com.issuetracker.domain.project.service.ProjectService;
import com.issuetracker.global.common.Response;
import com.issuetracker.global.common.SessionManager;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;
    private final AccountService accountService;
    private final SessionManager sessionManager;

    public Response<Project> createProject(String name){
        Account currentUser = sessionManager.getLoggedInAccount();
        if(currentUser == null){
            return Response.fail("You are not logged in.");
        }
        if(!currentUser.isAdmin()){
            return Response.fail("You are not allowed to create the project.");
        }
        return projectService.createProject(name, currentUser.getAccountId(), Role.ADMIN);
    }

    public Response<ProjectMember> addProjectMember(Long projectId, String username, Role role){
        Account currentUser = sessionManager.getLoggedInAccount();
        if(currentUser == null){
            return Response.fail("You are not logged in.");
        }
        if(!currentUser.isAdmin()){
            return Response.fail("You are not allowed to add a member.");
        }
        Response<Long> accountIdResult = accountService.getAccountIdByUsername(username);
        if(!accountIdResult.isSuccess()){
            return Response.fail("User does not exist.");
        }
        return projectService.addProjectMember(projectId, accountIdResult.getData(), role);
    }

    public Response<List<ProjectMember>> listProjectMembers(Long projectId) {
        if (sessionManager.getLoggedInAccount() == null) {
            return Response.fail("You are not logged in.");
        }
        return projectService.getProjectMembers(projectId);
    }
}