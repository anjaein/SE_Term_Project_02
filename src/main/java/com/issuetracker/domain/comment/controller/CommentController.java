package com.issuetracker.domain.comment.controller;

import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.comment.entity.Comment;
import com.issuetracker.domain.comment.service.CommentService;
import com.issuetracker.global.common.SessionManager;


import java.util.List;


public class CommentController {
    private final CommentService commentService;
    private final SessionManager sessionManager;

    public CommentController(CommentService commentService, SessionManager sessionManager) {
        this.commentService = commentService;
        this.sessionManager = sessionManager;
    }
    public CommentController() {
        this(new CommentService(), new SessionManager());
    }
    // 댓글 작성
    public void createComment(Long issueId, String content) {
        Account currentUser = sessionManager.getLoggedInAccount();

        if (currentUser == null) {
            notifyError("You are not logged in.");
            return;
        }

        if (commentService.createComment(issueId, currentUser.getAccountId(), content)) {
            notifySuccess("Your comment has been created.");
        } else {
            notifyError("Failed to create the comment.");
        }
    }

    // 댓글 조회 (이슈별)
    public void listComments(Long issueId) {
        List<Comment> comments = commentService.getCommentsByIssueId(issueId);
        System.out.println("[INFO] Comments on issue " + issueId + ": " + comments.size() + " comment(s)");
        for (Comment comment : comments) {
            System.out.println("  - [Comment " + comment.getCommentId() + "] Author(ID:" + comment.getAuthorId() + "): " + comment.getContent());
            System.out.println("    Created: " + comment.getCreatedDate() + ", Updated: " + comment.getUpdatedDate());
        }
    }

    // 댓글 수정
    public void updateComment(Long commentId, String newContent) {
        Account currentUser = sessionManager.getLoggedInAccount();

        if (currentUser == null) {
            notifyError("You are not logged in.");
            return;
        }

        if (commentService.updateComment(commentId, currentUser.getAccountId(), newContent)) {
            notifySuccess("Your comment has been updated.");
        } else {
            notifyError("Failed to update the comment. You can only update your own comments.");
        }
    }

    // 댓글 삭제
    public void deleteComment(Long commentId) {
        Account currentUser = sessionManager.getLoggedInAccount();

        if (currentUser == null) {
            notifyError("You are not logged in.");
            return;
        }

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (commentService.deleteComment(commentId, currentUser.getAccountId(), isAdmin)) {
            notifySuccess("Your comment has been deleted.");
        } else {
            notifyError("Failed to delete the comment. You can only delete your own comments.");
        }
    }

    private void notifySuccess(String message) {
        System.out.println("[SUCCESS] " + message);
    }

    private void notifyError(String message) {
        System.out.println("[ERROR] " + message);
    }
}
