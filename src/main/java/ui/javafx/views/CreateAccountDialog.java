package ui.javafx.views;

import ui.javafx.BackendFacade;
import ui.javafx.ViewLoader;
import ui.javafx.controllers.CreateAccountDialogController;
import ui.javafx.domain.Account;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.function.Consumer;

public class CreateAccountDialog {
    private final BackendFacade facade;
    private final Window owner;
    private final Consumer<Account> onCreate;

    public CreateAccountDialog(BackendFacade facade, Window owner, Consumer<Account> onCreate){
        this.facade=facade;
        this.owner=owner;
        this.onCreate=onCreate;
    }

    public void show(){
        ViewLoader.LoadedView<CreateAccountDialogController> loaded=
            ViewLoader.load("/ui/javafx/views/create-account-dialog.fxml");
        loaded.controller().init(facade, onCreate);

        Dialog<ButtonType> d=new Dialog<>();
        d.initOwner(owner);
        d.initModality(Modality.APPLICATION_MODAL);
        d.setTitle("계정 생성");
        d.setHeaderText(null);

        DialogPane dp=d.getDialogPane();
        dp.setContent(loaded.root());
        dp.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        if (owner!=null && owner.getScene()!=null) {
            dp.getStylesheets().setAll(owner.getScene().getStylesheets());
        }

        Node okButton=dp.lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (!loaded.controller().createAccount()) event.consume();
        });

        ((Stage) dp.getScene().getWindow()).setTitle("계정 생성");
        d.showAndWait();
    }
}
