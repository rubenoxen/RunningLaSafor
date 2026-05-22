package runninglasafor.controllers;

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

public class ProfileController implements Initializable {

    private static final int MIN_AGE = 12;

    @FXML
    private Label nickLabel;
    @FXML
    private Label emailReadLabel;
    @FXML
    private ImageView avatarPreview;
    @FXML
    private TextField emailField;
    @FXML
    private DatePicker birthPicker;
    @FXML
    private TextField avatarField;
    @FXML
    private PasswordField passField;
    @FXML
    private PasswordField confirmField;
    @FXML
    private Label errorLabel;

    private RootLayoutController root;
    private ResourceBundle bundle;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.bundle = rb;
        errorLabel.setText("");
        loadCurrentUser();
    }

    public void setRoot(RootLayoutController root) {
        this.root = root;
    }

    private void loadCurrentUser() {
        User u = SportActivityApp.getInstance().getCurrentUser();
        if (u == null) return;
        nickLabel.setText(u.getNickName());
        emailReadLabel.setText(u.getEmail());
        emailField.setText(u.getEmail());
        birthPicker.setValue(u.getBirthDate());
        avatarField.setText(u.getAvatarPath() == null ? "" : u.getAvatarPath());
        Image av = u.getAvatar();
        if (av != null) {
            avatarPreview.setImage(av);
        }
    }

    @FXML
    private void onBrowseAvatar(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle(bundle.getString("register.dialog.title"));
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                bundle.getString("register.dialog.images"), "*.png", "*.jpg", "*.jpeg"));
        Stage stage = (Stage) emailField.getScene().getWindow();
        File file = fc.showOpenDialog(stage);
        if (file != null) {
            avatarField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void onSave(ActionEvent event) {
        String email = trim(emailField.getText());
        String pass = passField.getText() == null ? "" : passField.getText();
        String confirm = confirmField.getText() == null ? "" : confirmField.getText();
        LocalDate birth = birthPicker.getValue();
        String avatarPath = trim(avatarField.getText());
        if (avatarPath.isEmpty()) avatarPath = null;

        User current = SportActivityApp.getInstance().getCurrentUser();
        if (current == null) return;

        String error = validate(email, pass, confirm, birth);
        if (error != null) {
            errorLabel.setText(error);
            return;
        }

        String finalPass = pass.isEmpty() ? current.getPassword() : pass;
        boolean ok = SportActivityApp.getInstance()
                .updateCurrentUser(email, finalPass, birth, avatarPath);
        if (!ok) {
            errorLabel.setText(bundle.getString("profile.errorUpdate"));
            return;
        }

        errorLabel.setText("");
        passField.clear();
        confirmField.clear();
        loadCurrentUser();

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setHeaderText(bundle.getString("profile.ok.header"));
        info.setContentText(bundle.getString("profile.ok.content"));
        info.showAndWait();
    }

    @FXML
    private void onBack(ActionEvent event) {
        if (root != null) {
            root.showHome();
        }
    }

    @FXML
    private void onLogout(ActionEvent event) {
        SportActivityApp.getInstance().logout();
        if (root != null) {
            root.showLogin();
        }
    }

    private String validate(String email, String pass, String confirm, LocalDate birth) {
        if (email.isEmpty() || birth == null) {
            return bundle.getString("register.errorFill");
        }
        if (!User.checkEmail(email)) {
            return bundle.getString("profile.errorEmail");
        }
        if (!User.isOlderThan(birth, MIN_AGE)) {
            return MessageFormat.format(
                    bundle.getString("register.errorAge"), MIN_AGE);
        }
        if (!pass.isEmpty()) {
            if (!User.checkPassword(pass)) {
                return bundle.getString("profile.errorPassword");
            }
            if (!pass.equals(confirm)) {
                return bundle.getString("profile.errorConfirm");
            }
        }
        return null;
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
