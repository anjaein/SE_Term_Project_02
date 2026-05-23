package com.issuetracker.domain.comment.service;

import com.issuetracker.domain.comment.entity.Comment;
import com.issuetracker.domain.comment.repository.CommentRepository;
import com.issuetracker.global.common.Response;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentValidator commentValidator;

    public Response<Comment> createComment(Long issueId, Long authorId, String content) {
        String missingParams = commentValidator.checkNonNull(issueId, authorId, content);
        if (missingParams != null) {
            return Response.fail(missingParams);
        }

        String blankContent = commentValidator.checkNonBlank(content, "Comment content");
        if (blankContent != null) {
            return Response.fail(blankContent);
        }

        String missingIssue = commentValidator.checkIssueExists(issueId);
        if (missingIssue != null) {
            return Response.fail(missingIssue);
        }

        String notMember = commentValidator.checkProjectMember(issueId, authorId);
        if (notMember != null) {
            return Response.fail(notMember);
        }

        Comment comment = new Comment(issueId, authorId, content);
        if (!commentRepository.save(comment)) {
            return Response.fail("Failed to save the comment.");
        }
        return Response.success("Comment created.", comment);
    }

    public Response<List<Comment>> getCommentsByIssueId(Long issueId) {
        return Response.success("Comments retrieved.", commentRepository.findByIssueId(issueId));
    }

    public Response<Comment> getCommentById(Long commentId) {
        Comment comment = commentRepository.findByCommentId(commentId);
        if (comment == null) {
            return Response.fail("Comment not found.");
        }
        return Response.success("Comment retrieved.", comment);
    }

    public Response<Comment> updateComment(Long commentId, Long userId, String newContent) {
        String missingParams = commentValidator.checkNonNull(commentId, userId, newContent);
        if (missingParams != null) {
            return Response.fail(missingParams);
        }

        String blankContent = commentValidator.checkNonBlank(newContent, "Comment content");
        if (blankContent != null) {
            return Response.fail(blankContent);
        }

        Comment comment = commentRepository.findByCommentId(commentId);

        if (comment == null) {
            return Response.fail("Comment not found.");
        }
        if (!comment.getAuthorId().equals(userId)) {
            return Response.fail("You can only update your own comments.");
        }

        comment.updateContent(newContent);
        if (!commentRepository.update(comment)) {
            return Response.fail("Failed to update the comment.");
        }
        return Response.success("Comment updated.", comment);
    }

    public Response<Comment> deleteComment(Long commentId, Long userId, boolean isAdmin) {
        String missingParams = commentValidator.checkNonNull(commentId, userId);
        if (missingParams != null) {
            return Response.fail(missingParams);
        }

        Comment comment = commentRepository.findByCommentId(commentId);

        if (comment == null) {
            return Response.fail("Comment not found.");
        }

        if (!comment.getAuthorId().equals(userId) && !isAdmin) {
            return Response.fail("You can only delete your own comments.");
        }

        if (!commentRepository.delete(commentId)) {
            return Response.fail("Failed to delete the comment.");
        }
        return Response.success("Comment deleted.", comment);
    }
}