package com.issuetracker.domain.comment.service;

import com.issuetracker.domain.account.repository.AccountRepository;
import com.issuetracker.domain.comment.entity.Comment;
import com.issuetracker.domain.comment.repository.CommentRepository;
import com.issuetracker.domain.issue.repository.IssueRepository;
import lombok.NoArgsConstructor;

import java.util.List;


public class CommentService {
    private final CommentRepository commentRepository;
    private final AccountRepository accountRepository ;
    private final IssueRepository issueRepository;
    private static CommentService instance;

    public CommentService(CommentRepository commentRepository, AccountRepository accountRepository, IssueRepository issueRepository) {
        this.commentRepository = commentRepository;
        this.accountRepository = accountRepository;
        this.issueRepository = issueRepository;
    }

    public boolean createComment(Long issueId, Long authorId, String content) {
        if (issueId == null || authorId == null || content == null || content.trim().isEmpty()) {
            return false;
        }

        if (accountRepository.findById(authorId) == null) {
            return false;
        }

        boolean issueExists = issueRepository.findAll().stream()
                .anyMatch(issue -> issueId.equals(issue.getIssueId()));
        if (!issueExists) {
            return false;
        }

        Comment comment = new Comment(issueId, authorId, content);
        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsByIssueId(Long issueId) {
        return commentRepository.findByIssueId(issueId);
    }

    public Comment getCommentById(Long commentId) {
        return commentRepository.findByCommentId(commentId);
    }

    public boolean updateComment(Long commentId, Long userId, String newContent) {
        Comment comment = commentRepository.findByCommentId(commentId);

        if (comment == null || !comment.getAuthorId().equals(userId)) {
            return false;
        }

        comment.updateContent(newContent);
        return commentRepository.update(comment);
    }

    public boolean deleteComment(Long commentId, Long userId, boolean isAdmin) {
        Comment comment = commentRepository.findByCommentId(commentId);

        if (comment == null) {
            return false;
        }

        if (!comment.getAuthorId().equals(userId) && !isAdmin) {
            return false;
        }

        return commentRepository.delete(commentId);
    }
}
