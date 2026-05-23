package com.issuetracker.domain.account.service;

import com.issuetracker.domain.account.repository.AccountRepository;
import com.issuetracker.global.common.Validator;
import lombok.RequiredArgsConstructor;

// Account 도메인용 Validator
@RequiredArgsConstructor
public class AccountValidator implements Validator {
    private final AccountRepository accountRepository;

    // username이 이미 존재하는지 검증
    public String checkUsernameDuplicate(String username) {
        if (accountRepository.findByUsername(username) != null) {
            return "Username already exists: " + username;
        }
        return null;
    }
}