package runninglasafor.controllers;

import java.net.URL;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.SportActivityApp;

public class ActivitiesListController implements Initializable {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private ListView<Activity> activitiesList;
    @FXML
    private Button viewButton;
    @FXML
    private Button renameButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Label statusLabel;

    private final ObservableList<Activity> items = FXCollections.observableArrayList();
    private RootLayoutController root;
    private ResourceBundle bundle;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.bundle = rb;
        activitiesList.setItems(items);
        activitiesList.setCellFactory(lv -> new ActivityCell(bundle));

        ChangeListener<Activity> selListener = (obs, oldV, newV) -> {
            boolean none = newV == null;
            viewButton.setDisable(none);
            renameButton.setDisable(none);
            deleteButton.setDisable(none);
        };
        activitiesList.getSelectionModel().selectedItemProperty().addListener(selListener);

        refresh();
    }

    public void setRoot(RootLayoutController root) {
        this.root = root;
    }

    @FXML
    private void onRefresh(ActionEvent event) {
        refresh();
    }

    @FXML
    private void onImport(ActionEvent event) {
        if (root != null && root.importGpx()) {
            refresh();
        }
    }

    @FXML
    private void onView(ActionEvent event) {
        Activity a = activitiesList.getSelectionModel().getSelectedItem();
        if (a == null) return;
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle(bundle.getString("activities.detail.title"));
        info.setHeaderText(displayName(a));
        info.setContentText(detalleTexto(a));
        info.showAndWait();
    }

    @FXML
    private void onRename(ActionEvent event) {
        Activity a = activitiesList.getSelectionModel().getSelectedItem();
        if (a == null) return;

        TextInputDialog dlg = new TextInputDialog(displayName(a));
        dlg.setTitle(bundle.getString("activities.rename.title"));
        dlg.setHeaderText(MessageFormat.format(
                bundle.getString("activities.rename.header"), displayName(a)));
        dlg.setContentText(bundle.getString("activities.rename.content"));
        Optional<String> res = dlg.showAndWait();
        if (!res.isPresent()) return;

        String nuevo = res.get().trim();
        if (nuevo.isEmpty()) {
            setStatus(bundle.getString("activities.rename.empty"), true);
            return;
        }
        if (nuevo.equals(a.getName())) {
            return;
        }

        boolean ok = SportActivityApp.getInstance().renameActivity(a, nuevo);
        if (ok) {
            setStatus(bundle.getString("activities.rename.ok"), false);
            refresh();
        } else {
            setStatus(bundle.getString("activities.rename.error"), true);
        }
    }

    @FXML
    private void onDelete(ActionEvent event) {
        Activity a = activitiesList.getSelectionModel().getSelectedItem();
        if (a == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                bundle.getString("activities.delete.content"),
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle(bundle.getString("activities.delete.title"));
        confirm.setHeaderText(MessageFormat.format(
                bundle.getString("activities.delete.header"), displayName(a)));
        Optional<ButtonType> res = confirm.showAndWait();
        if (!res.isPresent() || res.get() != ButtonType.OK) return;

        boolean ok = SportActivityApp.getInstance().removeActivity(a);
        if (ok) {
            setStatus(bundle.getString("activities.delete.ok"), false);
            refresh();
        } else {
            setStatus(bundle.getString("activities.delete.error"), true);
        }
    }

    private void refresh() {
        List<Activity> data = SportActivityApp.getInstance().getUserActivities();
        items.setAll(data == null ? List.of() : data);
        if (items.isEmpty()) {
            setStatus(bundle.getString("activities.empty"), false);
        } else {
            setStatus(MessageFormat.format(
                    bundle.getString("activities.count"), items.size()), false);
        }
        activitiesList.getSelectionModel().clearSelection();
    }

    private String displayName(Activity a) {
        return a.getName() == null
                ? bundle.getString("activities.unnamed")
                : a.getName();
    }

    private void setStatus(String text, boolean error) {
        if (statusLabel == null) return;
        statusLabel.setText(text);
        statusLabel.setStyle(error
                ? "-fx-text-fill: #c0392b;"
                : "-fx-text-fill: #777;");
    }

    private String detalleTexto(Activity a) {
        String dist = String.format(Locale.ROOT, "%.2f km", a.getTotalDistance() / 1000.0);
        String speed = String.format(Locale.ROOT, "%.2f km/h", a.getAverageSpeed());
        String gain = String.format(Locale.ROOT, "%.0f m", a.getElevationGain());
        String loss = String.format(Locale.ROOT, "%.0f m", a.getElevationLoss());
        return MessageFormat.format(
                bundle.getString("activities.detail.text"),
                formatDate(a.getStartTime()),
                formatDuration(a.getDuration()),
                dist,
                speed,
                formatPace(a.getAveragePace()),
                gain,
                loss);
    }

    private static String formatDate(LocalDateTime dt) {
        return dt == null ? "-" : DATE_FMT.format(dt);
    }

    private static String formatDuration(Duration d) {
        if (d == null) return "-";
        long total = d.getSeconds();
        long h = total / 3600;
        long m = (total % 3600) / 60;
        long s = total % 60;
        if (h > 0) {
            return String.format("%dh %02dmin %02ds", h, m, s);
        }
        return String.format("%dmin %02ds", m, s);
    }

    private static String formatPace(double minPerKm) {
        if (Double.isNaN(minPerKm) || Double.isInfinite(minPerKm) || minPerKm <= 0) {
            return "-";
        }
        int min = (int) Math.floor(minPerKm);
        int sec = (int) Math.round((minPerKm - min) * 60.0);
        if (sec == 60) { min++; sec = 0; }
        return String.format("%d:%02d", min, sec);
    }

    private static final class ActivityCell extends ListCell<Activity> {
        private final ResourceBundle bundle;
        private final Label title = new Label();
        private final Label subtitle = new Label();
        private final Label distance = new Label();
        private final Label pace = new Label();
        private final HBox root;

        ActivityCell(ResourceBundle bundle) {
            this.bundle = bundle;
            title.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
            subtitle.setStyle("-fx-text-fill: #777; -fx-font-size: 12;");
            distance.setStyle("-fx-font-size: 14;");
            pace.setStyle("-fx-text-fill: #777; -fx-font-size: 12;");

            VBox left = new VBox(2.0, title, subtitle);
            VBox right = new VBox(2.0, distance, pace);
            right.setAlignment(Pos.CENTER_RIGHT);
            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            root = new HBox(10.0, left, spacer, right);
            root.setAlignment(Pos.CENTER_LEFT);
        }

        @Override
        protected void updateItem(Activity a, boolean empty) {
            super.updateItem(a, empty);
            if (empty || a == null) {
                setGraphic(null);
                setText(null);
                return;
            }
            title.setText(a.getName() == null
                    ? bundle.getString("activities.unnamed")
                    : a.getName());
            subtitle.setText(formatDate(a.getStartTime()) + "   "
                    + formatDuration(a.getDuration()));
            distance.setText(String.format(Locale.ROOT,
                    "%.2f km", a.getTotalDistance() / 1000.0));
            pace.setText(String.format(Locale.ROOT,
                    "%.1f km/h", a.getAverageSpeed()));
            setGraphic(root);
            setText(null);
        }
    }
}
