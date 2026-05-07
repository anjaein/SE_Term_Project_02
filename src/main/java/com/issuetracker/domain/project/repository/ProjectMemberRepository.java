package com.issuetracker.domain.project.repository;

import com.google.gson.reflect.TypeToken;
import com.issuetracker.domain.project.entity.ProjectMember;
import com.issuetracker.global.common.JsonFileManager;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectMemberRepository {
    private static final String FILE_PATH = "data/project_members.json";
    private static final Type TYPE = new TypeToken<List<ProjectMember>>(){}.getType();

    public List<ProjectMember> findAll(){
        return JsonFileManager.readList(FILE_PATH, TYPE);
    }

    public List<ProjectMember> findByProjectId(Long projectId){
        return findAll().stream()
                .filter(m -> m.getProjectId().equals(projectId))
                .collect(Collectors.toList());
    }

    public ProjectMember findByProjectIdAndAccountId(Long projectId, Long accountId){
        return findAll().stream()
                .filter(m -> m.getProjectId().equals(projectId) && m.getAccountId().equals(accountId))
                .findFirst()
                .orElse(null);
    }

    public boolean save(ProjectMember projectMember){
        if(findByProjectIdAndAccountId(projectMember.getProjectId(), projectMember.getAccountId()) != null){
            return false;
        }
        List<ProjectMember> projectMembers = findAll();
        projectMembers.add(projectMember);
        JsonFileManager.writeList(FILE_PATH, projectMembers);
        return true;
    }
}
