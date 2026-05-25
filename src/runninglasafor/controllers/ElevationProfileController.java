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
        
        double maxEle = -Double.MAX_VALUE;
        double minEle = Double.MAX_VALUE;
        double maxDist = 0.0;
        double minDist = 0.0;

        for (TrackPoint point : trackPoints) {
            accumulatedDistance += GeoUtils.distance(prev, point);
            double distKm = accumulatedDistance / 1000.0;
            double ele = point.getElevation();

            series.getData().add(new XYChart.Data<>(distKm, ele));

            if (ele > maxEle) { maxEle = ele; maxDist = distKm; }
            if (ele < minEle) { minEle = ele; minDist = distKm; }

            prev = point;
            accumulatedDistances[index++] = distKm;
        }

        elevationChart.getData().add(series);

        XYChart.Series<Number, Number> maxSeries = new XYChart.Series<>();
        XYChart.Data<Number, Number> maxData = new XYChart.Data<>(maxDist, maxEle);
        javafx.scene.shape.Circle maxCircle = new javafx.scene.shape.Circle(5, javafx.scene.paint.Color.web("#2ECC71"));
        maxData.setNode(maxCircle);
        maxSeries.getData().add(maxData);

        XYChart.Series<Number, Number> minSeries = new XYChart.Series<>();
        XYChart.Data<Number, Number> minData = new XYChart.Data<>(minDist, minEle);
        javafx.scene.shape.Circle minCircle = new javafx.scene.shape.Circle(5, javafx.scene.paint.Color.web("#E74C3C"));
        minData.setNode(minCircle);
        minSeries.getData().add(minData);

        elevationChart.getData().addAll(maxSeries, minSeries);

        maxSeries.getNode().setStyle("-fx-stroke: transparent; -fx-fill: transparent;");
        minSeries.getNode().setStyle("-fx-stroke: transparent; -fx-fill: transparent;");

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
