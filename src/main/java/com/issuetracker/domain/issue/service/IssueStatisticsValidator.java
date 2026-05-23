package com.issuetracker.domain.issue.service;

import com.issuetracker.global.common.Validator;
import lombok.RequiredArgsConstructor;

// IssueStatistics 도메인용 Validator
@RequiredArgsConstructor
public class IssueStatisticsValidator implements Validator {

    // 통계 집계 기간(개월 수)이 1 이상인지 검증
    public String checkMonths(int months) {
        if (months < 1) {
            return "Months must be at least 1.";
        }
        return null;
    }
}
