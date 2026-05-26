package com.issuetracker.domain.recommend.service;

import com.issuetracker.global.common.Response;
import java.util.List;

public interface IRecommendService {
    Response<List<Long>> recommendAssignees(Long projectId, String title, String description);
}
