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
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import runninglasafor.MainApp;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.SportActivityApp;

public class ActivitiesListController implements Initializable {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private enum Period { ALL, MONTH, YEAR }

    @FXML
    private HBox authRoot;
    @FXML
    private ListView<Activity> activitiesList;
    @FXML
    private MenuButton optionsButton;
    @FXML
    private Label statusLabel;
    @FXML
    private ComboBox<String> languageBox;
    @FXML
    private Region themeIcon;
    @FXML
    private ImageView bgImage;

    @FXML
    private ComboBox<Period> periodCombo;
    @FXML
    private Label lblCount;
    @FXML
    private Label lblDistance;
    @FXML
    private Label lblTime;
    @FXML
    private Label lblSpeed;
    @FXML
    private Label lblGain;
    @FXML
    private Label lblLoss;

    private final ObservableList<Activity> items = FXCollections.observableArrayList();
    private RootLayoutController root;
    private ResourceBundle bundle;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.bundle = rb;
        activitiesList.setItems(items);
        activitiesList.setCellFactory(lv -> new ActivityCell(bundle));

        ChangeListener<Activity> selListener = (obs, oldV, newV) -> {
            optionsButton.setDisable(newV == null);
        };
        activitiesList.getSelectionModel().selectedItemProperty().addListener(selListener);

        setupPeriodCombo();
        setupLanguageBox();
        applyTheme();
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
        if (root != null) {
            root.refreshChromeTheme();
        }
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
        double loss = 0.0;
        for (Activity a : filtered) {
            distMeters += a.getTotalDistance();
            Duration d = a.getDuration();
            if (d != null) durSeconds += d.getSeconds();
            gain += a.getElevationGain();
            loss += a.getElevationLoss();
        }
        double avgSpeed = durSeconds > 0
                ? (distMeters / 1000.0) / (durSeconds / 3600.0)
                : 0.0;

        lblCount.setText(String.valueOf(count));
        lblDistance.setText(String.format(Locale.ROOT, "%.2f km", distMeters / 1000.0));
        lblTime.setText(formatHoursMinutes(durSeconds));
        lblSpeed.setText(String.format(Locale.ROOT, "%.2f km/h", avgSpeed));
        lblGain.setText(String.format(Locale.ROOT, "%.0f m", gain));
        lblLoss.setText(String.format(Locale.ROOT, "%.0f m", loss));
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

    private static String formatHoursMinutes(long totalSeconds) {
        if (totalSeconds <= 0) {
            return "0h 00min";
        }
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        return String.format("%dh %02dmin", h, m);
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
        private final HBox container;

        ActivityCell(ResourceBundle bundle) {
            this.bundle = bundle;
            title.getStyleClass().add("activity-cell-title");
            subtitle.getStyleClass().add("activity-cell-subtitle");
            distance.getStyleClass().add("activity-cell-stat");
            pace.getStyleClass().add("activity-cell-substat");

            VBox left = new VBox(2.0, title, subtitle);
            VBox right = new VBox(2.0, distance, pace);
            right.setAlignment(Pos.CENTER_RIGHT);
            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            container = new HBox(10.0, left, spacer, right);
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
            title.setText(a.getName() == null
                    ? bundle.getString("activities.unnamed")
                    : a.getName());
            subtitle.setText(formatDate(a.getStartTime()) + "   "
                    + formatDuration(a.getDuration()));
            distance.setText(String.format(Locale.ROOT,
                    "%.2f km", a.getTotalDistance() / 1000.0));
            pace.setText(String.format(Locale.ROOT,
                    "%.1f km/h", a.getAverageSpeed()));
            setGraphic(container);
            setText(null);
        }
    }
}
