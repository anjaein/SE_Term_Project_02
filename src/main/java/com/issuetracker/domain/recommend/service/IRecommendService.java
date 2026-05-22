package com.issuetracker.domain.recommend.service;

import java.util.List;

public interface IRecommendService {
    List<Long> recommendAssignees(Long projectId, String title, String description);
}
