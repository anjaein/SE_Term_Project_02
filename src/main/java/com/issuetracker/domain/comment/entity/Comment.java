package com.issuetracker.domain.comment.entity;



import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
public class Comment {

    @Setter
    private Long commentId;
    private Long issueId;          // 어느 이슈에 달린 댓글인지
    private Long authorId;         // 누가 작성했는지
    private String content;        // 댓글 내용
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate; //코멘트 수정 날짜

    // 생성자 (댓글 생성)
    public Comment(Long issueId, Long authorId, String content) {
        this.issueId = issueId;
        this.authorId = authorId;
        this.content = content;
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }


    // content 수정 시 updatedDate 자동 업데이트
    public void updateContent(String newContent) {
        this.content = newContent.trim();  // 저장 시 공백 제거 (선택)
        this.updatedDate = LocalDateTime.now();
    }
}
