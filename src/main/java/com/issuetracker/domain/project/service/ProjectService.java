package com.issuetracker.domain.project.service;

import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.project.entity.Project;
import com.issuetracker.domain.project.entity.ProjectMember;
import com.issuetracker.domain.project.repository.ProjectMemberRepository;
import com.issuetracker.domain.project.repository.ProjectRepository;
import com.issuetracker.global.common.Response;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectValidator projectValidator;

    public Response<Project> createProject(String name, Long accountId, Role role){
        // 요청에 누락은 없는지
        String missingParams = projectValidator.checkNonNull(name, accountId, role);
        if(missingParams != null){
            return Response.fail(missingParams);
        }

        // name 필수값 검증
        String blankName = projectValidator.checkNonBlank(name, "Project name");
        if(blankName != null){
            return Response.fail(blankName);
        }

        Project project = new Project(name, accountId);
        if(!projectRepository.save(project)){
            return Response.fail("Failed to create the project.");
        }

        Response<ProjectMember> memberResult = addProjectMember(project.getProjectId(), accountId, role);
        if(!memberResult.isSuccess()){
            return Response.fail(memberResult.getMessage());
        }
        return Response.success("Project created.", project);
    }

    public Response<ProjectMember> addProjectMember(Long projectId, Long accountId, Role role) {
        // 요청에 누락은 없는지
        String missingParams = projectValidator.checkNonNull(projectId, accountId, role);
        if(missingParams != null){
            return Response.fail(missingParams);
        }

        // 멤버를 추가할 project가 존재하는지 확인
        String missingProject = projectValidator.checkProjectExists(projectId);
        if(missingProject != null){
            return Response.fail(missingProject);
        }

        ProjectMember projectMember = new ProjectMember(projectId, accountId, role);
        if(!projectMemberRepository.save(projectMember)){
            return Response.fail("Failed to add the project member.");
        }
        return Response.success("Project member added.", projectMember);
    }

    public Response<List<Project>> getAllProjects() {
        return Response.success("Projects retrieved.", projectRepository.findAll());
    }

    public Response<List<ProjectMember>> getProjectMembers(Long projectId) {
        return Response.success("Project members retrieved.", projectMemberRepository.findByProjectId(projectId));
    }
}
