package com.issuetracker.domain.comment.repository;


import com.google.gson.reflect.TypeToken;
import com.issuetracker.domain.comment.entity.Comment;
import com.issuetracker.global.common.JsonFileManager;
import lombok.NoArgsConstructor;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
public class CommentRepository {
    private static final String FILE_PATH = "data/comments.json";
    private static final Type TYPE = new TypeToken<List<Comment>>(){}.getType();
    private static CommentRepository instance;

    // Singleton 인스턴스 반환
    public static synchronized CommentRepository getInstance() {
        if (instance == null) {
            instance = new CommentRepository();
        }
        return instance;
    }

    // 모든 댓글 조회
    public List<Comment> findAll() {
        return JsonFileManager.readList(FILE_PATH, TYPE);
    }

    // 특정 이슈의 모든 댓글 조회
    public List<Comment> findByIssueId(Long issueId) {
        return findAll().stream()
                .filter(c -> c.getIssueId().equals(issueId))
                .collect(Collectors.toList());
    }

    // 댓글 ID로 조회
    public Comment findByCommentId(Long commentId) {
        return findAll().stream()
                .filter(c -> c.getCommentId().equals(commentId))
                .findFirst()
                .orElse(null);
    }

    // 댓글 저장 (새로 생성)
    public boolean save(Comment comment) {
        try {
            List<Comment> comments = findAll();
            Long newId = comments.stream()
                    .mapToLong(Comment::getCommentId)
                    .max()
                    .orElse(0L) + 1L;
            comment.assignId(newId);
            comments.add(comment);
            JsonFileManager.writeList(FILE_PATH, comments);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 댓글 수정
    public boolean update(Comment comment) {
        try {
            List<Comment> comments = findAll();
            boolean updated = false;
            for (int i = 0; i < comments.size(); i++) {
                if (comments.get(i).getCommentId().equals(comment.getCommentId())) {
                    comments.set(i, comment);
                    updated = true;
                    break;
                }
            }
            if (updated) {
                JsonFileManager.writeList(FILE_PATH, comments);
            }
            return updated;
        } catch (Exception e) {
            return false;
        }
    }

    // 댓글 삭제
    public boolean delete(Long commentId) {
        try {
            List<Comment> comments = findAll();
            boolean removed = comments.removeIf(c -> c.getCommentId().equals(commentId));
            if (removed) {
                JsonFileManager.writeList(FILE_PATH, comments);
            }
            return removed;
        } catch (Exception e) {
            return false;
        }
    }
}
