package runninglasafor.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import upv.ipc.sportlib.Activity;

public class ActivityDetailController implements Initializable {

    // Referencia al controlador del mapa (que cargaremos del FXML)
    @FXML private MapViewController mapViewController;
    @FXML private ElevationProfileController elevationProfileController;
    
    @FXML private Label activityNameLabel;
    @FXML private Label distanceLabel;
    @FXML private Label durationLabel;
    @FXML private Label speedLabel;
    @FXML private Label elevationLabel;
    @FXML private Label paceLabel;

    private Activity currentActivity;
    private RootLayoutController root;

    @Override
    public void initialize(URL url, ResourceBundle rb) {        
    }

    public void setActivity(Activity activity) {
        this.currentActivity = activity;
        if (activity == null) return;
        
        activityNameLabel.setText(activity.getName());
        updateStats();
               
        mapViewController.loadActivity(activity);
        elevationProfileController.loadProfileData(activity);
    }

    private void updateStats() {
        if (currentActivity == null) return;
        distanceLabel.setText(String.format("%.2f km", currentActivity.getTotalDistance() / 1000.0));
        durationLabel.setText(formatDuration(currentActivity.getDuration()));
        speedLabel.setText(String.format("%.1f km/h", currentActivity.getAverageSpeed()));
        elevationLabel.setText(String.format("+%.0f / -%.0f m", currentActivity.getElevationGain(), currentActivity.getElevationLoss()));
        paceLabel.setText(String.format("%.2f min/km", currentActivity.getAveragePace()));
    }

    private String formatDuration(java.time.Duration d) {
        if (d == null) return "--";
        long totalSecs = d.getSeconds();
        return String.format("%dh %02dm", totalSecs / 3600, (totalSecs % 3600) / 60);
    }
    
    public void setRoot(RootLayoutController root) { this.root = root; }
}