package runninglasafor.controllers;

import java.net.URL;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import upv.ipc.sportlib.Session;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

public class HistoryController implements Initializable {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private TableView<Session> sessionsTable;
    @FXML
    private TableColumn<Session, String> colStart;
    @FXML
    private TableColumn<Session, String> colEnd;
    @FXML
    private TableColumn<Session, String> colDuration;
    @FXML
    private TableColumn<Session, Number> colImports;
    @FXML
    private TableColumn<Session, Number> colViews;
    @FXML
    private TableColumn<Session, Number> colAnnotations;
    @FXML
    private Label statusLabel;

    private RootLayoutController root;
    private ResourceBundle bundle;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.bundle = rb;
        setupColumns();
        loadSessions();
    }

    public void setRoot(RootLayoutController root) {
        this.root = root;
    }

    private void setupColumns() {
        colStart.setCellValueFactory(c ->
                new SimpleStringProperty(formatDate(c.getValue().getStartTime())));
        colEnd.setCellValueFactory(c ->
                new SimpleStringProperty(formatDate(c.getValue().getEndTime())));
        colDuration.setCellValueFactory(c ->
                new SimpleStringProperty(formatDuration(c.getValue().getDuration())));
        colImports.setCellValueFactory(c ->
                new SimpleIntegerProperty(c.getValue().getImportedActivities()));
        colViews.setCellValueFactory(c ->
                new SimpleIntegerProperty(c.getValue().getViewedActivities()));
        colAnnotations.setCellValueFactory(c ->
                new SimpleIntegerProperty(c.getValue().getAnnotationsCreated()));
    }

    @FXML
    private void onRefresh(ActionEvent event) {
        loadSessions();
    }

    private void loadSessions() {
        ObservableList<Session> data = FXCollections.observableArrayList();
        User u = SportActivityApp.getInstance().getCurrentUser();
        if (u != null) {
            List<Session> list = SportActivityApp.getInstance().getSessionsByUser(u);
            if (list != null) data.setAll(list);
        }
        sessionsTable.setItems(data);
        if (data.isEmpty()) {
            statusLabel.setText(bundle.getString("history.empty"));
        } else {
            statusLabel.setText(MessageFormat.format(
                    bundle.getString("history.count"), data.size()));
        }
    }

    private static String formatDate(LocalDateTime t) {
        return t == null ? "-" : FMT.format(t);
    }

    private static String formatDuration(Duration d) {
        if (d == null) return "-";
        long sec = d.getSeconds();
        long h = sec / 3600;
        long m = (sec % 3600) / 60;
        long s = sec % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }
}
