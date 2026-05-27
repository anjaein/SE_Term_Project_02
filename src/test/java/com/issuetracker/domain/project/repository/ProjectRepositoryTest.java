package com.issuetracker.domain.project.repository;

import com.issuetracker.domain.project.entity.Project;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectRepositoryTest {

    private static final Path PROJECTS_FILE = Path.of("data", "projects.json");
    private static final Long OWNER_ID = 1L;

    private final ProjectRepository projectRepository = new JsonProjectRepository();

    private String originalJson;

    @BeforeEach
    void setUp() throws IOException {
        originalJson = Files.exists(PROJECTS_FILE)
                ? Files.readString(PROJECTS_FILE, StandardCharsets.UTF_8)
                : null;

        Files.createDirectories(PROJECTS_FILE.getParent());
        Files.writeString(PROJECTS_FILE, "[]", StandardCharsets.UTF_8);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (originalJson == null) {
            Files.deleteIfExists(PROJECTS_FILE);
        } else {
            Files.writeString(PROJECTS_FILE, originalJson, StandardCharsets.UTF_8);
        }
    }

    @Test
    @DisplayName("프로젝트 저장 성공: ID가 순차 부여되고 저장")
    void saveAssignsSequentialIds() {
        assertTrue(projectRepository.save(new Project("Project-A", OWNER_ID)));
        assertTrue(projectRepository.save(new Project("Project-B", OWNER_ID)));

        assertEquals(2, projectRepository.findAll().size());
        assertNotNull(projectRepository.findByProjectId(1L));
        assertNotNull(projectRepository.findByProjectId(2L));
    }

    @Test
    @DisplayName("프로젝트 단건 조회 성공: projectId 일치 데이터 반환")
    void findByProjectIdReturnsProject() {
        projectRepository.save(new Project("Project-A", OWNER_ID));

        Project found = projectRepository.findByProjectId(1L);

        assertNotNull(found);
        assertEquals(1L, found.getProjectId());
    }

    @Test
    @DisplayName("프로젝트 전체 조회 성공: 저장된 모든 프로젝트 반환")
    void findAllReturnsAllProjects() {
        projectRepository.save(new Project("Project-A", OWNER_ID));
        projectRepository.save(new Project("Project-B", OWNER_ID));

        List<Project> all = projectRepository.findAll();

        assertEquals(2, all.size());
    }
}
