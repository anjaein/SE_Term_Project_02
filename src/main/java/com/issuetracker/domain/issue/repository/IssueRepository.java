package com.issuetracker.domain.issue.repository;

import com.google.gson.reflect.TypeToken;
import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.domain.issue.enums.Status;
import com.issuetracker.domain.issue.enums.Priority;
import com.issuetracker.global.common.JsonFileManager;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;


public class IssueRepository {
    private static final String FILE_PATH = "data/issues.json";
    private static final Type TYPE = new TypeToken<List<Issue>>(){}.getType();

    public List<Issue> findAll(){
        return JsonFileManager.readList(FILE_PATH, TYPE);
    }

    public List<Issue> findByProjectId(Long projectId){
        return findAll().stream()
                .filter(issue -> issue.getProjectId().equals(projectId))
                .collect(Collectors.toList());
    }

    public List<Issue> findByAssigneeId(Long assigneeId){
        return findAll().stream()
                .filter(issue -> issue.getAssigneeId().equals(assigneeId))
                .collect(Collectors.toList());
    }

    public List<Issue> findByReporterId(Long reporterId){
        return findAll().stream()
                .filter(issue -> issue.getReporterId().equals(reporterId))
                .collect(Collectors.toList());
    }

    public List<Issue> findByStatus(Status status){
        return findAll().stream()
                .filter(issue -> issue.getStatus().equals(status))
                .collect(Collectors.toList());
    }

    public List<Issue> findByPriority(Priority priority){
        return findAll().stream()
                .filter(issue -> issue.getPriority().equals(priority))
                .collect(Collectors.toList());
    }






    public boolean save(Issue issue){
        try{
            List<Issue> issues = findAll();
            Long newId = issues.stream()
                    .mapToLong(Issue::getIssueId)
                    .max()
                    .orElse(0L) + 1L;
            issue.setIssueId(newId);
            issues.add(issue);
            JsonFileManager.writeList(FILE_PATH, issues);
            return true;
        } catch(Exception e){
            return false;
        }
    }
}
