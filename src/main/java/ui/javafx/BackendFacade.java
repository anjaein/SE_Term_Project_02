package ui.javafx;

import com.issuetracker.Main;
import com.issuetracker.domain.account.controller.AccountController;
import com.issuetracker.domain.comment.controller.CommentController;
import com.issuetracker.domain.issue.controller.IssueController;
import com.issuetracker.domain.issue.controller.IssueStatisticsController;
import com.issuetracker.domain.project.controller.ProjectController;
import com.issuetracker.domain.recommend.controller.RecommendController;
import com.issuetracker.global.common.Backend;
import com.issuetracker.global.common.Response;
import ui.javafx.domain.*;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class BackendFacade {

    private static BackendFacade instance;

    public static BackendFacade get(){
        if (instance==null){
            instance=new BackendFacade();
        }

        return instance;
    }

    public final ObservableList<Account> accounts=FXCollections.observableArrayList();
    public final ObservableList<Project> projects=FXCollections.observableArrayList();
    public final ObservableList<ProjectMember> members=FXCollections.observableArrayList();
    public final ObservableList<Issue> issues=FXCollections.observableArrayList();
    public final ObservableList<Comment> comments=FXCollections.observableArrayList();

    public final SimpleObjectProperty<Account> currentUser=new SimpleObjectProperty<>(null);
    public final SimpleLongProperty currentProjectId=new SimpleLongProperty(1L);

    private final AccountController accountController;
    private final ProjectController projectController;
    private final IssueController issueController;
    private final CommentController commentController;
    private final IssueStatisticsController issueStatisticsController;
    private final RecommendController recommendController;

    private BackendFacade(){
        Backend backend=Main.createBackend();

        accountController=backend.accountController;
        projectController=backend.projectController;
        issueController=backend.issueController;
        commentController=backend.commentController;
        issueStatisticsController=backend.issueStatisticsController;
        recommendController=backend.recommendController;

        loadPublicData();
    }

    private void loadPublicData(){
        accounts.setAll(dataOrThrow(accountController.listAccounts()).stream().map(account -> toUiAccount(account)).toList());
        projects.setAll(dataOrThrow(projectController.getAllProjects()).stream().map(project -> toUiProject(project)).toList());
    }

    private void loadProjectData(){
        members.clear();
        issues.clear();
        comments.clear();

        for (Project project : projects) {
            Response<List<com.issuetracker.domain.project.entity.ProjectMember>> membersResult=
                projectController.listProjectMembers(project.getProjectId());
            if (!membersResult.isSuccess()) {
                throw new IllegalStateException(membersResult.getMessage());
            }
            members.addAll(membersResult.getData().stream().map(member -> toUiProjectMember(member)).toList());

            Response<List<com.issuetracker.domain.issue.entity.Issue>> issuesResult=
                issueController.listIssuesByProject(project.getProjectId());
            if (!issuesResult.isSuccess()) {
                throw new IllegalStateException(issuesResult.getMessage());
            }
            for (com.issuetracker.domain.issue.entity.Issue issue : issuesResult.getData()) {
                issues.add(toUiIssue(issue));

                Response<List<com.issuetracker.domain.comment.entity.Comment>> commentsResult=
                    commentController.listComments(issue.getIssueId());
                if (!commentsResult.isSuccess()) {
                    throw new IllegalStateException(commentsResult.getMessage());
                }
                comments.addAll(commentsResult.getData().stream().map(comment -> toUiComment(comment)).toList());
            }
        }
    }

    private Account toUiAccount(com.issuetracker.domain.account.entity.Account account){
        return new Account(
            account.getAccountId(),
            account.getUsername(),
            account.getPassword(),
            account.isAdmin()
        );
    }

    public Account login(String username, String password){
        Response<com.issuetracker.domain.account.entity.Account> result=
            accountController.login(username, password);
        if (!result.isSuccess()) {
            throw new IllegalStateException(result.getMessage());
        }

        Account account=toUiAccount(result.getData());
        currentUser.set(account);
        loadProjectData();
        return account;
    }

    public void logout(){
        Response<Void> result=accountController.logout();
        if (!result.isSuccess()) {
            throw new IllegalStateException(result.getMessage());
        }
        currentUser.set(null);
        members.clear();
        issues.clear();
        comments.clear();
    }

    private com.issuetracker.domain.project.enums.Role toDomainRole(Role role){
        return com.issuetracker.domain.project.enums.Role.valueOf(role.name());
    }

    private Role toUiRole(com.issuetracker.domain.project.enums.Role role){
        return Role.valueOf(role.name());
    }

    private Priority toUiPriority(com.issuetracker.domain.issue.enums.Priority priority){
        return Priority.valueOf(priority.name());
    }

    private com.issuetracker.domain.issue.enums.Priority toDomainPriority(Priority priority){
        return com.issuetracker.domain.issue.enums.Priority.valueOf(priority.name());
    }

    private Status toUiStatus(com.issuetracker.domain.issue.enums.Status status){
        return Status.valueOf(status.name());
    }

    private Project toUiProject(com.issuetracker.domain.project.entity.Project project){
        return new Project(
            project.getProjectId(),
            project.getName(),
            project.getCreatedBy(),
            project.getCreatedDate()
        );
    }

    private Issue toUiIssue(com.issuetracker.domain.issue.entity.Issue issue){
        Issue uiIssue=new Issue(
            issue.getIssueId(),
            issue.getProjectId(),
            issue.getTitle(),
            issue.getDescription(),
            issue.getReporterId()
        );
        uiIssue.setPriority(toUiPriority(issue.getPriority()));
        uiIssue.setStatus(toUiStatus(issue.getStatus()));
        uiIssue.setAssigneeId(issue.getAssigneeId());
        uiIssue.setFixerId(issue.getFixerId());
        uiIssue.setReportedDate(issue.getReportedDate());
        uiIssue.setFixedDate(issue.getFixedDate());
        uiIssue.setResolvedDate(issue.getResolvedDate());
        uiIssue.setClosedDate(issue.getClosedDate());
        return uiIssue;
    }

    private Comment toUiComment(com.issuetracker.domain.comment.entity.Comment comment){
        Comment uiComment=new Comment(
            comment.getCommentId(),
            comment.getIssueId(),
            comment.getAuthorId(),
            comment.getContent()
        );
        uiComment.setCreatedDate(comment.getCreatedDate());
        return uiComment;
    }

    private ProjectMember toUiProjectMember(com.issuetracker.domain.project.entity.ProjectMember member){
        return new ProjectMember(member.getProjectId(), member.getAccountId(), toUiRole(member.getRole()));
    }

    private void replaceIssue(Issue issue){
        int idx=issues.indexOf(issueById(issue.getIssueId()));
        if (idx>=0) {
            issues.set(idx, issue);
        } else {
            issues.add(0, issue);
        }
    }

    public Account accountById(Long id){
        if (id==null) return null;
        return accounts.stream().filter(a -> Objects.equals(a.getAccountId(), id)).findFirst().orElse(null);
    }

    public Project projectById(Long id){
        return projects.stream().filter(p -> Objects.equals(p.getProjectId(), id)).findFirst().orElse(null);
    }

    public Issue issueById(Long id){
        return issues.stream().filter(i -> Objects.equals(i.getIssueId(), id)).findFirst().orElse(null);
    }

    public List<Issue> issuesOf(long projectId){
        return issues.stream().filter(i -> Objects.equals(i.getProjectId(), projectId)).toList();
    }

    public List<Comment> commentsOf(long issueId){
        return comments.stream().filter(c -> Objects.equals(c.getIssueId(), issueId)).sorted(
            Comparator.comparing(comment -> comment.getCreatedDate())).toList();
    }

    public List<ProjectMember> membersOf(long projectId){
        return members.stream().filter(m -> Objects.equals(m.getProjectId(), projectId)).toList();
    }

    public boolean isCurrentUserAdmin(){
        Account current=currentUser.get();
        return current!=null && current.isAdmin();
    }

    public Role roleOf(Account account, long projectId){
        return account==null ? null : roleOf(account.getAccountId(), projectId);
    }

    public Role roleOf(Long accountId, long projectId){
        if (accountId==null) return null;
        return membersOf(projectId).stream()
            .filter(m -> Objects.equals(m.getAccountId(), accountId))
            .map(ProjectMember::getRole)
            .findFirst()
            .orElse(null);
    }

    public Role currentProjectRole(){
        Account current=currentUser.get();
        if (current==null) return null;
        Role role=roleOf(current, currentProjectId.get());
        if (role!=null) return role;
        return current.isAdmin() ? Role.ADMIN : null;
    }

    private <T> T dataOrThrow(Response<T> response){
        if (!response.isSuccess()){
            throw new IllegalStateException(response.getMessage());
        }
        return response.getData();
    }

    public Issue createIssue(long projectId, String title, String desc, Priority priority){
        if (priority==null) {
            throw new IllegalStateException("Priority is required.");
        }

        Response<com.issuetracker.domain.issue.entity.Issue> result=
            issueController.createIssue(projectId, title, desc, toDomainPriority(priority));
        if (!result.isSuccess()) {
            throw new IllegalStateException(result.getMessage());
        }

        Issue issue=toUiIssue(result.getData());
        issues.add(0, issue);
        return issue;
    }

    public void changeStatus(long issueId, Status target){
        Response<com.issuetracker.domain.issue.entity.Issue> result;
        switch (target) {
            case FIXED:
                result=issueController.fixIssue(issueId);
                break;
            case RESOLVED:
                result=issueController.resolveIssue(issueId);
                break;
            case CLOSED:
                result=issueController.closeIssue(issueId);
                break;
            case REOPENED:
                result=issueController.reopenIssue(issueId);
                break;
            default:
                result=Response.fail("Use assignment controls for NEW/ASSIGNED transitions.");
        }
        if (!result.isSuccess()) {
            throw new IllegalStateException(result.getMessage());
        }
        replaceIssue(toUiIssue(result.getData()));
    }

    public void assignIssue(long issueId, long assigneeId){
        Response<com.issuetracker.domain.issue.entity.Issue> result=
            issueController.assignIssue(issueId, assigneeId);
        if (!result.isSuccess()) {
            throw new IllegalStateException(result.getMessage());
        }
        replaceIssue(toUiIssue(result.getData()));
    }

    public Comment addComment(long issueId, String content){
        Response<com.issuetracker.domain.comment.entity.Comment> result=
            commentController.createComment(issueId, content);
        if (!result.isSuccess()) {
            throw new IllegalStateException(result.getMessage());
        }
        Comment comment=toUiComment(result.getData());
        comments.add(comment);
        return comment;
    }

    public void updateComment(long commentId, String newContent){
        Response<com.issuetracker.domain.comment.entity.Comment> result=
            commentController.updateComment(commentId, newContent);
        if (!result.isSuccess()) {
            throw new IllegalStateException(result.getMessage());
        }
        Comment updated=toUiComment(result.getData());
        int idx=-1;
        for (int i=0; i<comments.size(); i++) {
            if (comments.get(i).getCommentId().equals(updated.getCommentId())) {
                idx=i;
                break;
            }
        }
        if (idx>=0) comments.set(idx, updated);
    }

    public void deleteComment(long commentId){
        Response<com.issuetracker.domain.comment.entity.Comment> result=
            commentController.deleteComment(commentId);
        if (!result.isSuccess()) {
            throw new IllegalStateException(result.getMessage());
        }
        comments.removeIf(c -> Objects.equals(c.getCommentId(), commentId));
    }

    public Project createProject(String name){
        Response<com.issuetracker.domain.project.entity.Project> result=
            projectController.createProject(name);
        if (!result.isSuccess()) {
            throw new IllegalStateException(result.getMessage());
        }

        Project project=toUiProject(result.getData());
        projects.add(project);
        Response<List<com.issuetracker.domain.project.entity.ProjectMember>> membersResult=
            projectController.listProjectMembers(project.getProjectId());
        if (!membersResult.isSuccess()) {
            throw new IllegalStateException(membersResult.getMessage());
        }
        members.addAll(membersResult.getData().stream().map(member -> toUiProjectMember(member)).toList());
        return project;
    }

    public void addMember(long projectId, long accountId, Role role){
        Account account=accountById(accountId);
        if (account==null) {
            throw new IllegalStateException("Account not found.");
        }

        Response<com.issuetracker.domain.project.entity.ProjectMember> result=
            projectController.addProjectMember(projectId, account.getUsername(), toDomainRole(role));
        if (!result.isSuccess()) {
            throw new IllegalStateException(result.getMessage());
        }

        members.removeIf(m -> Objects.equals(m.getProjectId(), projectId) && Objects.equals(m.getAccountId(), accountId));
        members.add(toUiProjectMember(result.getData()));
    }

    public Account createAccount(String username, String password){
        Response<com.issuetracker.domain.account.entity.Account> result=
            accountController.createAccount(username, password, false);
        if (!result.isSuccess()) {
            throw new IllegalStateException(result.getMessage());
        }
        Account account=toUiAccount(result.getData());
        accounts.add(account);
        return account;
    }

    public record Recommendation(Account dev, double score, int historyCount){}

    public Map<LocalDate, Long> dailyReportedTrend(long projectId){
        return dataOrThrow(issueStatisticsController.getDailyReportedTrend(projectId));
    }

    public Map<YearMonth, Long> monthlyReportedTrend(long projectId, int months){
        return dataOrThrow(issueStatisticsController.getMonthlyReportedTrend(projectId, months));
    }

    public Map<LocalDate, Long> dailyResolvedTrend(long projectId){
        return dataOrThrow(issueStatisticsController.getDailyResolvedTrend(projectId));
    }

    public Map<YearMonth, Long> monthlyResolvedTrend(long projectId, int months){
        return dataOrThrow(issueStatisticsController.getMonthlyResolvedTrend(projectId, months));
    }

    public Map<LocalDate, Map<Priority, Long>> dailyPriorityDistribution(long projectId){
        Map<LocalDate, Map<com.issuetracker.domain.issue.enums.Priority, Long>> result=
            dataOrThrow(issueStatisticsController.getDailyPriorityDistribution(projectId));
        Map<LocalDate, Map<Priority, Long>> converted=new TreeMap<>();
        result.forEach((date, counts) -> {
            Map<Priority, Long> uiCounts=new EnumMap<>(Priority.class);
            counts.forEach((priority, count) -> uiCounts.put(toUiPriority(priority), count));
            converted.put(date, uiCounts);
        });
        return converted;
    }

    public Map<YearMonth, Map<Priority, Long>> monthlyPriorityDistribution(long projectId, int months){
        Map<YearMonth, Map<com.issuetracker.domain.issue.enums.Priority, Long>> result=
            dataOrThrow(issueStatisticsController.getMonthlyPriorityDistribution(projectId, months));
        Map<YearMonth, Map<Priority, Long>> converted=new TreeMap<>();
        result.forEach((month, counts) -> {
            Map<Priority, Long> uiCounts=new EnumMap<>(Priority.class);
            counts.forEach((priority, count) -> uiCounts.put(toUiPriority(priority), count));
            converted.put(month, uiCounts);
        });
        return converted;
    }

    public Map<YearMonth, Double> monthlyAverageClosedDays(long projectId, int months){
        return dataOrThrow(issueStatisticsController.getMonthlyAverageClosedDays(projectId, months));
    }

    public List<Recommendation> recommendAssignees(Issue target){
        return dataOrThrow(recommendController
            .getRecommendedAssignees(target.getProjectId(), target.getTitle(), target.getDescription()))
            .stream()
            .map(account -> toUiAccount(account))
            .map(account -> new Recommendation(account, 0, 0))
            .toList();
    }

}
