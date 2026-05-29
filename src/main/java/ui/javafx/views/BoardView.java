package ui.javafx.views;

import ui.javafx.App;
import ui.javafx.BackendFacade;
import ui.javafx.ViewLoader;
import ui.javafx.controllers.BoardController;
import javafx.scene.Parent;

public class BoardView {
    private final Parent root;

    public BoardView(BackendFacade facade, App app){
        ViewLoader.LoadedView<BoardController> loaded=ViewLoader.load("/ui/javafx/views/board-view.fxml");
        root=loaded.root();
        loaded.controller().init(facade, app);
    }

    public Parent getRoot(){
        return root;
    }
}
