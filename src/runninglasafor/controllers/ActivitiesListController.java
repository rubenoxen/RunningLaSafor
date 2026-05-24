package runninglasafor.controllers;

import java.net.URL;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import runninglasafor.MainApp;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.SportActivityApp;

public class ActivitiesListController implements Initializable {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private enum Period { ALL, MONTH, YEAR }

    @FXML private VBox authRoot;
    @FXML private ListView<Activity> activitiesList;
    @FXML private MenuButton optionsButton;
    @FXML private Label statusLabel;

    @FXML private ComboBox<Period> periodCombo;
    @FXML private Label lblCount;
    @FXML private Label lblDistance;
    @FXML private Label lblTime;
    @FXML private Label lblGain;

    private final ObservableList<Activity> items = FXCollections.observableArrayList();
    private RootLayoutController root;
    private ResourceBundle bundle;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.bundle = rb;
        activitiesList.setItems(items);
        activitiesList.setCellFactory(lv -> new ActivityCell(bundle));

        activitiesList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Activity a = activitiesList.getSelectionModel().getSelectedItem();
                if (a != null && root != null) {
                    root.showActivityDetail(a);
                }
            }
        });

        ChangeListener<Activity> selListener = (obs, oldV, newV) -> {
            optionsButton.setDisable(newV == null);
        };
        activitiesList.getSelectionModel().selectedItemProperty().addListener(selListener);

        setupPeriodCombo();
        refresh();
    }

    private void setupPeriodCombo() {
        if (periodCombo == null) return;
        periodCombo.setItems(FXCollections.observableArrayList(
                Period.ALL, Period.MONTH, Period.YEAR));
        periodCombo.setConverter(new StringConverter<Period>() {
            @Override
            public String toString(Period p) {
                if (p == null) return "";
                switch (p) {
                    case MONTH: return bundle.getString("acc.period.month");
                    case YEAR:  return bundle.getString("acc.period.year");
                    case ALL:
                    default:    return bundle.getString("acc.period.all");
                }
            }
            @Override
            public Period fromString(String s) {
                return Period.ALL;
            }
        });
        periodCombo.getSelectionModel().select(Period.ALL);
        periodCombo.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldV, newV) -> updateStats());
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

        if (root != null) {
            root.showActivityDetail(a);
        }
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
        updateStats();
    }

    private void updateStats() {
        if (lblCount == null) return;
        Period period = periodCombo != null
                ? periodCombo.getSelectionModel().getSelectedItem()
                : Period.ALL;
        List<Activity> filtered = filterByPeriod(items, period);

        int count = filtered.size();
        double distMeters = 0.0;
        long durSeconds = 0L;
        double gain = 0.0;
        for (Activity a : filtered) {
            distMeters += a.getTotalDistance();
            Duration d = a.getDuration();
            if (d != null) durSeconds += d.getSeconds();
            gain += a.getElevationGain();
        }

        lblCount.setText(String.valueOf(count));
        lblDistance.setText(String.format(Locale.ROOT, "%.1f km", distMeters / 1000.0));
        lblTime.setText(formatHoursMinutes(durSeconds));
        lblGain.setText(String.format(Locale.ROOT, "%.0f m", gain));
    }

    private static List<Activity> filterByPeriod(List<? extends Activity> all, Period period) {
        if (period == null || period == Period.ALL) {
            return all.stream().collect(Collectors.toList());
        }
        LocalDate today = LocalDate.now();
        LocalDate from = period == Period.MONTH
                ? today.withDayOfMonth(1)
                : today.withDayOfYear(1);
        LocalDateTime fromDt = from.atStartOfDay();
        return all.stream()
                .filter(a -> a.getStartTime() != null && !a.getStartTime().isBefore(fromDt))
                .collect(Collectors.toList());
    }

    private String displayName(Activity a) {
        return a.getName() == null
                ? bundle.getString("activities.unnamed")
                : a.getName();
    }

    private void setStatus(String text, boolean error) {
        if (statusLabel == null) return;
        statusLabel.setText(text);
        statusLabel.getStyleClass().removeAll("status-label", "error-label");
        statusLabel.getStyleClass().add(error ? "error-label" : "status-label");
    }

    private static String formatDate(LocalDateTime dt) {
        return dt == null ? "-" : DATE_FMT.format(dt);
    }

    private static String formatDuration(Duration d) {
        if (d == null) return "-";
        long total = d.getSeconds();
        long h = total / 3600;
        long m = (total % 3600) / 60;
        if (h > 0) {
            return String.format("%dh %02dmin", h, m);
        }
        return String.format("%dmin", m);
    }

    private static String formatHoursMinutes(long totalSeconds) {
        if (totalSeconds <= 0) {
            return "0h 00min";
        }
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        return String.format("%dh %02dmin", h, m);
    }

    private static final class ActivityCell extends ListCell<Activity> {
        private final ResourceBundle bundle;
        private final Label name = new Label();
        private final Label date = new Label();
        private final Label dist = new Label();
        private final Label dur = new Label();
        private final Label gain = new Label();
        private final Label type = new Label();
        private final HBox container;

        ActivityCell(ResourceBundle bundle) {
            this.bundle = bundle;
            name.getStyleClass().add("activity-cell-title");
            date.getStyleClass().add("activity-cell-subtitle");
            dist.getStyleClass().add("activity-cell-stat");
            dur.getStyleClass().add("activity-cell-substat");
            gain.getStyleClass().add("activity-cell-substat");
            type.getStyleClass().add("activity-cell-substat");

            name.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(name, javafx.scene.layout.Priority.ALWAYS);
            date.setMinWidth(100);
            dist.setMinWidth(90);
            dur.setMinWidth(80);
            gain.setMinWidth(80);
            type.setMinWidth(60);

            container = new HBox(0.0, name, date, dist, dur, gain, type);
            container.setAlignment(Pos.CENTER_LEFT);
        }

        @Override
        protected void updateItem(Activity a, boolean empty) {
            super.updateItem(a, empty);
            if (empty || a == null) {
                setGraphic(null);
                setText(null);
                return;
            }
            name.setText(a.getName() == null
                    ? bundle.getString("activities.unnamed")
                    : a.getName());
            date.setText(formatDate(a.getStartTime()));
            dist.setText(String.format(Locale.ROOT, "%.2f km", a.getTotalDistance() / 1000.0));
            dur.setText(formatDuration(a.getDuration()));
            gain.setText(String.format(Locale.ROOT, "%.0f m", a.getElevationGain()));
            type.setText(bundle.getString("activities.type.running"));
            setGraphic(container);
            setText(null);
        }
    }
}
