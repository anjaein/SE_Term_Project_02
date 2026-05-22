package com.issuetracker.global.dto;

public record CommonResponse<T>(
        boolean success,
        String message,
        T data
) {
}
