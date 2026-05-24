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
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import runninglasafor.MainApp;
import runninglasafor.MainApp.View;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

public class RootLayoutController implements Initializable {

    @FXML private BorderPane rootPane;
    @FXML private Label footerLabel;
    @FXML private HBox footerBox;

    @FXML private javafx.scene.layout.VBox sidebar;
    @FXML private Button navHome;
    @FXML private Button navActivities;
    @FXML private Button navStats;
    @FXML private Button navSessions;
    @FXML private Button navMaps;
    @FXML private Button navProfile;
    @FXML private Button navLogout;
    @FXML private HBox sidebarUser;
    @FXML private Label sidebarUserName;
    @FXML private Label sidebarUserEmail;
    @FXML private ImageView sidebarAvatar;

    @FXML private Button footerThemeButton;
    @FXML private Region footerThemeIcon;
    @FXML private ComboBox<String> footerLangBox;

    private ResourceBundle bundle;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.bundle = rb;
        setupLanguageBox();
        applyTheme();
        restoreView();
    }

    private void setupLanguageBox() {
        if (footerLangBox == null) return;
        footerLangBox.getItems().setAll("ES", "EN", "FR", "DE", "ZH");
        String current = MainApp.getCurrentLocale().getLanguage().toUpperCase();
        footerLangBox.setValue(current);
        footerLangBox.setOnAction(e -> {
            String sel = footerLangBox.getValue();
            if (sel == null) return;
            MainApp.changeLocale(new Locale(sel.toLowerCase()));
        });
    }

    private void applyTheme() {
        if (footerThemeIcon != null) {
            boolean light = !MainApp.isLightTheme();
            footerThemeIcon.getStyleClass().removeAll("theme-moon", "theme-sun");
            footerThemeIcon.getStyleClass().add(light ? "theme-sun" : "theme-moon");
        }
    }

    public void refreshChromeTheme() {
        applyTheme();
    }

    @FXML
    private void onToggleTheme(ActionEvent event) {
        MainApp.toggleTheme();
        applyTheme();
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
            case MAPS:
                showMaps();
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
        updateSidebarUser();
        markActiveNav(null);
        LoginController c = loadCenter("/runninglasafor/views/Login.fxml");
        if (c != null) c.setRoot(this);
    }

    public void showRegister() {
        MainApp.setCurrentView(View.REGISTER);
        setSessionMenusEnabled(false);
        setChromeVisible(false);
        updateFooter();
        updateSidebarUser();
        markActiveNav(null);
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
        updateSidebarUser();
        markActiveNav(navActivities);
        ActivitiesListController c = loadCenter("/runninglasafor/views/ActivitiesList.fxml");
        if (c != null) c.setRoot(this);
    }

    public void showAccumulated() {
        MainApp.setCurrentView(View.ACCUMULATED);
        setSessionMenusEnabled(true);
        setChromeVisible(true);
        updateFooter();
        updateSidebarUser();
        markActiveNav(navStats);
        AccumulatedController c = loadCenter("/runninglasafor/views/Accumulated.fxml");
        if (c != null) c.setRoot(this);
    }

    public void showActivityDetail(upv.ipc.sportlib.Activity activity) {
        MainApp.setCurrentView(MainApp.View.ACTIVITIES);
        setSessionMenusEnabled(true);
        setChromeVisible(true);
        updateFooter();
        updateSidebarUser();
        markActiveNav(navActivities);

        Activity activityToShow = activity;
        if (activity != null) {
            try {
                activityToShow = SportActivityApp.getInstance().getActivityById(activity.getId());
            } catch (Exception ignored) {
                activityToShow = activity;
            }
        }

        ActivityDetailController c = loadCenter("/runninglasafor/views/ActivityDetail.fxml");
        if (c != null) {
            c.setRoot(this);
            c.setActivity(activityToShow);
        }
    }

    public void showProfile() {
        MainApp.setCurrentView(View.PROFILE);
        setSessionMenusEnabled(true);
        setChromeVisible(true);
        updateFooter();
        updateSidebarUser();
        markActiveNav(navProfile);
        ProfileController c = loadCenter("/runninglasafor/views/Profile.fxml");
        if (c != null) c.setRoot(this);
    }

    public void showHistory() {
        MainApp.setCurrentView(View.HISTORY);
        setSessionMenusEnabled(true);
        setChromeVisible(true);
        updateFooter();
        updateSidebarUser();
        markActiveNav(navSessions);
        SessionHistoryController c = loadCenter("/runninglasafor/views/SessionHistory.fxml");
        if (c != null) c.setRoot(this);
    }

    public void showMaps() {
        MainApp.setCurrentView(View.MAPS);
        setSessionMenusEnabled(true);
        setChromeVisible(true);
        updateFooter();
        updateSidebarUser();
        markActiveNav(navMaps);
        MapsController c = loadCenter("/runninglasafor/views/Maps.fxml");
        if (c != null) c.setRoot(this);
    }

    private void markActiveNav(Button active) {
        Button[] all = { navHome, navActivities, navStats, navSessions, navMaps, navProfile, navLogout };
        for (Button b : all) {
            if (b == null) continue;
            b.getStyleClass().remove("active");
        }
        if (active != null && !active.getStyleClass().contains("active")) {
            active.getStyleClass().add("active");
        }
    }

    @FXML
    private void onImportGpx(ActionEvent event) {
        Activity imported = importGpxActivity();
        if (imported != null) {
            showActivityDetail(imported);
        }
    }

    @FXML private void onShowActivities(ActionEvent event) { showActivities(); }
    @FXML private void onShowAccumulated(ActionEvent event) { showAccumulated(); }
    @FXML private void onShowProfile(ActionEvent event) { showProfile(); }
    @FXML private void onShowHistory(ActionEvent event) { showHistory(); }
    @FXML private void onShowMaps(ActionEvent event) { showMaps(); }

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

    @FXML private void onIdiomaEs(ActionEvent event) { MainApp.changeLocale(new Locale("es")); }
    @FXML private void onIdiomaEn(ActionEvent event) { MainApp.changeLocale(new Locale("en")); }
    @FXML private void onIdiomaFr(ActionEvent event) { MainApp.changeLocale(new Locale("fr")); }
    @FXML private void onIdiomaDe(ActionEvent event) { MainApp.changeLocale(new Locale("de")); }
    @FXML private void onIdiomaZh(ActionEvent event) { MainApp.changeLocale(new Locale("zh")); }

    public boolean importGpx() {
        return importGpxActivity() != null;
    }

    public Activity importGpxActivity() {
        FileChooser fc = new FileChooser();
        fc.setTitle(bundle.getString("import.title"));
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(bundle.getString("import.filter"), "*.gpx"));
        Stage stage = (Stage) rootPane.getScene().getWindow();
        File file = fc.showOpenDialog(stage);
        if (file == null) {
            return null;
        }
        try {
            Activity imported = SportActivityApp.getInstance().importActivity(file);
            if (imported == null) {
                showError(bundle.getString("error.importGpx"));
                return null;
            }
            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setHeaderText(bundle.getString("import.ok.header"));
            ok.setContentText(MessageFormat.format(
                    bundle.getString("import.ok.content"), imported.getName()));
            ok.showAndWait();
            return imported;
        } catch (RuntimeException ex) {
            showError(bundle.getString("error.importException") + ": " + ex.getMessage());
            return null;
        }
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    private void setSessionMenusEnabled(boolean enabled) {
        if (sidebar != null) {
            sidebar.setVisible(enabled);
            sidebar.setManaged(enabled);
        }
    }

    private void setChromeVisible(boolean visible) {
        if (footerBox != null) {
            footerBox.setVisible(visible);
            footerBox.setManaged(visible);
        }
    }

    private void updateFooter() {
        if (footerLabel == null) return;
        footerLabel.setText(bundle.getString("app.footer"));
    }

    private void updateSidebarUser() {
        if (sidebarUserName == null || sidebarUserEmail == null) return;
        User u;
        try {
            u = SportActivityApp.getInstance().getCurrentUser();
        } catch (Throwable t) {
            return;
        }
        if (u == null) {
            sidebarUserName.setText("");
            sidebarUserEmail.setText("");
            if (sidebarAvatar != null) sidebarAvatar.setImage(null);
        } else {
            sidebarUserName.setText(u.getNickName());
            sidebarUserEmail.setText(u.getEmail() == null ? "" : u.getEmail());
            if (sidebarAvatar != null) {
                Image av = u.getAvatar();
                sidebarAvatar.setImage(av);
            }
        }
    }

    public void refreshSessionChrome() {
        updateFooter();
        updateSidebarUser();
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
