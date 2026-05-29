package ui.javafx.views;

import ui.javafx.App;
import ui.javafx.BackendFacade;
import ui.javafx.ViewLoader;
import ui.javafx.controllers.ProjectsController;
import javafx.scene.Parent;

public class ProjectsView {
    private final Parent root;

    public ProjectsView(BackendFacade facade, App app){
        ViewLoader.LoadedView<ProjectsController> loaded=ViewLoader.load("/ui/javafx/views/projects-view.fxml");
        root=loaded.root();
        loaded.controller().init(facade, app);
    }

    public Parent getRoot(){
        return root;
    }
}
