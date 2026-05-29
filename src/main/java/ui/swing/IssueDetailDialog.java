package ui.swing;

import com.issuetracker.domain.account.controller.AccountController;
import com.issuetracker.domain.account.entity.Account;
import com.issuetracker.domain.account.enums.Role;
import com.issuetracker.domain.comment.controller.CommentController;
import com.issuetracker.domain.comment.entity.Comment;
import com.issuetracker.domain.issue.controller.IssueController;
import com.issuetracker.domain.issue.entity.Issue;
import com.issuetracker.domain.issue.enums.Status;
import com.issuetracker.domain.project.controller.ProjectController;
import com.issuetracker.domain.project.entity.ProjectMember;
import com.issuetracker.domain.recommend.controller.RecommendController;
import com.issuetracker.global.common.Response;
import com.issuetracker.global.common.SessionManager;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class IssueDetailDialog extends JDialog {
    private final AccountController accountController;
    private final ProjectController projectController;
    private final IssueController issueController;
    private final CommentController commentController;
    private final RecommendController recommendController;
    private final SessionManager sessionManager;
    private final Long issueId;
    private final Runnable refreshTable;
    @Getter
    private boolean initialized;

    public IssueDetailDialog(
            JFrame owner,
            Long issueId,
            Runnable refreshTable,
            AccountController accountController,
            ProjectController projectController,
            IssueController issueController,
            CommentController commentController,
            RecommendController recommendController,
            SessionManager sessionManager
    ) {
        super(owner, "Issue Detail #" + issueId, true);
        this.issueId = issueId;
        this.refreshTable = refreshTable;
        this.accountController = accountController;
        this.projectController = projectController;
        this.issueController = issueController;
        this.commentController = commentController;
        this.recommendController = recommendController;
        this.sessionManager = sessionManager;

        initDialog(owner);
    }

    private void initDialog(JFrame owner) {
        Response<Issue> issueResp = issueController.getIssueDetail(issueId);
        if (!issueResp.isSuccess()) {
            JOptionPane.showMessageDialog(owner, issueResp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Issue issue = issueResp.getData();
        Role projectRole = getProjectRole(issue.getProjectId());

        setSize(700, 600);
        setLayout(new BorderLayout(10, 10));

        add(createInfoPanel(issue), BorderLayout.NORTH);

        DefaultListModel<Comment> commentListModel = new DefaultListModel<>();
        JList<Comment> commentList = new JList<>(commentListModel);
        commentList.setCellRenderer(this::renderComment);

        Runnable refreshComment = () -> refreshComments(commentListModel);
        refreshComment.run();

        add(new JScrollPane(commentList), BorderLayout.CENTER);
        add(createActionsPanel(issue, projectRole, commentList, refreshComment), BorderLayout.SOUTH);
        setLocationRelativeTo(owner);
        initialized = true;
    }

    private JPanel createInfoPanel(Issue issue) {
        JPanel info = new JPanel(new GridLayout(0, 1));
        info.setBorder(BorderFactory.createTitledBorder("Information"));

        info.add(new JLabel("Title: " + issue.getTitle()));
        info.add(new JLabel("Priority: " + issue.getPriority()));
        info.add(new JLabel("Status: " + issue.getStatus()));
        info.add(new JLabel("Description: " + issue.getDescription()));
        info.add(new JLabel("Reporter name: " + getUsernameOrUnknown(issue.getReporterId())));
        info.add(new JLabel("Assignee name: " + getUsernameOrUnknown(issue.getAssigneeId())));

        return info;
    }

    private Component renderComment(
            JList<? extends Comment> list,
            Comment value,
            int index,
            boolean isSelected,
            boolean cellHasFocus
    ) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss");
        String authorName = getUsernameOrUnknown(value.getAuthorId());
        String dateStr = (value.getUpdatedDate() != null)
                ? " [" + value.getUpdatedDate().format(formatter) + "]"
                : "";

        JLabel label = new JLabel(authorName + ": " + value.getContent() + dateStr);
        label.setOpaque(true);
        label.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
        label.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
        return label;
    }

    private void refreshComments(DefaultListModel<Comment> commentListModel) {
        commentListModel.clear();
        Response<List<Comment>> resp = commentController.listComments(issueId);
        if (resp.isSuccess()) {
            for (Comment comment : resp.getData()) {
                commentListModel.addElement(comment);
            }
        } else {
            JOptionPane.showMessageDialog(this, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createActionsPanel(
            Issue issue,
            Role projectRole,
            JList<Comment> commentList,
            Runnable refreshComment
    ) {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Account curuser = sessionManager.getLoggedInAccount();

        JButton addCommentBtn = new JButton("Create Comment");
        addCommentBtn.addActionListener(e -> createComment(refreshComment));
        actions.add(addCommentBtn);

        JButton updateCommentButton = new JButton("Update Comment");
        updateCommentButton.addActionListener(e -> updateComment(commentList, refreshComment));
        actions.add(updateCommentButton);

        if (issue.getStatus() == Status.NEW) {
            addNewIssueActions(actions, issue, projectRole);
        } else if ((issue.getStatus() == Status.ASSIGNED || issue.getStatus() == Status.REOPENED)
                && projectRole == Role.DEV
                && curuser.getAccountId().equals(issue.getAssigneeId())) {
            addFixAction(actions);
        } else if (issue.getStatus() == Status.FIXED
                && projectRole == Role.TESTER
                && curuser.getAccountId().equals(issue.getReporterId())) {
            addResolveAction(actions);
        } else if (issue.getStatus() == Status.RESOLVED && projectRole == Role.PL) {
            addCloseAction(actions);
        } else if (issue.getStatus() == Status.CLOSED && projectRole == Role.PL) {
            addReopenAction(actions);
        }

        return actions;
    }

    private void createComment(Runnable refreshComment) {
        String content = JOptionPane.showInputDialog("Comment content:");
        if (content == null || content.trim().isEmpty()) {
            return;
        }

        Response<Comment> resp = commentController.createComment(issueId, content);
        if (resp.isSuccess()) {
            refreshComment.run();
        } else {
            JOptionPane.showMessageDialog(this, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateComment(JList<Comment> commentList, Runnable refreshComment) {
        Comment selected = commentList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a comment first.");
            return;
        }

        String updated = JOptionPane.showInputDialog(this, "Update comment:", selected.getContent());
        if (updated == null || updated.trim().isEmpty()) {
            return;
        }

        Response<Comment> resp = commentController.updateComment(selected.getCommentId(), updated);
        if (resp.isSuccess()) {
            refreshComment.run();
        } else {
            JOptionPane.showMessageDialog(this, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addNewIssueActions(JPanel actions, Issue issue, Role projectRole) {
        JButton assign = new JButton("Assign");
        assign.addActionListener(e -> assignIssue());

        JButton recommend = new JButton("Recommend");
        recommend.addActionListener(e -> recommendAssignees(issue));

        if (projectRole == Role.PL) {
            actions.add(assign);
            actions.add(recommend);
        }
    }

    private void assignIssue() {
        String targetName = JOptionPane.showInputDialog("Target Dev name:");
        String message = JOptionPane.showInputDialog("Assignment Message:");
        if (targetName == null || message == null) {
            return;
        }

        Response<Long> accountIdResp = accountController.getAccountIdByUsername(targetName);
        if (!accountIdResp.isSuccess() || accountIdResp.getData() == null) {
            JOptionPane.showMessageDialog(this, accountIdResp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Response<Issue> assignResp = issueController.assignIssue(issueId, accountIdResp.getData());
        if (assignResp.isSuccess()) {
            commentController.createComment(issueId, message);
            dispose();
            refreshTable.run();
        } else {
            JOptionPane.showMessageDialog(this, assignResp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void recommendAssignees(Issue issue) {
        List<Account> recommendedAssignees = recommendController.getRecommendedAssignees(
                issue.getProjectId(),
                issue.getTitle(),
                issue.getDescription()
        );

        if (recommendedAssignees == null || recommendedAssignees.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No recommendations available.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder("Recommended users:\n");
        for (Account account : recommendedAssignees) {
            sb.append("- ").append(account.getUsername()).append("\n");
        }
        JOptionPane.showMessageDialog(this, sb.toString(), "Recommendation Complete", JOptionPane.INFORMATION_MESSAGE);
    }

    private void addFixAction(JPanel actions) {
        JButton fix = new JButton("Fix Issue");
        fix.addActionListener(e -> {
            String message = JOptionPane.showInputDialog("Fixing Message:");
            if (message == null) {
                return;
            }

            commentController.createComment(issueId, message);
            Response<Issue> resp = issueController.fixIssue(issueId);
            handleStateChangeResponse(resp);
        });
        actions.add(fix);
    }

    private void addResolveAction(JPanel actions) {
        JButton resolve = new JButton("Resolve issue");
        resolve.addActionListener(e -> handleStateChangeResponse(issueController.resolveIssue(issueId)));
        actions.add(resolve);
    }

    private void addCloseAction(JPanel actions) {
        JButton close = new JButton("Close issue");
        close.addActionListener(e -> handleStateChangeResponse(issueController.closeIssue(issueId)));
        actions.add(close);
    }

    private void addReopenAction(JPanel actions) {
        JButton reopen = new JButton("Reopen issue");
        reopen.addActionListener(e -> handleStateChangeResponse(issueController.reopenIssue(issueId)));
        actions.add(reopen);
    }

    private void handleStateChangeResponse(Response<Issue> resp) {
        if (resp.isSuccess()) {
            dispose();
            refreshTable.run();
        } else {
            JOptionPane.showMessageDialog(this, resp.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Role getProjectRole(Long projectId) {
        if (projectId == null || sessionManager.getLoggedInAccount() == null) {
            return null;
        }

        Long currentAccountId = sessionManager.getLoggedInAccount().getAccountId();
        Response<List<ProjectMember>> memberResp = projectController.listProjectMembers(projectId);

        if (!memberResp.isSuccess() || memberResp.getData() == null) {
            return null;
        }

        return memberResp.getData().stream()
                .filter(member -> member.getAccountId().equals(currentAccountId))
                .map(ProjectMember::getRole)
                .findFirst()
                .orElse(null);
    }

    private String getUsernameOrUnknown(Long accountId) {
        Response<Account> accountResp = accountController.getAccountById(accountId);
        if (accountResp.isSuccess() && accountResp.getData() != null) {
            return accountResp.getData().getUsername();
        }
        return "";
    }
}
