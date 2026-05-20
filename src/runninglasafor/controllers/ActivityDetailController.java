/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package runninglasafor.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.MapRegion;

/**
 * FXML Controller class
 *
 * @author rubenpuigmur
 */
public class ActivityDetailController implements Initializable {

    @FXML
    private ComboBox<MapRegion> mapSelector;
    @FXML
    private Button addMapButton;
    @FXML
    private Button zoomOutButton;
    @FXML
    private Slider zoomSlider;
    @FXML
    private Button zoomInButton;
    @FXML
    private Button backButton;
    @FXML
    private Label activityNameLabel;
    @FXML
    private ScrollPane mapScrollPane;
    @FXML
    private Group mapContentGroup;
    @FXML
    private Group mapZoomGroup;
    @FXML
    private Pane mapPane;
    @FXML
    private HBox statsBox;
    @FXML
    private Label distanceLabel;
    @FXML
    private Label durationLabel;
    @FXML
    private Label speedLabel;
    @FXML
    private Label elevationLabel;
    @FXML
    private Label paceLabel;
    @FXML
    private ElevationProfileController elevationProfileController;
    
    
    private Activity currentActivity;
    private RootLayoutController root;
    private MapViewController mapViewController;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mapViewController = new MapViewController();
        mapViewController.setMapPane(mapPane);
        mapViewController.setMapZoomGroup(mapZoomGroup);
        mapViewController.setMapScrollPane(mapScrollPane);
        mapViewController.setZoomSlider(zoomSlider);
        mapViewController.setMapSelector(mapSelector);
        
        mapViewController.setupZoom();
        mapViewController.setupMapSelector();
    }    

    @FXML
    private void onAddMap(ActionEvent event) {
        AddMapController.showDialog(addMapButton.getScene().getWindow());
        
        mapViewController.setupMapSelector();
    }

    @FXML
    private void onZoomOut(ActionEvent event) {
        mapViewController.zoomOut();
    }

    @FXML
    private void onZoomIn(ActionEvent event) {
        mapViewController.zoomIn();
    }

    @FXML
    private void onBack(ActionEvent event) {
        if (root != null) {
            root.showActivities();
        }
    }
    
    
    public void setActivity(Activity activity) {
        this.currentActivity = activity;
        if (activity == null) return;
        
        activityNameLabel.setText(activity.getName());
        
        updateStats();
        
        mapViewController.loadActivity(activity);
        
        elevationProfileController.loadProfileData(activity);
    }
    
    public void setRoot(RootLayoutController root) {
        this.root = root;
    }
    
    private void updateStats() {
        if (currentActivity == null) return;

        distanceLabel.setText(String.format("%.2f km",
            currentActivity.getTotalDistance() / 1000.0));

        durationLabel.setText(formatDuration(
            currentActivity.getDuration()));

        speedLabel.setText(String.format("%.1f km/h",
            currentActivity.getAverageSpeed()));

        elevationLabel.setText(String.format("+%.0f / -%.0f m",
            currentActivity.getElevationGain(),
            currentActivity.getElevationLoss()));

        paceLabel.setText(String.format("%.2f min/km",
            currentActivity.getAveragePace()));
    }
    
    private String formatDuration(javafx.util.Duration d) {
        if (d == null) return "--";
        long totalSecs = (long) d.toSeconds();
        long h = totalSecs / 3600;
        long m = (totalSecs % 3600) / 60;
        long s = totalSecs % 60;
        if (h > 0) {
            return String.format("%dh %02dm %02ds", h, m, s);
        }
        return String.format("%dm %02ds", m, s);
    }
}
