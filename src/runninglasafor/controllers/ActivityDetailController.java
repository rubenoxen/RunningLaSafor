package runninglasafor.controllers;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.MapRegion;
import upv.ipc.sportlib.SportActivityApp;

public class ActivityDetailController implements Initializable {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy · HH:mm", new Locale("es"));

    @FXML private MapViewController mapViewController;
    @FXML private ElevationProfileController elevationProfileController;

    @FXML private Label activityNameLabel;
    @FXML private Label activityDateLabel;
    @FXML private Label distanceLabel;
    @FXML private Label durationLabel;
    @FXML private Label speedLabel;
    @FXML private Label elevationLabel;
    @FXML private Label paceLabel;
    @FXML private Label minElevLabel;
    @FXML private Label maxElevLabel;

    @FXML private Label summaryDistance;
    @FXML private Label summaryDuration;
    @FXML private Label summaryPace;

    @FXML private ComboBox<MapRegion> mapSelector;
    @FXML private Button addMapButton;
    @FXML private Button backButton;

    @FXML private VBox detailRoot;

    private Activity currentActivity;
    private RootLayoutController root;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    public void setActivity(Activity activity) {
        if (elevationProfileController != null && mapViewController != null) {
            elevationProfileController.setMapViewController(mapViewController);
        }
        this.currentActivity = activity;
        if (activity == null) return;

        activityNameLabel.setText(activity.getName() == null ? "" : activity.getName());
        if (activityDateLabel != null) {
            activityDateLabel.setText(activity.getStartTime() == null
                    ? "" : DATE_FMT.format(activity.getStartTime()));
        }
        updateStats();

        if (mapViewController != null) {
            mapViewController.loadActivity(activity);
        }
        if (elevationProfileController != null) {
            elevationProfileController.loadProfileData(activity);
        }

        populateMapSelector();
    }

    private void populateMapSelector() {
        if (mapSelector == null) return;

        List<MapRegion> regions = SportActivityApp.getInstance().getMapRegions();
        mapSelector.getItems().setAll(regions);

        MapRegion suggested = currentActivity.getSuggestedMap();
        if (suggested != null) {
            mapSelector.getSelectionModel().select(suggested);
        }

        mapSelector.setConverter(new javafx.util.StringConverter<MapRegion>() {
            @Override
            public String toString(MapRegion r) {
                return r == null ? "" : r.getName();
            }
            @Override
            public MapRegion fromString(String s) { return null; }
        });

        mapSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && currentActivity != null && mapViewController != null) {
                mapViewController.loadActivityWithMap(currentActivity, newVal);
            }
        });
    }

    private void updateStats() {
        if (currentActivity == null) return;
        String distTxt = String.format(Locale.ROOT, "%.2f km", currentActivity.getTotalDistance() / 1000.0);
        String durTxt = formatDuration(currentActivity.getDuration());
        String paceTxt = String.format(Locale.ROOT, "%.2f min/km", currentActivity.getAveragePace());
        String speedTxt = String.format(Locale.ROOT, "%.1f km/h", currentActivity.getAverageSpeed());

        distanceLabel.setText(distTxt);
        durationLabel.setText(durTxt);
        speedLabel.setText(speedTxt);
        elevationLabel.setText(String.format(Locale.ROOT, "+%.0f m", currentActivity.getElevationGain()));
        paceLabel.setText(paceTxt);
        minElevLabel.setText(String.format(Locale.ROOT, "%.0f m", currentActivity.getMinElevation()));
        maxElevLabel.setText(String.format(Locale.ROOT, "%.0f m", currentActivity.getMaxElevation()));

        if (summaryDistance != null) summaryDistance.setText(distTxt);
        if (summaryDuration != null) summaryDuration.setText(durTxt);
        if (summaryPace != null) summaryPace.setText(paceTxt);
    }

    private String formatDuration(java.time.Duration d) {
        if (d == null) return "--";
        long totalSecs = d.getSeconds();
        long h = totalSecs / 3600;
        long m = (totalSecs % 3600) / 60;
        long s = totalSecs % 60;
        if (h > 0) return String.format("%dh %02dmin %02ds", h, m, s);
        return String.format("%dmin %02ds", m, s);
    }

    public void setRoot(RootLayoutController root) { this.root = root; }

    @FXML
    private void onBack() {
        if (root != null) {
            root.showActivities();
        }
    }

    @FXML
    private void onAddMap() {
        Optional<MapRegion> result = AddMapController.showDialog(addMapButton.getScene().getWindow());
        result.ifPresent(newMap -> {
            mapSelector.getItems().add(newMap);
            mapSelector.getSelectionModel().select(newMap);
        });
    }

    @FXML
    private void onViewAnnotations() {
        if (root != null) {
            root.showMaps();
        }
    }
}
