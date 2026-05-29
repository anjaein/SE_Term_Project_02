package ui.javafx.views;

import ui.javafx.BackendFacade;
import ui.javafx.ViewLoader;
import ui.javafx.controllers.MembersController;
import javafx.scene.Parent;

public class MembersView {
    private final Parent root;

    public MembersView(BackendFacade facade){
        ViewLoader.LoadedView<MembersController> loaded=ViewLoader.load("/ui/javafx/views/members-view.fxml");
        root=loaded.root();
        loaded.controller().init(facade);
    }

    public Parent getRoot(){
        return root;
    }
}
