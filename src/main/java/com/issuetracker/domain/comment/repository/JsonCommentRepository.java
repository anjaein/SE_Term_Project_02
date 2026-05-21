package com.issuetracker.domain.comment.repository;

import com.google.gson.reflect.TypeToken;
import com.issuetracker.domain.comment.entity.Comment;
import com.issuetracker.global.common.JsonFileManager;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

public class JsonCommentRepository implements CommentRepository {
    private static final String FILE_PATH = "data/comments.json";
    private static final Type TYPE = new TypeToken<List<Comment>>(){}.getType();

    @Override
    public List<Comment> findAll() {
        return JsonFileManager.readList(FILE_PATH, TYPE);
    }

    @Override
    public List<Comment> findByIssueId(Long issueId) {
        return findAll().stream()
                .filter(c -> c.getIssueId().equals(issueId))
                .collect(Collectors.toList());
    }

    @Override
    public Comment findByCommentId(Long commentId) {
        return findAll().stream()
                .filter(c -> c.getCommentId().equals(commentId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean save(Comment comment) {
        try {
            List<Comment> comments = findAll();
            Long newId = comments.stream()
                    .mapToLong(Comment::getCommentId)
                    .max()
                    .orElse(0L) + 1L;
            comment.setCommentId(newId);
            comments.add(comment);
            JsonFileManager.writeList(FILE_PATH, comments);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean update(Comment comment) {
        try {
            List<Comment> comments = findAll();
            for (int i = 0; i < comments.size(); i++) {
                if (comments.get(i).getCommentId().equals(comment.getCommentId())) {
                    comments.set(i, comment);
                    JsonFileManager.writeList(FILE_PATH, comments);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
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