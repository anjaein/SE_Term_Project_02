package com.issuetracker.domain.account.dto;

public record LoginRequest(
        String username,
        String password
) {
}
