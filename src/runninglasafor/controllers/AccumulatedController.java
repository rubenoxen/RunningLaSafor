package runninglasafor.controllers;

import java.net.URL;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

public class AccumulatedController implements Initializable {

    private enum Period { ALL, MONTH, YEAR }

    private static final String[] MONTHS_ES =
            { "Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic" };

    @FXML private VBox authRoot;
    @FXML private TabPane periodTabs;
    @FXML private Tab tabAll;
    @FXML private Tab tabMonth;
    @FXML private Tab tabYear;

    @FXML private Label lblCount;
    @FXML private Label lblDistance;
    @FXML private Label lblTime;
    @FXML private Label lblGain;
    @FXML private Label lblLoss;
    @FXML private Label lblSpeed;
    @FXML private Label statusLabel;

    @FXML private BarChart<String, Number> distanceChart;
    @FXML private CategoryAxis distanceXAxis;
    @FXML private NumberAxis distanceYAxis;

    private RootLayoutController root;
    private ResourceBundle bundle;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.bundle = rb;
        if (periodTabs != null) {
            periodTabs.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldV, newV) -> refresh());
        }
        refresh();
    }

    public void setRoot(RootLayoutController root) {
        this.root = root;
    }

    private Period currentPeriod() {
        if (periodTabs == null) return Period.ALL;
        Tab sel = periodTabs.getSelectionModel().getSelectedItem();
        if (sel == tabMonth) return Period.MONTH;
        if (sel == tabYear) return Period.YEAR;
        return Period.ALL;
    }

    private void refresh() {
        User user = SportActivityApp.getInstance().getCurrentUser();
        List<Activity> all = user == null ? List.of() : user.getActivities();
        if (all == null) all = List.of();
        List<Activity> filtered = filterByPeriod(all, currentPeriod());

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

        updateDistanceChart(filtered);
    }

    private void updateDistanceChart(List<Activity> activities) {
        if (distanceChart == null) return;
        distanceChart.getData().clear();

        Map<Integer, Double> byMonth = new HashMap<>();
        for (Activity a : activities) {
            if (a.getStartTime() == null) continue;
            int m = a.getStartTime().getMonthValue();
            byMonth.merge(m, a.getTotalDistance() / 1000.0, Double::sum);
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 1; i <= 12; i++) {
            String label = MONTHS_ES[i - 1];
            double v = byMonth.getOrDefault(i, 0.0);
            series.getData().add(new XYChart.Data<>(label, v));
        }
        distanceChart.setData(FXCollections.singletonObservableList(series));
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
