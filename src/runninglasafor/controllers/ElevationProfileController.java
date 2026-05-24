package runninglasafor.controllers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.GeoUtils;
import upv.ipc.sportlib.TrackPoint;

public class ElevationProfileController implements Initializable {

    @FXML private AreaChart<Number, Number> elevationChart;
    @FXML private NumberAxis yAxis;
    @FXML private NumberAxis xAxis;

    private MapViewController mapViewController;
    private double[] accumulatedDistances;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    public void loadProfileData(Activity activity) {
        elevationChart.getData().clear();

        if (activity == null || activity.getTrackPoints().isEmpty()) {
            return;
        }

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        List<TrackPoint> trackPoints = activity.getTrackPoints();

        double accumulatedDistance = 0.0;
        TrackPoint prev = trackPoints.get(0);

        accumulatedDistances = new double[trackPoints.size()];
        int index = 0;

        for (TrackPoint point : trackPoints) {
            accumulatedDistance += GeoUtils.distance(prev, point);
            double distKm = accumulatedDistance / 1000.0;
            double ele = point.getElevation();

            series.getData().add(new XYChart.Data<>(distKm, ele));

            prev = point;
            accumulatedDistances[index++] = distKm;
        }

        elevationChart.getData().add(series);

        setupHoverInteractivity(trackPoints);
    }

    public void setMapViewController(MapViewController mvc) {
        this.mapViewController = mvc;
    }

    private void setupHoverInteractivity(List<TrackPoint> points) {
        elevationChart.setOnMouseMoved((MouseEvent e) -> {
            if (points == null || points.isEmpty() || mapViewController == null || accumulatedDistances == null) return;

            double mouseXScene = e.getSceneX();
            double mouseXLocal = xAxis.sceneToLocal(mouseXScene, 0).getX();
            double distKmTarget = xAxis.getValueForDisplay(mouseXLocal).doubleValue();

            int closestIndex = 0;
            double minDiff = Double.MAX_VALUE;

            for (int i = 0; i < accumulatedDistances.length; i++) {
                double diff = Math.abs(accumulatedDistances[i] - distKmTarget);
                if (diff < minDiff) {
                    minDiff = diff;
                    closestIndex = i;
                }
            }

            mapViewController.highlightPoint(points.get(closestIndex));
        });

        elevationChart.setOnMouseExited((MouseEvent e) -> {
            if (mapViewController != null) {
                mapViewController.clearHighlight();
            }
        });
    }
}
