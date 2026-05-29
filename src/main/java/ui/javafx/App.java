package ui.javafx;

import ui.javafx.domain.*;
import ui.javafx.controllers.AppController;
import ui.javafx.views.*;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Objects;


public class App extends Application {

    public enum View { PROJECTS, BOARD, ISSUE, STATS, MEMBERS }

    private final StackPane rootStack=new StackPane();
    private AppController appController;
    private Parent appRoot;
    private View currentView=View.PROJECTS;
    private Long openIssueId=null;
    private BackendFacade facade;

    @Override
    public void start(Stage stage){
        facade=BackendFacade.get();
        //1280*820 화면
        Scene scene=new Scene(rootStack, 1280, 820);
        scene.getStylesheets().add(Objects.requireNonNull(
            getClass().getResource("/ui/javafx/app.css")).toExternalForm());

        renderRoot();

        //useState(currentUser, ... ) 역할
        facade.currentUser.addListener((o, was, now)->renderRoot());

        stage.setTitle("Issue Tracker");
        stage.setScene(scene);
        stage.show();
    }

    private void renderRoot(){
        rootStack.getChildren().clear();
        if (facade.currentUser.get()==null) {
            LoginView loginview=new LoginView(facade, account -> onLogin(account));
            rootStack.getChildren().add(loginview.getRoot());
        } else {
            renderApp();
            rootStack.getChildren().add(appRoot);
        }
    }

    private void onLogin(Account account){
        //BackendFacade.login updates the session and currentUser; listener above rerenders.
    }

    private void renderApp(){
        ViewLoader.LoadedView<AppController> loaded=ViewLoader.load("/ui/javafx/views/app.fxml");
        appRoot=loaded.root();
        appController=loaded.controller();
        appController.init(facade, this);
        switchView(currentView, openIssueId);
    }

    public void switchView(View v, Long issueId){

        currentView=v;

        if (issueId!=null) openIssueId=issueId;        

        appController.updateNavHighlight(currentView);

        switch (v) {
            case PROJECTS:
                ProjectsView projectsView=new ProjectsView(facade, this);
                appController.setCenter(projectsView.getRoot());
                break;
            case BOARD:
                BoardView boardView=new BoardView(facade, this);
                appController.setCenter(boardView.getRoot());
                break;
            case ISSUE:
                if (openIssueId==null) {
                    switchView(View.BOARD, null);
                    return;
                }
                IssueDetailView issueDetailView=new IssueDetailView(facade, openIssueId, this);
                appController.setCenter(issueDetailView.getRoot());
                break;
            case STATS:
                StatsView statsView=new StatsView(facade);
                appController.setCenter(statsView.getRoot());
                break;
            case MEMBERS:
                MembersView membersView=new MembersView(facade);
                appController.setCenter(membersView.getRoot());
                break;
        }
    }

    public void openIssue(long id){
        openIssueId=id;
        switchView(View.ISSUE, id);
    }

    public void openCreateIssueDialog(){
        Account me=facade.currentUser.get();
        if (me==null) return;
        new CreateIssueDialog(facade, rootStack.getScene().getWindow(), created -> {
            openIssue(created.getIssueId());
        }).show();
    }

    public StackPane rootStack(){ return rootStack; }

    public static void main(String[] args){
        launch(args);
    }
}
