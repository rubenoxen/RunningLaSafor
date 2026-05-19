package runninglasafor.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import upv.ipc.sportlib.SportActivityApp;

public class LoginController implements Initializable {

    @FXML
    private TextField userField;
    @FXML
    private PasswordField passField;
    @FXML
    private Label errorLabel;

    private RootLayoutController root;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        errorLabel.setText("");
    }

    public void setRoot(RootLayoutController root) {
        this.root = root;
    }

    @FXML
    private void onLogin(ActionEvent event) {
        String user = userField.getText() == null ? "" : userField.getText().trim();
        String pass = passField.getText() == null ? "" : passField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Introduce usuario y contrasena.");
            return;
        }

        boolean ok = SportActivityApp.getInstance().login(user, pass);
        if (!ok) {
            errorLabel.setText("Usuario o contrasena incorrectos.");
            passField.clear();
            return;
        }

        errorLabel.setText("");
        if (root != null) {
            root.showHome();
        }
    }

    @FXML
    private void onRegister(ActionEvent event) {
        if (root != null) {
            root.showRegister();
        }
    }
}
