package com.issuetracker.domain.project.repository;

import com.issuetracker.domain.project.enums.Role;
import com.issuetracker.domain.project.entity.ProjectMember;
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

class ProjectMemberRepositoryTest {

    private static final Path MEMBERS_FILE = Path.of("data", "project_members.json");
    private static final Long PROJECT_ID = 1L;
    private static final Long OTHER_PROJECT_ID = 2L;

    private final ProjectMemberRepository memberRepository = new JsonProjectMemberRepository();

    private String originalJson;

    @BeforeEach
    void setUp() throws IOException {
        originalJson = Files.exists(MEMBERS_FILE)
                ? Files.readString(MEMBERS_FILE, StandardCharsets.UTF_8)
                : null;

        Files.createDirectories(MEMBERS_FILE.getParent());
        Files.writeString(MEMBERS_FILE, "[]", StandardCharsets.UTF_8);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (originalJson == null) {
            Files.deleteIfExists(MEMBERS_FILE);
        } else {
            Files.writeString(MEMBERS_FILE, originalJson, StandardCharsets.UTF_8);
        }
    }

    @Test
    @DisplayName("프로젝트 멤버 저장 성공: 저장됨")
    void saveStoresMember() {
        assertTrue(memberRepository.save(new ProjectMember(PROJECT_ID, 10L, Role.PL)));

        assertEquals(1, memberRepository.findAll().size());
    }

    @Test
    @DisplayName("프로젝트별 멤버 조회 성공: 해당 프로젝트의 멤버만 반환")
    void findByProjectIdReturnsOnlyThatProject() {
        memberRepository.save(new ProjectMember(PROJECT_ID, 10L, Role.PL));
        memberRepository.save(new ProjectMember(PROJECT_ID, 20L, Role.DEV));
        memberRepository.save(new ProjectMember(OTHER_PROJECT_ID, 30L, Role.DEV));

        List<ProjectMember> members = memberRepository.findByProjectId(PROJECT_ID);

        assertEquals(2, members.size());
        assertTrue(members.stream().allMatch(m -> m.getProjectId().equals(PROJECT_ID)));
    }

    @Test
    @DisplayName("프로젝트 멤버 단건 조회 성공: (projectId, accountId) 일치하는 데이터 반환")
    void findByProjectIdAndAccountIdReturnsMember() {
        memberRepository.save(new ProjectMember(PROJECT_ID, 10L, Role.PL));

        ProjectMember found = memberRepository.findByProjectIdAndAccountId(PROJECT_ID, 10L);

        assertNotNull(found);
        assertEquals(Role.PL, found.getRole());
    }

    @Test
    @DisplayName("프로젝트 멤버 전체 조회 성공: 저장된 모든 멤버 반환")
    void findAllReturnsAllMembers() {
        memberRepository.save(new ProjectMember(PROJECT_ID, 10L, Role.PL));
        memberRepository.save(new ProjectMember(OTHER_PROJECT_ID, 20L, Role.DEV));

        List<ProjectMember> all = memberRepository.findAll();

        assertEquals(2, all.size());
    }
}
