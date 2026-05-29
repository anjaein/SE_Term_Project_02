package com.issuetracker.ui.views;

import com.issuetracker.ui.BackendFacade;
import com.issuetracker.ui.ViewLoader;
import com.issuetracker.ui.controllers.MembersController;
import javafx.scene.Parent;

public class MembersView {
    private final Parent root;

    public MembersView(BackendFacade facade){
        ViewLoader.LoadedView<MembersController> loaded=ViewLoader.load("/com/issuetracker/ui/fxml/members-view.fxml");
        root=loaded.root();
        loaded.controller().init(facade);
    }

    public Parent getRoot(){
        return root;
    }
}
