package com.issuetracker.ui.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 백엔드 Account 엔티티와 동일 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    private Long accountId;
    private String username;
    private String password;
    private Role role;

    public Account(String username, String password, Role role){
        this.username=username;
        this.password=password;
        this.role=role;
    }
}
