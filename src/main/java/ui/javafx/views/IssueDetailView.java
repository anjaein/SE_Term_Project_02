package ui.javafx.views;

import ui.javafx.App;
import ui.javafx.BackendFacade;
import ui.javafx.ViewLoader;
import ui.javafx.controllers.IssueDetailController;
import javafx.scene.Parent;

public class IssueDetailView {
    private final Parent root;

    public IssueDetailView(BackendFacade facade, long issueId, App app){
        ViewLoader.LoadedView<IssueDetailController> loaded=ViewLoader.load("/ui/javafx/views/issue-detail-view.fxml");
        root=loaded.root();
        loaded.controller().init(facade, issueId, app);
    }

    public Parent getRoot(){
        return root;
    }
}
