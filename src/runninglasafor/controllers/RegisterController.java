package runninglasafor.controllers;

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import runninglasafor.MainApp;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

public class RegisterController implements Initializable {

    private static final int MIN_AGE = 12;

    @FXML private VBox authRoot;
    @FXML private TextField nickField;
    @FXML private TextField emailField;
    @FXML private PasswordField passField;
    @FXML private PasswordField confirmField;
    @FXML private DatePicker birthPicker;
    @FXML private TextField avatarField;
    @FXML private Label errorLabel;
    @FXML private ComboBox<String> languageBox;
    @FXML private Region themeIcon;

    private RootLayoutController root;
    private ResourceBundle bundle;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.bundle = rb;
        errorLabel.setText("");
        setupLanguageBox();
        applyTheme();
    }

    private void setupLanguageBox() {
        if (languageBox == null) return;
        languageBox.getItems().setAll("ES", "EN", "FR", "DE", "ZH");
        String current = MainApp.getCurrentLocale().getLanguage().toUpperCase();
        languageBox.setValue(current);
        languageBox.setOnAction(e -> {
            String sel = languageBox.getValue();
            if (sel == null) return;
            MainApp.changeLocale(new Locale(sel.toLowerCase()));
        });
    }

    private void applyTheme() {
        if (themeIcon != null) {
            boolean light = !MainApp.isLightTheme();
            themeIcon.getStyleClass().removeAll("theme-moon", "theme-sun");
            themeIcon.getStyleClass().add(light ? "theme-sun" : "theme-moon");
        }
    }

    @FXML
    private void onToggleTheme(ActionEvent event) {
        MainApp.toggleTheme();
        applyTheme();
        if (root != null) {
            root.refreshChromeTheme();
        }
    }

    public void setRoot(RootLayoutController root) {
        this.root = root;
    }

    @FXML
    private void onBrowseAvatar(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle(bundle.getString("register.dialog.title"));
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                bundle.getString("register.dialog.images"), "*.png", "*.jpg", "*.jpeg"));
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
            errorLabel.setText(bundle.getString("register.errorTaken"));
            return;
        }

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setHeaderText(bundle.getString("register.ok.header"));
        info.setContentText(bundle.getString("register.ok.content"));
        info.getDialogPane().getStylesheets().add(
                getClass().getResource("/runninglasafor/resources/estilos.css").toExternalForm());
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
            return bundle.getString("register.errorFill");
        }
        if (!User.checkNickName(nick)) {
            return bundle.getString("register.errorNick");
        }
        if (!User.checkEmail(email)) {
            return bundle.getString("register.errorEmail");
        }
        if (!User.checkPassword(pass)) {
            return bundle.getString("register.errorPassword");
        }
        if (!pass.equals(confirm)) {
            return bundle.getString("register.errorConfirm");
        }
        if (!User.isOlderThan(birth, MIN_AGE)) {
            return MessageFormat.format(
                    bundle.getString("register.errorAge"), MIN_AGE);
        }
        return null;
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
