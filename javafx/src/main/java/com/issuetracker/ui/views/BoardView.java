package com.issuetracker.ui.views;

import com.issuetracker.ui.App;
import com.issuetracker.ui.BackendFacade;
import com.issuetracker.ui.ViewLoader;
import com.issuetracker.ui.controllers.BoardController;
import javafx.scene.Parent;

public class BoardView {
    private final Parent root;

    public BoardView(BackendFacade facade, App app){
        ViewLoader.LoadedView<BoardController> loaded=ViewLoader.load("/com/issuetracker/ui/fxml/board-view.fxml");
        root=loaded.root();
        loaded.controller().init(facade, app);
    }

    public Parent getRoot(){
        return root;
    }
}
