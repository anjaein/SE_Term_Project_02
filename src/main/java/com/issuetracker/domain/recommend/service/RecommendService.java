package com.issuetracker.domain.recommend.service;

import com.issuetracker.domain.issue.enums.Status;
import com.issuetracker.domain.issue.repository.IssueRepository;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RecommendService {
    private final IssueRepository issueRepository;

    // 추천 Assignees ID 반환
    public List<Long> recommendAssignees(Long projectId, String title, String description) {
        if (projectId == null || title == null || description == null) return List.of();

        // 새로운 Issue 토큰화 (Set 중복 제거)
        Set<String> newTokens = tokenize(title + " " + description);
        if (newTokens.isEmpty()) return List.of();

        // 점수 저장 Map
        Map<Long, Double> scores = new HashMap<>();
        issueRepository.findByProjectId(projectId).stream()
                .filter(i -> i.getStatus() == Status.RESOLVED || i.getStatus() == Status.CLOSED)
                .filter(i -> i.getFixerId() != null)
                .forEach(i -> {
                    // 유사도 검사
                    double sim = jaccardSimilarity(newTokens, tokenize(i.getTitle() + " " + i.getDescription()));
                    if (sim > 0) { // 공통 단어 하나도 없으면 제외
                        scores.merge(i.getFixerId(), sim, Double::sum);
                    }
                });

        return scores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed()) //점수 높은 순으로 정렬
                .limit(3) // 상위 3명
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    Set<String> tokenize(String text) {
        if (text == null) return Collections.emptySet();
        return Arrays.stream(text.toLowerCase().split("[^a-z0-9가-힣]+")) //문장을 단어 집합으로 변환
                .filter(w -> w.length() > 1)
                .collect(Collectors.toSet());
    }

    double jaccardSimilarity(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty()) return 0.0;
        Set<String> intersection = new HashSet<>(a); // 교집합
        intersection.retainAll(b);
        Set<String> union = new HashSet<>(a); // 합집합
        union.addAll(b);
        return (double) intersection.size() / union.size(); // 교집합/합집합
    }
}
