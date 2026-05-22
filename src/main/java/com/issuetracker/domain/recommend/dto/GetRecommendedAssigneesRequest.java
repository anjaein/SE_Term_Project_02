package com.issuetracker.domain.recommend.dto;

public record GetRecommendedAssigneesRequest(
        Long projectId,
        String title,
        String description
) {
}
