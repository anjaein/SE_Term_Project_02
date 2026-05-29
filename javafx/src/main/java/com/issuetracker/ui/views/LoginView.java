package com.issuetracker.ui.views;

import com.issuetracker.ui.BackendFacade;
import com.issuetracker.ui.ViewLoader;
import com.issuetracker.ui.controllers.LoginController;
import com.issuetracker.ui.domain.Account;
import javafx.scene.Parent;

import java.util.function.Consumer;

public class LoginView {
    private final Parent root;

    public LoginView(BackendFacade facade, Consumer<Account> onLogin){
        ViewLoader.LoadedView<LoginController> loaded=ViewLoader.load("/com/issuetracker/ui/fxml/login-view.fxml");
        root=loaded.root();
        loaded.controller().init(facade, onLogin);
    }

    public Parent getRoot(){
        return root;
    }
}
