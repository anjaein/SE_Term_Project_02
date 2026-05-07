package com.issuetracker.domain.issue.repository;

import com.google.gson.reflect.TypeToken;
import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.global.common.JsonFileManager;

import java.lang.reflect.Type;
import java.util.List;

public class IssueRepository {
    private static final String FILE_PATH = "data/issues.json";
    private static final Type TYPE = new TypeToken<List<Issue>>(){}.getType();

    public List<Issue> findAll(){
        return JsonFileManager.readList(FILE_PATH, TYPE);
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
