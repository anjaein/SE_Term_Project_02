package ui.javafx.views;

import ui.javafx.BackendFacade;
import ui.javafx.ViewLoader;
import ui.javafx.controllers.StatsController;
import javafx.scene.Parent;

public class StatsView {
    private final Parent root;

    public StatsView(BackendFacade facade){
        ViewLoader.LoadedView<StatsController> loaded=ViewLoader.load("/ui/javafx/views/stats-view.fxml");
        root=loaded.root();
        loaded.controller().init(facade);
    }

    public Parent getRoot(){
        return root;
    }
}
