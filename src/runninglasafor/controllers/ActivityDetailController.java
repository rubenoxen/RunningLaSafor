package runninglasafor.controllers;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.MapRegion;
import upv.ipc.sportlib.SportActivityApp;

public class ActivityDetailController implements Initializable {
    
    @FXML private MapViewController mapViewController;
    @FXML private ElevationProfileController elevationProfileController;
    
    @FXML private Label activityNameLabel;
    @FXML private Label distanceLabel;
    @FXML private Label durationLabel;
    @FXML private Label speedLabel;
    @FXML private Label elevationLabel;
    @FXML private Label paceLabel;
    @FXML private ComboBox<MapRegion> mapSelector;
    @FXML private Button addMapButton;
    @FXML private Button backButton;  
    
    @FXML private Label minElevLabel;
    @FXML private Label maxElevLabel;

    private Activity currentActivity;
    private RootLayoutController root;  
    
    @FXML
    private HBox statsBox;

    @Override
    public void initialize(URL url, ResourceBundle rb) { 
    }

    public void setActivity(Activity activity) {
        elevationProfileController.setMapViewController(mapViewController);
        this.currentActivity = activity;
        if (activity == null) return;
        
        activityNameLabel.setText(activity.getName());
        updateStats();
               
        mapViewController.loadActivity(activity);
        elevationProfileController.loadProfileData(activity);
        
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
            if (newVal != null && currentActivity != null) {
                mapViewController.loadActivityWithMap(currentActivity, newVal);
            }
        });
    }

    private void updateStats() {
        if (currentActivity == null) return;
        distanceLabel.setText(String.format("%.2f km", currentActivity.getTotalDistance() / 1000.0));
        durationLabel.setText(formatDuration(currentActivity.getDuration()));
        speedLabel.setText(String.format("%.1f km/h", currentActivity.getAverageSpeed()));
        elevationLabel.setText(String.format("+%.0f / -%.0f m", currentActivity.getElevationGain(), currentActivity.getElevationLoss()));
        paceLabel.setText(String.format("%.2f min/km", currentActivity.getAveragePace()));
        minElevLabel.setText(String.format("%.0f m", currentActivity.getMinElevation()));
        maxElevLabel.setText(String.format("%.0f m", currentActivity.getMaxElevation()));
    }

    private String formatDuration(java.time.Duration d) {
        if (d == null) return "--";
        long totalSecs = d.getSeconds();
        return String.format("%dh %02dm", totalSecs / 3600, (totalSecs % 3600) / 60);
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
}