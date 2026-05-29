package ui.javafx.views;

import ui.javafx.BackendFacade;
import ui.javafx.ViewLoader;
import ui.javafx.controllers.LoginController;
import ui.javafx.domain.Account;
import javafx.scene.Parent;

import java.util.function.Consumer;

public class LoginView {
    private final Parent root;

    public LoginView(BackendFacade facade, Consumer<Account> onLogin){
        ViewLoader.LoadedView<LoginController> loaded=ViewLoader.load("/ui/javafx/views/login-view.fxml");
        root=loaded.root();
        loaded.controller().init(facade, onLogin);
    }

    public Parent getRoot(){
        return root;
    }
}
