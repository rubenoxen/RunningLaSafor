/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package runninglasafor.controllers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;

import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.GeoUtils;
import upv.ipc.sportlib.TrackPoint;

/**
 *
 * @author rubenpuigmur
 */
public class ElevationProfileController implements Initializable {
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML private LineChart<Number, Number> elevationChart;
    @FXML private NumberAxis yAxis;
    @FXML private NumberAxis xAxis;
    
    private MapViewController mapViewController;
    private double[] accumulatedDistances;
    
    public void loadProfileData(Activity activity){
        // vaciado previo de los datos para no solapar graficas
        elevationChart.getData().clear();
        
        if (activity == null || activity.getTrackPoints().isEmpty()){
            return;
        }
        
        // instanciamos las series requeridas por la api del chart de javafx
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        List<TrackPoint> trackPoints = activity.getTrackPoints();
        
        double accumlatedDistance = 0.0;
        TrackPoint prev = trackPoints.get(0);
        
        double maxEle = Double.MIN_VALUE;
        double minEle = Double.MAX_VALUE;
        double distAtMax = 0;
        double distAtMin = 0;
        
        // guardamos las distancias en un array en paralelo para la busqueda rapida en el hover
        accumulatedDistances = new double[trackPoints.size()];
        int index = 0;
        
        // recorrido basico calculando distancias acumuladas
        for(TrackPoint point : trackPoints) {
            accumlatedDistance += GeoUtils.distance(prev, point);
            double distKm = accumlatedDistance / 1000.0;
            double ele = point.getElevation();
                      
            series.getData().add(new XYChart.Data<>(distKm, ele));
            
            if(ele > maxEle){ maxEle = ele; distAtMax = distKm;}
            if(ele < minEle){ minEle = ele; distAtMin = distKm;}
            
            prev = point;
            accumulatedDistances[index++] = distKm;
        }
        
        // series extra exclusivas para pintar los puntos extremos (max/min)
        XYChart.Series<Number, Number> maxSeries = new XYChart.Series<>();
        maxSeries.getData().add(new XYChart.Data<>(distAtMax, maxEle));
        
        XYChart.Series<Number, Number> minSeries = new XYChart.Series<>();
        minSeries.getData().add(new XYChart.Data<>(distAtMin, minEle));
        
        elevationChart.setCreateSymbols(true);
        elevationChart.getData().addAll(series, maxSeries, minSeries);
        
        setupHoverInteractivity(trackPoints);
        
        // ocultamos los simbolos del trazado principal para que solo quede la linea
        for (XYChart.Data<Number, Number> data : series.getData()) {
            data.getNode().setVisible(false);
        }
        
        maxSeries.getNode().setStyle("-fx-stroke: transparent;");
        minSeries.getNode().setStyle("-fx-stroke: transparent;");
    }
    
    public void setMapViewController(MapViewController mvc) {
        this.mapViewController = mvc;
    }
    
    private void setupHoverInteractivity(List<TrackPoint> points) {
        elevationChart.setOnMouseMoved((MouseEvent e) -> {
            if (points == null || points.isEmpty() || mapViewController == null || accumulatedDistances == null) return;
            
            // hecho por ia: la traduccion del raton en pantalla a valor matematico del chart.
            // meter sceneToLocal y pedir getValueForDisplay
            double mouseXScene = e.getSceneX();
            double mouseXLocal = xAxis.sceneToLocal(mouseXScene, 0).getX();
            double distKmTarget = xAxis.getValueForDisplay(mouseXLocal).doubleValue();
            
            // algoritmo de busqueda del minimo basico iterando el array
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
        
        // lambda trivial de control de eventos
        elevationChart.setOnMouseExited((MouseEvent e) -> {
            if (mapViewController != null) {
                mapViewController.clearHighlight();
            }
        });
    }
}