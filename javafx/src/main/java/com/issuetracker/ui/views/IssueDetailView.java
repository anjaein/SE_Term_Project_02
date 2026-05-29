package com.issuetracker.ui.views;

import com.issuetracker.ui.App;
import com.issuetracker.ui.BackendFacade;
import com.issuetracker.ui.ViewLoader;
import com.issuetracker.ui.controllers.IssueDetailController;
import javafx.scene.Parent;

public class IssueDetailView {
    private final Parent root;

    public IssueDetailView(BackendFacade facade, long issueId, App app){
        ViewLoader.LoadedView<IssueDetailController> loaded=ViewLoader.load("/com/issuetracker/ui/fxml/issue-detail-view.fxml");
        root=loaded.root();
        loaded.controller().init(facade, issueId, app);
    }

    public Parent getRoot(){
        return root;
    }
}
