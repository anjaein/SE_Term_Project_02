package com.issuetracker.domain.account.dto;

import com.issuetracker.domain.account.enums.Role;

public record CreateAccountRequest(
        String username,
        String password,
        Role role
) {
}
