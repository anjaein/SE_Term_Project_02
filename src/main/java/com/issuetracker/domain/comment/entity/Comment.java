package com.issuetracker.domain.comment.entity;



import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

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
    // 다른 레이어에서 ID를 할당해야 할 경우 사용, 사용 이유를 명확히 하기 위해 setter 대신 별도의 메서드로 구현
    public void assignId(Long id) {
        this.commentId = id;
    }

    // content 수정 시 updatedDate 자동 업데이트
    public void updateContent(String newContent) {
        this.content = newContent.trim();  // 저장 시 공백 제거 (선택)
        this.updatedDate = LocalDateTime.now();
    }
}
