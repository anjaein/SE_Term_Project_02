package com.issuetracker.ui.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMember {
    private Long projectId;
    private Long accountId;
    private Role role;
}
