package runninglasafor.controllers;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import runninglasafor.MainApp;
import upv.ipc.sportlib.SportActivityApp;

public class LoginController implements Initializable {

    private static final double FORM_CARD_NORMAL_WIDTH = 500.0;
    private static final double FORM_CARD_MAXIMIZED_WIDTH = 600.0;

    @FXML private VBox authRoot;
    @FXML private TextField userField;
    @FXML private PasswordField passField;
    @FXML private TextField passVisibleField;
    @FXML private Button showPassButton;
    @FXML private Region passToggleIcon;
    @FXML private Label errorLabel;
    @FXML private CheckBox rememberCheck;
    @FXML private ComboBox<String> languageBox;
    @FXML private Region themeIcon;
    @FXML private ImageView brandLogo;
    @FXML private StackPane heroStack;
    @FXML private VBox formCard;
    @FXML private VBox formSide;

    private RootLayoutController root;
    private ResourceBundle bundle;
    private boolean passVisible;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.bundle = rb;
        errorLabel.setText("");
        setupLanguageBox();
        applyTheme();
        setupHeroBackground();
        setupPassToggle();
        Platform.runLater(this::setupMaximizeListener);
    }

    private void setupPassToggle() {
        updatePassToggleIcon();
    }

    private void setupHeroBackground() {
        if (heroStack == null) return;
        URL imgUrl = getClass().getResource("/resources/LoginCorredora.png");
        if (imgUrl == null) return;
        heroStack.setStyle(
                "-fx-background-color: #120D31;"
                + "-fx-background-image: url('" + imgUrl.toExternalForm() + "');"
                + "-fx-background-size: cover;"
                + "-fx-background-position: center 35%;"
                + "-fx-background-repeat: no-repeat;");
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

    private void setupMaximizeListener() {
        if (authRoot == null) return;
        Scene scene = authRoot.getScene();
        if (scene == null) {
            authRoot.sceneProperty().addListener((obs, oldS, newS) -> {
                if (newS != null) bindToStage(newS);
            });
            return;
        }
        bindToStage(scene);
    }

    private void bindToStage(Scene scene) {
        Window window = scene.getWindow();
        if (window instanceof Stage) {
            attachStageListener((Stage) window);
        } else {
            scene.windowProperty().addListener((obs, oldW, newW) -> {
                if (newW instanceof Stage) {
                    attachStageListener((Stage) newW);
                }
            });
        }
    }

    private void attachStageListener(Stage stage) {
        stage.maximizedProperty().addListener((obs, oldV, newV) -> updateFormCardSize(newV));
        updateFormCardSize(stage.isMaximized());
    }

    private void updateFormCardSize(boolean maximized) {
        if (formCard == null) return;
        formCard.setMaxWidth(maximized ? FORM_CARD_MAXIMIZED_WIDTH : FORM_CARD_NORMAL_WIDTH);
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
    private void onTogglePassVisible(ActionEvent event) {
        passVisible = !passVisible;
        if (passVisible) {
            passVisibleField.setText(passField.getText());
            passField.setVisible(false);
            passField.setManaged(false);
            passVisibleField.setVisible(true);
            passVisibleField.setManaged(true);
            passVisibleField.requestFocus();
            passVisibleField.positionCaret(passVisibleField.getText().length());
        } else {
            passField.setText(passVisibleField.getText());
            passVisibleField.setVisible(false);
            passVisibleField.setManaged(false);
            passField.setVisible(true);
            passField.setManaged(true);
            passField.requestFocus();
            passField.end();
        }
        updatePassToggleIcon();
    }

    private void updatePassToggleIcon() {
        if (passToggleIcon == null || showPassButton == null || bundle == null) {
            return;
        }
        passToggleIcon.getStyleClass().removeAll("pass-eye-icon", "pass-eye-off-icon");
        if (passVisible) {
            passToggleIcon.getStyleClass().add("pass-eye-off-icon");
            showPassButton.setTooltip(new Tooltip(bundle.getString("login.hidePassword")));
        } else {
            passToggleIcon.getStyleClass().add("pass-eye-icon");
            showPassButton.setTooltip(new Tooltip(bundle.getString("login.showPassword")));
        }
    }

    private String getPassText() {
        if (passVisible && passVisibleField != null) {
            return passVisibleField.getText() == null ? "" : passVisibleField.getText();
        }
        return passField.getText() == null ? "" : passField.getText();
    }

    private void clearPassFields() {
        passField.clear();
        if (passVisibleField != null) {
            passVisibleField.clear();
        }
    }

    @FXML
    private void onLogin(ActionEvent event) {
        String user = userField.getText() == null ? "" : userField.getText().trim();
        String pass = getPassText();

        if (user.isEmpty() || pass.isEmpty()) {
            errorLabel.setText(bundle.getString("login.errorEmpty"));
            return;
        }

        boolean ok = SportActivityApp.getInstance().login(user, pass);
        if (!ok) {
            errorLabel.setText(bundle.getString("login.errorInvalid"));
            clearPassFields();
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

    @FXML
    private void onForgot(ActionEvent event) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(bundle.getString("login.forgot.header"));
        a.setContentText(bundle.getString("login.forgot.content"));
        a.showAndWait();
    }

    @FXML
    private void onMoreInfo(ActionEvent event) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(bundle.getString("about.title"));
        a.setHeaderText(bundle.getString("about.header"));
        a.setContentText(bundle.getString("about.content"));
        a.showAndWait();
    }
}
