package runninglasafor.controllers;

import java.net.URL;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import runninglasafor.MainApp;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.SportActivityApp;

public class AccumulatedController implements Initializable {

    private enum Period { ALL, MONTH, YEAR }

    @FXML
    private HBox authRoot;
    @FXML
    private ComboBox<Period> periodCombo;
    @FXML
    private Label lblCount;
    @FXML
    private Label lblDistance;
    @FXML
    private Label lblTime;
    @FXML
    private Label lblGain;
    @FXML
    private Label lblLoss;
    @FXML
    private Label lblSpeed;
    @FXML
    private Label statusLabel;
    @FXML
    private ComboBox<String> languageBox;
    @FXML
    private Region themeIcon;
    @FXML
    private ImageView bgImage;

    private RootLayoutController root;
    private ResourceBundle bundle;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.bundle = rb;

        ObservableList<Period> values = FXCollections.observableArrayList(
                Period.ALL, Period.MONTH, Period.YEAR);
        periodCombo.setItems(values);
        periodCombo.setConverter(new javafx.util.StringConverter<Period>() {
            @Override
            public String toString(Period p) {
                if (p == null) return "";
                switch (p) {
                    case MONTH: return bundle.getString("acc.period.month");
                    case YEAR: return bundle.getString("acc.period.year");
                    case ALL:
                    default: return bundle.getString("acc.period.all");
                }
            }

            @Override
            public Period fromString(String s) {
                return Period.ALL;
            }
        });
        periodCombo.getSelectionModel().select(Period.ALL);
        periodCombo.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldV, newV) -> refresh());

        setupLanguageBox();
        applyTheme();
        refresh();
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

    private void refresh() {
        List<Activity> all = SportActivityApp.getInstance().getUserActivities();
        if (all == null) all = List.of();
        List<Activity> filtered = filterByPeriod(all,
                periodCombo.getSelectionModel().getSelectedItem());

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
        lblTime.setText(formatDuration(durSeconds));
        lblGain.setText(String.format(Locale.ROOT, "%.0f m", gain));
        lblLoss.setText(String.format(Locale.ROOT, "%.0f m", loss));
        lblSpeed.setText(String.format(Locale.ROOT, "%.2f km/h", avgSpeed));

        if (statusLabel != null) {
            if (count == 0) {
                statusLabel.setText(bundle.getString("acc.empty"));
            } else {
                statusLabel.setText(MessageFormat.format(
                        bundle.getString("acc.count"), count));
            }
        }
    }

    private static List<Activity> filterByPeriod(List<Activity> all, Period period) {
        if (period == null || period == Period.ALL) {
            return all;
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

    private static String formatDuration(long totalSeconds) {
        if (totalSeconds <= 0) {
            return "0h 00min";
        }
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        return String.format("%dh %02dmin", h, m);
    }
}
