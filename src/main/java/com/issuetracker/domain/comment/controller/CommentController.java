package com.issuetracker.domain.comment.controller;

import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.comment.entity.Comment;
import com.issuetracker.domain.comment.service.CommentService;
import com.issuetracker.global.common.Response;
import com.issuetracker.global.common.SessionManager;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final SessionManager sessionManager;

    // 댓글 작성
    public Response<Comment> createComment(Long issueId, String content) {
        Account currentUser = sessionManager.getLoggedInAccount();
        if (currentUser == null) {
            return Response.fail("You are not logged in.");
        }
        return commentService.createComment(issueId, currentUser.getAccountId(), content);
    }

    // 댓글 조회 (이슈별)
    public Response<List<Comment>> listComments(Long issueId) {
        if (sessionManager.getLoggedInAccount() == null) {
            return Response.fail("You are not logged in.");
        }
        return commentService.getCommentsByIssueId(issueId);
    }

    // 댓글 수정
    public Response<Comment> updateComment(Long commentId, String newContent) {
        Account currentUser = sessionManager.getLoggedInAccount();
        if (currentUser == null) {
            return Response.fail("You are not logged in.");
        }
        return commentService.updateComment(commentId, currentUser.getAccountId(), newContent);
    }

    // 댓글 삭제
    public Response<Comment> deleteComment(Long commentId) {
        Account currentUser = sessionManager.getLoggedInAccount();
        if (currentUser == null) {
            return Response.fail("You are not logged in.");
        }
        boolean isAdmin = currentUser.isAdmin();
        return commentService.deleteComment(commentId, currentUser.getAccountId(), isAdmin);
    }
}