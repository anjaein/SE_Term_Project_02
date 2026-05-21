package com.issuetracker.domain.project.repository;

import com.google.gson.reflect.TypeToken;
import com.issuetracker.domain.project.entity.Project;
import com.issuetracker.global.common.JsonFileManager;

import java.lang.reflect.Type;
import java.util.List;

public class JsonProjectRepository implements ProjectRepository {
    private static final String FILE_PATH = "data/projects.json";
    private static final Type TYPE = new TypeToken<List<Project>>(){}.getType();

    @Override
    public List<Project> findAll() {
        return JsonFileManager.readList(FILE_PATH, TYPE);
    }

    @Override
    public Project findByProjectId(Long projectId) {
        return findAll().stream()
                .filter(m -> m.getProjectId().equals(projectId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean save(Project project) {
        if (findByProjectId(project.getProjectId()) != null) {
            return false;
        }
        List<Project> projects = findAll();
        Long newId = projects.stream()
                .mapToLong(Project::getProjectId)
                .max()
                .orElse(0L) + 1L;
        project.setProjectId(newId);
        projects.add(project);
        JsonFileManager.writeList(FILE_PATH, projects);
        return true;
    }
}