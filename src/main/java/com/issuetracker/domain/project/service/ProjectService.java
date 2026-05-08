package com.issuetracker.domain.project.service;

import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.project.entity.Project;
import com.issuetracker.domain.project.entity.ProjectMember;
import com.issuetracker.domain.project.repository.ProjectMemberRepository;
import com.issuetracker.domain.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public boolean createProject(String name, Long accountId, Role role){
        Project project = new Project(name, accountId);
        if(!projectRepository.save(project)){
            return false;
        }

        return addProjectMember(project.getProjectId(), accountId, role);
    }

    public boolean addProjectMember(Long projectId, Long accountId, Role role) {
        ProjectMember projectMember = new ProjectMember(projectId, accountId, role);
        return projectMemberRepository.save(projectMember);
    }

    public List<ProjectMember> getProjectMembers(Long projectId) {
        return projectMemberRepository.findByProjectId(projectId);
    }
}
