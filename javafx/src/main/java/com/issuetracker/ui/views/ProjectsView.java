package com.issuetracker.ui.views;

import com.issuetracker.ui.App;
import com.issuetracker.ui.BackendFacade;
import com.issuetracker.ui.ViewLoader;
import com.issuetracker.ui.controllers.ProjectsController;
import javafx.scene.Parent;

public class ProjectsView {
    private final Parent root;

    public ProjectsView(BackendFacade facade, App app){
        ViewLoader.LoadedView<ProjectsController> loaded=ViewLoader.load("/com/issuetracker/ui/fxml/projects-view.fxml");
        root=loaded.root();
        loaded.controller().init(facade, app);
    }

    public Parent getRoot(){
        return root;
    }
}
