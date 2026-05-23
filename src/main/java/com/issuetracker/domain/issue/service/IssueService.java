package com.issuetracker.domain.issue.service;

import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.domain.issue.enums.Priority;
import com.issuetracker.domain.issue.enums.Status;
import com.issuetracker.domain.issue.repository.IssueRepository;
import com.issuetracker.domain.project.entity.ProjectMember;
import com.issuetracker.domain.project.repository.ProjectMemberRepository;
import com.issuetracker.global.common.Response;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class IssueService {
    private final IssueRepository issueRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final IssueValidator issueValidator;

    public Response<Issue> createIssue(Long projectId, String title, String description, Long reporterId){

        // 요청에 누락은 없는지
        String missingParams = issueValidator.checkNonNull(projectId, title, description, reporterId);
        if(missingParams != null){
            return Response.fail(missingParams);
        }

        // title 필수값 검증
        String blankTitle = issueValidator.checkNonBlank(title, "Issue title");
        if(blankTitle != null){
            return Response.fail(blankTitle);
        }

        // description 필수값 검증
        String blankDescription = issueValidator.checkNonBlank(description, "Issue description");
        if(blankDescription != null){
            return Response.fail(blankDescription);
        }

        // Issue가 속할 project가 존재하는지 확인
        String missingProject = issueValidator.checkProjectExists(projectId);
        if(missingProject != null){
            return Response.fail(missingProject);
        }

        // Issue 생성을 요청한 reporterId가 project의 member인지 확인
        String notMember = issueValidator.checkProjectMember(projectId, reporterId);
        if(notMember != null){
            return Response.fail(notMember);
        }

        Issue issue = new Issue(projectId, title, description, reporterId);
        if(!issueRepository.save(issue)){
            return Response.fail("Failed to save the issue.");
        }
        return Response.success("Issue created.", issue);
    }

    public Response<List<Issue>> getAllIssues(){
        return Response.success("Issues retrieved.", issueRepository.findAll());
    }

    public Response<List<Issue>> getIssuesByProjectId(Long projectId){
        if(projectId == null) return Response.success("Issues retrieved.", List.of());
        return Response.success("Issues retrieved.", issueRepository.findByProjectId(projectId));
    }

    public Response<List<Issue>> getIssuesByAssigneeId(Long assigneeId){
        if(assigneeId == null) return Response.success("Issues retrieved.", List.of());
        return Response.success("Issues retrieved.", issueRepository.findByAssigneeId(assigneeId));
    }

    public Response<List<Issue>> getIssuesByReporterId(Long reporterId){
        if(reporterId == null) return Response.success("Issues retrieved.", List.of());
        return Response.success("Issues retrieved.", issueRepository.findByReporterId(reporterId));
    }

    public Response<List<Issue>> getIssuesByStatus(Status status){
        if(status == null) return Response.success("Issues retrieved.", List.of());
        return Response.success("Issues retrieved.", issueRepository.findByStatus(status));
    }

    public Response<List<Issue>> getIssuesByPriority(Priority priority){
        if(priority == null) return Response.success("Issues retrieved.", List.of());
        return Response.success("Issues retrieved.", issueRepository.findByPriority(priority));
    }

    public Response<Issue> getIssueById(Long issueId){
        String missingParams = issueValidator.checkNonNull(issueId);
        if(missingParams != null){
            return Response.fail(missingParams);
        }

        Issue issue = issueRepository.findByIssueId(issueId);
        if(issue == null){
            return Response.fail("Issue not found.");
        }
        return Response.success("Issue retrieved.", issue);
    }

    public Response<Issue> assignIssue(Long issueId, Long assigneeId, Long requesterId){
        String missingParams = issueValidator.checkNonNull(issueId, assigneeId, requesterId);
        if(missingParams != null){
            return Response.fail(missingParams);
        }

        Issue issue = issueRepository.findByIssueId(issueId);
        if(issue == null){
            return Response.fail("Issue not found.");
        }
        if(issue.getStatus() != Status.NEW){
            return Response.fail("Issue is not in NEW status.");
        }

        ProjectMember requester = projectMemberRepository.findByProjectIdAndAccountId(issue.getProjectId(), requesterId);
        if(requester == null || requester.getRole() != Role.PL){
            return Response.fail("Only a PL can assign issues.");
        }

        ProjectMember assignee = projectMemberRepository.findByProjectIdAndAccountId(issue.getProjectId(), assigneeId);
        if(assignee == null || assignee.getRole() != Role.DEV){
            return Response.fail("Assignee must be a DEV of the project.");
        }

        issue.assignTo(assigneeId);
        if(!issueRepository.update(issue)){
            return Response.fail("Failed to update the issue.");
        }
        return Response.success("Issue assigned.", issue);
    }

    public Response<Issue> fixIssue(Long issueId, Long requesterId){
        String missingParams = issueValidator.checkNonNull(issueId, requesterId);
        if(missingParams != null){
            return Response.fail(missingParams);
        }

        Issue issue = issueRepository.findByIssueId(issueId);
        if(issue == null){
            return Response.fail("Issue not found.");
        }
        if(issue.getStatus() != Status.ASSIGNED){
            return Response.fail("Issue is not in ASSIGNED status.");
        }

        if(!requesterId.equals(issue.getAssigneeId())){
            return Response.fail("Only the assignee can fix the issue.");
        }

        issue.markAsFixed(requesterId);
        if(!issueRepository.update(issue)){
            return Response.fail("Failed to update the issue.");
        }
        return Response.success("Issue fixed.", issue);
    }

    public Response<Issue> resolveIssue(Long issueId, Long requesterId){
        String missingParams = issueValidator.checkNonNull(issueId, requesterId);
        if(missingParams != null){
            return Response.fail(missingParams);
        }

        Issue issue = issueRepository.findByIssueId(issueId);
        if(issue == null){
            return Response.fail("Issue not found.");
        }
        if(issue.getStatus() != Status.FIXED){
            return Response.fail("Issue is not in FIXED status.");
        }

        if(!requesterId.equals(issue.getReporterId())){
            return Response.fail("Only the reporter can resolve the issue.");
        }

        issue.markAsResolved();
        if(!issueRepository.update(issue)){
            return Response.fail("Failed to update the issue.");
        }
        return Response.success("Issue resolved.", issue);
    }

    public Response<Issue> closeIssue(Long issueId, Long requesterId){
        String missingParams = issueValidator.checkNonNull(issueId, requesterId);
        if(missingParams != null){
            return Response.fail(missingParams);
        }

        Issue issue = issueRepository.findByIssueId(issueId);
        if(issue == null){
            return Response.fail("Issue not found.");
        }
        if(issue.getStatus() != Status.RESOLVED){
            return Response.fail("Issue is not in RESOLVED status.");
        }

        ProjectMember requester = projectMemberRepository.findByProjectIdAndAccountId(issue.getProjectId(), requesterId);
        if(requester == null || requester.getRole() != Role.PL){
            return Response.fail("Only a PL can close issues.");
        }

        issue.markAsClosed();
        if(!issueRepository.update(issue)){
            return Response.fail("Failed to update the issue.");
        }
        return Response.success("Issue closed.", issue);
    }
}