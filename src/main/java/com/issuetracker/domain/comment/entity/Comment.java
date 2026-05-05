package com.issuetracker.domain.comment.entity;

import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.issue.entity.Issue;

import java.time.LocalDateTime;

public class Comment {

    private int commentId;
    private Issue issue;
    private Account author;
    private String content;
    private LocalDateTime createdDate;

    public Comment(Issue issue, Account author, String content) {
        this.issue = issue;
        this.author = author;
        this.content = content;
        this.createdDate = LocalDateTime.now();
    }
}
