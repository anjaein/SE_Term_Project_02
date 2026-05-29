package com.issuetracker.ui.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Comment {
    private Long commentId;
    private Long issueId;
    private Long authorId;
    private String content;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public Comment(Long commentId, Long issueId, Long authorId, String content){
        this.commentId=commentId;
        this.issueId=issueId;
        this.authorId=authorId;
        this.content=content;
        this.createdDate=LocalDateTime.now();
        this.updatedDate=LocalDateTime.now();
    }

    public void updateContent(String c){
        this.content=c==null ? "" : c.trim();
        this.updatedDate=LocalDateTime.now();
    }
}
