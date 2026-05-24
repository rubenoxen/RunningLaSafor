package runninglasafor.controllers;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import upv.ipc.sportlib.Session;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

public class SessionHistoryController implements Initializable {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private VBox authRoot;
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

    private RootLayoutController root;
    private ResourceBundle bundle;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.bundle = rb;
        setupTableColumns();
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
