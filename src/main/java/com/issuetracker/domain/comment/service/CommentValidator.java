package com.issuetracker.domain.comment.service;

import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.domain.issue.repository.IssueRepository;
import com.issuetracker.domain.project.repository.ProjectMemberRepository;
import com.issuetracker.global.common.Validator;
import lombok.RequiredArgsConstructor;

// Comment 도메인용 Validator
@RequiredArgsConstructor
public class CommentValidator implements Validator {
    private final IssueRepository issueRepository;
    private final ProjectMemberRepository projectMemberRepository;

    // 댓글이 달릴 이슈가 존재하는지 검증
    public String checkIssueExists(Long issueId) {
        if (issueRepository.findByIssueId(issueId) == null) {
            return "Issue does not exist.";
        }
        return null;
    }

    // 댓글 작성자가 이슈가 속한 프로젝트의 멤버인지 검증.
    // 이슈 존재가 보장된 뒤(checkIssueExists 통과 후) 호출해야 한다.
    public String checkProjectMember(Long issueId, Long authorId) {
        Issue issue = issueRepository.findByIssueId(issueId);
        if (projectMemberRepository.findByProjectIdAndAccountId(issue.getProjectId(), authorId) == null) {
            return "Author is not a member of the project.";
        }
        return null;
    }

    // 댓글 수정 권한 검증: 본인 댓글만 수정 가능
    public String checkUpdatePermission(Long commentAuthorId, Long userId) {
        if (!commentAuthorId.equals(userId)) {
            return "You can only update your own comments.";
        }
        return null;
    }

    // 댓글 삭제 권한 검증: 본인 댓글이거나 관리자만 삭제 가능
    public String checkDeletePermission(Long commentAuthorId, Long userId, boolean isAdmin) {
        if (!commentAuthorId.equals(userId) && !isAdmin) {
            return "You can only delete your own comments.";
        }
        return null;
    }
}