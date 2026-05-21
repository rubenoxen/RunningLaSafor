/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package runninglasafor.controllers;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import runninglasafor.MainApp;
import upv.ipc.sportlib.Session;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

public class SessionHistoryController implements Initializable {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private HBox authRoot;
    @FXML private TableView<Session> sessionTable;
    @FXML private TableColumn<Session, String> colStartTime;
    @FXML private TableColumn<Session, String> colEndTime;
    @FXML private TableColumn<Session, String> colDuration;
    @FXML private TableColumn<Session, Number> colImported;
    @FXML private TableColumn<Session, Number> colViewed;
    @FXML private TableColumn<Session, Number> colAnnotations;

    @FXML private Label lblTotalSessions;
    @FXML private Label lblTotalDuration;
    @FXML private Label lblTotalImported;
    @FXML private Label lblTotalViewed;
    @FXML private Label lblTotalAnnotations;
    @FXML private Label statusLabel;

    @FXML private ComboBox<String> languageBox;
    @FXML private Region themeIcon;
    @FXML private ImageView bgImage;

    private RootLayoutController root;
    private ResourceBundle bundle;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.bundle = rb;
        setupTableColumns();
        setupLanguageBox();
        applyTheme();
        refresh();
    }

    private void setupTableColumns() {
        colStartTime.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        formatDateTime(cellData.getValue().getStartTime())));
        colEndTime.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        formatDateTime(cellData.getValue().getEndTime())));
        colDuration.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        formatDuration(cellData.getValue().getDuration())));
        colImported.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(
                        cellData.getValue().getImportedActivities()));
        colViewed.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(
                        cellData.getValue().getViewedActivities()));
        colAnnotations.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(
                        cellData.getValue().getAnnotationsCreated()));
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
            String path = light
                    ? "/resources/running_bg_light.png"
                    : "/resources/running_bg.png";
            bgImage.setImage(new Image(getClass().getResource(path).toExternalForm()));
            bgImage.setBlendMode(light ? BlendMode.SRC_OVER : BlendMode.MULTIPLY);
            bgImage.setOpacity(light ? 0.9 : 0.65);
        }
    }

    @FXML
    private void onToggleTheme(ActionEvent event) {
        MainApp.toggleTheme();
        applyTheme();
        if (root != null) root.refreshChromeTheme();
    }

    public void setRoot(RootLayoutController root) {
        this.root = root;
    }

    private void refresh() {
        User user = SportActivityApp.getInstance().getCurrentUser();
        if (user == null) {
            if (statusLabel != null)
                statusLabel.setText(bundle.getString("history.noSession"));
            return;
        }

        List<Session> sessions =
                SportActivityApp.getInstance().getSessionsByUser(user);
        ObservableList<Session> items = FXCollections.observableArrayList(
                sessions == null ? List.of() : sessions);
        sessionTable.setItems(items);

        /* --- Totales acumulados --- */
        long totalSeconds = 0;
        int totalImported = 0;
        int totalViewed = 0;
        int totalAnnotations = 0;

        for (Session s : items) {
            Duration d = s.getDuration();
            if (d != null) totalSeconds += d.getSeconds();
            totalImported  += s.getImportedActivities();
            totalViewed    += s.getViewedActivities();
            totalAnnotations += s.getAnnotationsCreated();
        }

        lblTotalSessions.setText(String.valueOf(items.size()));
        lblTotalDuration.setText(formatHoursMinutes(totalSeconds));
        lblTotalImported.setText(String.valueOf(totalImported));
        lblTotalViewed.setText(String.valueOf(totalViewed));
        lblTotalAnnotations.setText(String.valueOf(totalAnnotations));

        if (statusLabel != null) {
            if (items.isEmpty()) {
                statusLabel.setText(bundle.getString("history.empty"));
            } else {
                statusLabel.setText(java.text.MessageFormat.format(
                        bundle.getString("history.count"), items.size()));
            }
        }
    }

    // ─── Formateo ────────────────────────────────────────────

    private static String formatDateTime(LocalDateTime dt) {
        return dt == null ? "-" : DATE_FMT.format(dt);
    }

    private static String formatDuration(Duration d) {
        if (d == null) return "-";
        long total = d.getSeconds();
        long h = total / 3600;
        long m = (total % 3600) / 60;
        long s = total % 60;
        return h > 0
                ? String.format("%dh %02dmin %02ds", h, m, s)
                : String.format("%dmin %02ds", m, s);
    }

    private static String formatHoursMinutes(long totalSeconds) {
        if (totalSeconds <= 0) return "0h 00min";
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        return String.format("%dh %02dmin", h, m);
    }
}