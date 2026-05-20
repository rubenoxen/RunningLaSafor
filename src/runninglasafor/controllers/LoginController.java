package runninglasafor.controllers;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import runninglasafor.MainApp;
import upv.ipc.sportlib.SportActivityApp;

public class LoginController implements Initializable {

    @FXML
    private HBox authRoot;
    @FXML
    private TextField userField;
    @FXML
    private PasswordField passField;
    @FXML
    private Label errorLabel;
    @FXML
    private ComboBox<String> languageBox;
    @FXML
    private Region themeIcon;
    @FXML
    private ImageView bgImage;

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
        boolean light = MainApp.isLightTheme();
        if (authRoot != null) {
            if (light && !authRoot.getStyleClass().contains("theme-light")) {
                authRoot.getStyleClass().add("theme-light");
            } else if (!light) {
                authRoot.getStyleClass().remove("theme-light");
            }
        }
        if (themeIcon != null) {
            themeIcon.getStyleClass().removeAll("theme-moon", "theme-sun");
            themeIcon.getStyleClass().add(light ? "theme-sun" : "theme-moon");
        }
        if (bgImage != null) {
            String path = light ? "/resources/running_bg_light.png" : "/resources/running_bg.png";
            bgImage.setImage(new Image(getClass().getResource(path).toExternalForm()));
            bgImage.setBlendMode(light ? BlendMode.SRC_OVER : BlendMode.MULTIPLY);
            bgImage.setOpacity(light ? 0.9 : 0.65);
        }
    }

    @FXML
    private void onToggleTheme(ActionEvent event) {
        MainApp.toggleTheme();
        applyTheme();
    }

    public void setRoot(RootLayoutController root) {
        this.root = root;
    }

    @FXML
    private void onLogin(ActionEvent event) {
        String user = userField.getText() == null ? "" : userField.getText().trim();
        String pass = passField.getText() == null ? "" : passField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            errorLabel.setText(bundle.getString("login.errorEmpty"));
            return;
        }

        boolean ok = SportActivityApp.getInstance().login(user, pass);
        if (!ok) {
            errorLabel.setText(bundle.getString("login.errorInvalid"));
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
