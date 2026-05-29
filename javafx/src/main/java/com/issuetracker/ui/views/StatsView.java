package com.issuetracker.ui.views;

import com.issuetracker.ui.BackendFacade;
import com.issuetracker.ui.ViewLoader;
import com.issuetracker.ui.controllers.StatsController;
import javafx.scene.Parent;

public class StatsView {
    private final Parent root;

    public StatsView(BackendFacade facade){
        ViewLoader.LoadedView<StatsController> loaded=ViewLoader.load("/com/issuetracker/ui/fxml/stats-view.fxml");
        root=loaded.root();
        loaded.controller().init(facade);
    }

    public Parent getRoot(){
        return root;
    }
}
