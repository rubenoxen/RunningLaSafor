package runninglasafor.controllers;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

public class RegisterController implements Initializable {

    private static final int MIN_AGE = 16;

    @FXML
    private TextField nickField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passField;
    @FXML
    private PasswordField confirmField;
    @FXML
    private DatePicker birthPicker;
    @FXML
    private TextField avatarField;
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
    private void onBrowseAvatar(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Selecciona una imagen de avatar");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imagenes", "*.png", "*.jpg", "*.jpeg"));
        Stage stage = (Stage) nickField.getScene().getWindow();
        File file = fc.showOpenDialog(stage);
        if (file != null) {
            avatarField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void onCreate(ActionEvent event) {
        String nick = trim(nickField.getText());
        String email = trim(emailField.getText());
        String pass = passField.getText() == null ? "" : passField.getText();
        String confirm = confirmField.getText() == null ? "" : confirmField.getText();
        LocalDate birth = birthPicker.getValue();
        String avatarPath = trim(avatarField.getText());
        if (avatarPath.isEmpty()) avatarPath = null;

        String error = validate(nick, email, pass, confirm, birth);
        if (error != null) {
            errorLabel.setText(error);
            return;
        }

        boolean ok = SportActivityApp.getInstance()
                .registerUser(nick, email, pass, birth, avatarPath);
        if (!ok) {
            errorLabel.setText("El usuario o el email ya estan en uso.");
            return;
        }

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setHeaderText("Cuenta creada");
        info.setContentText("Ya puedes iniciar sesion con tus datos.");
        info.showAndWait();

        if (root != null) {
            root.showLogin();
        }
    }

    @FXML
    private void onBack(ActionEvent event) {
        if (root != null) {
            root.showLogin();
        }
    }

    private String validate(String nick, String email, String pass,
                            String confirm, LocalDate birth) {
        if (nick.isEmpty() || email.isEmpty() || pass.isEmpty()
                || confirm.isEmpty() || birth == null) {
            return "Rellena todos los campos obligatorios.";
        }
        if (!User.checkNickName(nick)) {
            return "El nombre de usuario no es valido.";
        }
        if (!User.checkEmail(email)) {
            return "El email no tiene un formato valido.";
        }
        if (!User.checkPassword(pass)) {
            return "La contrasena no cumple los requisitos.";
        }
        if (!pass.equals(confirm)) {
            return "Las contrasenas no coinciden.";
        }
        if (!User.isOlderThan(birth, MIN_AGE)) {
            return "Debes tener al menos " + MIN_AGE + " anos.";
        }
        return null;
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
