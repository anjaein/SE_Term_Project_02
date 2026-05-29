package com.issuetracker.domain.project.service;

import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.project.entity.Project;
import com.issuetracker.domain.project.entity.ProjectMember;
import com.issuetracker.domain.project.repository.ProjectMemberRepository;
import com.issuetracker.domain.project.repository.ProjectRepository;
import com.issuetracker.global.common.Response;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
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

    public Response<List<ProjectMember>> getProjectMembers(Long projectId) {
        return Response.success("Project members retrieved.", projectMemberRepository.findByProjectId(projectId));
    }

    // 전체 프로젝트 목록을 조회하여 반환하는 메서드
    public Response<List<Project>> getMyProjects(Long accountId) {
        // 1. Repository에서 전체 프로젝트 목록을 가져옵니다.
        List<Project> allProjects = projectRepository.findAll();

        // 2. 내가 속한 프로젝트만 골라담을 빈 바구니(List)를 준비합니다.
        List<Project> myProjects = new ArrayList<>();

        // 3. 전체 프로젝트를 하나씩 꺼내보면서 검사합니다.
        for (Project project : allProjects) {
            Long projectId = project.getProjectId();

            // 해당 프로젝트(projectId)에 내 계정(accountId)이 멤버로 등록되어 있는지 확인합니다.
            ProjectMember member = projectMemberRepository.findByProjectIdAndAccountId(projectId, accountId);

            // 만약 member가 null이 아니라면(즉, 멤버로 존재한다면) 내 프로젝트 바구니에 담습니다.
            // (만약 반환 타입이 Optional이라면 member.isPresent() 등으로 체크하시면 됩니다.)
            if (member != null) {
                myProjects.add(project);
            }
        }

        // 4. 안전하게 필터링이 끝난 '내 프로젝트' 목록만 UI로 전달합니다.
        return Response.success("My projects retrieved.", myProjects);
    }
    public Response<List<Project>> getAllProjects() {
        return Response.success("All projects retrieved.", projectRepository.findAll());
    }


}