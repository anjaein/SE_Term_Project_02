package com.issuetracker.domain.comment.repository;

import com.issuetracker.domain.comment.entity.Comment;

import java.util.List;

public interface CommentRepository {
    List<Comment> findAll();
    List<Comment> findByIssueId(Long issueId);
    Comment findByCommentId(Long commentId);
    boolean save(Comment comment);
    boolean update(Comment comment);
    boolean delete(Long commentId);
}