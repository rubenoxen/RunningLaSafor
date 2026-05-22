package runninglasafor.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import runninglasafor.MainApp;
import runninglasafor.MainApp.View;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

public class RootLayoutController implements Initializable {

    @FXML
    private BorderPane rootPane;
    @FXML
    private MenuBar menuBar;
    @FXML
    private MenuItem miImport;
    @FXML
    private MenuItem miLogout;
    @FXML
    private Menu menuActivities;
    @FXML
    private Menu menuProfile;
    @FXML
    private Label footerLabel;

    private ResourceBundle bundle;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.bundle = rb;
        refreshChromeTheme();
        restoreView();
    }

    public void refreshChromeTheme() {
        if (rootPane == null) return;
        boolean light = MainApp.isLightTheme();
        if (light) {
            if (!rootPane.getStyleClass().contains("theme-light")) {
                rootPane.getStyleClass().add("theme-light");
            }
        } else {
            rootPane.getStyleClass().remove("theme-light");
        }
    }

    private void restoreView() {
        View v = MainApp.getCurrentView();
        switch (v) {
            case REGISTER:
                showRegister();
                break;
            case ACTIVITIES:
                showActivities();
                break;
            case ACCUMULATED:
                showAccumulated();
                break;
            case PROFILE:
                showProfile();
                break;
            case HISTORY:
                showHistory();
                break;
            case LOGIN:
            default:
                showLogin();
                break;
        }
    }

    public void showLogin() {
        MainApp.setCurrentView(View.LOGIN);
        setSessionMenusEnabled(false);
        setChromeVisible(false);
        updateFooter();
        LoginController c = loadCenter("/runninglasafor/views/Login.fxml");
        if (c != null) c.setRoot(this);
    }

    public void showRegister() {
        MainApp.setCurrentView(View.REGISTER);
        setSessionMenusEnabled(false);
        setChromeVisible(false);
        updateFooter();
        RegisterController c = loadCenter("/runninglasafor/views/Register.fxml");
        if (c != null) c.setRoot(this);
    }

    public void showHome() {
        showActivities();
    }

    public void showActivities() {
        MainApp.setCurrentView(View.ACTIVITIES);
        setSessionMenusEnabled(true);
        setChromeVisible(true);
        updateFooter();
        ActivitiesListController c = loadCenter("/runninglasafor/views/ActivitiesList.fxml");
        if (c != null) c.setRoot(this);
    }

    public void showAccumulated() {
        MainApp.setCurrentView(View.ACCUMULATED);
        setSessionMenusEnabled(true);
        setChromeVisible(true);
        updateFooter();
        AccumulatedController c = loadCenter("/runninglasafor/views/Accumulated.fxml");
        if (c != null) c.setRoot(this);
    }
    
    public void showActivityDetail(upv.ipc.sportlib.Activity activity) {
        MainApp.setCurrentView(MainApp.View.ACTIVITIES); 
        setSessionMenusEnabled(true);
        setChromeVisible(true);
        updateFooter();
        
        ActivityDetailController c = loadCenter("/runninglasafor/views/ActivityDetail.fxml");
        if (c != null) {
            c.setRoot(this);
            c.setActivity(activity); 
        }
    }

    public void showProfile() {
        MainApp.setCurrentView(View.PROFILE);
        setSessionMenusEnabled(true);
        setChromeVisible(true);
        updateFooter();
        ProfileController c = loadCenter("/runninglasafor/views/Profile.fxml");
        if (c != null) c.setRoot(this);
    }

    public void showHistory() {
        MainApp.setCurrentView(View.HISTORY);
        setSessionMenusEnabled(true);
        setChromeVisible(true);
        updateFooter();
        SessionHistoryController c = loadCenter("/runninglasafor/views/SessionHistory.fxml");
        if (c != null) c.setRoot(this);
    }

    @FXML
    private void onImportGpx(ActionEvent event) {
        if (importGpx()) {
            showActivities();
        }
    }

    @FXML
    private void onShowActivities(ActionEvent event) {
        showActivities();
    }

    @FXML
    private void onShowAccumulated(ActionEvent event) {
        showAccumulated();
    }

    @FXML
    private void onShowProfile(ActionEvent event) {
        showProfile();
    }

    @FXML
    private void onShowHistory(ActionEvent event) {
        showHistory();
    }

    @FXML
    private void onLogout(ActionEvent event) {
        SportActivityApp.getInstance().logout();
        showLogin();
    }

    @FXML
    private void onExit(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                bundle.getString("dialog.exit.content"),
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText(bundle.getString("dialog.exit.title"));
        Optional<ButtonType> r = confirm.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.OK) {
            SportActivityApp.getInstance().logout();
            Platform.exit();
        }
    }

    @FXML
    private void onAbout(ActionEvent event) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(bundle.getString("about.title"));
        a.setHeaderText(bundle.getString("about.header"));
        a.setContentText(bundle.getString("about.content"));
        a.showAndWait();
    }

    @FXML
    private void onIdiomaEs(ActionEvent event) {
        MainApp.changeLocale(new Locale("es"));
    }

    @FXML
    private void onIdiomaEn(ActionEvent event) {
        MainApp.changeLocale(new Locale("en"));
    }

    @FXML
    private void onIdiomaFr(ActionEvent event) {
        MainApp.changeLocale(new Locale("fr"));
    }

    @FXML
    private void onIdiomaDe(ActionEvent event) {
        MainApp.changeLocale(new Locale("de"));
    }

    @FXML
    private void onIdiomaZh(ActionEvent event) {
        MainApp.changeLocale(new Locale("zh"));
    }

    public boolean importGpx() {
        FileChooser fc = new FileChooser();
        fc.setTitle(bundle.getString("import.title"));
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(bundle.getString("import.filter"), "*.gpx"));
        Stage stage = (Stage) rootPane.getScene().getWindow();
        File file = fc.showOpenDialog(stage);
        if (file == null) {
            return false;
        }
        try {
            Activity imported = SportActivityApp.getInstance().importActivity(file);
            if (imported == null) {
                showError(bundle.getString("error.importGpx"));
                return false;
            }
            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setHeaderText(bundle.getString("import.ok.header"));
            ok.setContentText(MessageFormat.format(
                    bundle.getString("import.ok.content"), imported.getName()));
            ok.showAndWait();
            return true;
        } catch (RuntimeException ex) {
            showError(bundle.getString("error.importException") + ": " + ex.getMessage());
            return false;
        }
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    private void placeholderCenter(String text) {
        Label placeholder = new Label(text);
        placeholder.getStyleClass().add("auth-subtitle");
        placeholder.setStyle("-fx-font-size: 16px;");
        StackPane wrapper = new StackPane(placeholder);
        wrapper.getStyleClass().add("auth-root");
        rootPane.setCenter(wrapper);
    }

    private void setSessionMenusEnabled(boolean enabled) {
        if (miImport != null) miImport.setDisable(!enabled);
        if (miLogout != null) miLogout.setDisable(!enabled);
        if (menuActivities != null) menuActivities.setDisable(!enabled);
        if (menuProfile != null) menuProfile.setDisable(!enabled);
    }

    private void setChromeVisible(boolean visible) {
        if (menuBar != null) {
            menuBar.setVisible(visible);
            menuBar.setManaged(visible);
        }
        if (footerLabel != null && footerLabel.getParent() != null) {
            footerLabel.getParent().setVisible(visible);
            footerLabel.getParent().setManaged(visible);
        }
        if (visible) {
            refreshChromeTheme();
        }
    }

    private void updateFooter() {
        if (footerLabel == null) return;
        User u;
        try {
            u = SportActivityApp.getInstance().getCurrentUser();
        } catch (Throwable t) {
            footerLabel.setText(bundle.getString("app.footer"));
            return;
        }
        if (u == null) {
            footerLabel.setText(bundle.getString("app.footer"));
        } else {
            footerLabel.setText(bundle.getString("app.footer") + "  -  "
                    + bundle.getString("app.session") + ": " + u.getNickName());
        }
    }

    private <T> T loadCenter(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath), bundle);
            Parent view = loader.load();
            rootPane.setCenter(view);
            return loader.getController();
        } catch (IOException ex) {
            showError(bundle.getString("error.viewLoad") + ": " + fxmlPath);
            return null;
        }
    }

    private void showError(String message) {
        Alert a = new Alert(Alert.AlertType.ERROR, message);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
