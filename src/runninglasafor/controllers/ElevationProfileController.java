/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package runninglasafor.controllers;

import java.util.List;
import javafx.fxml.FXML;
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
public class ElevationProfileController {

    @FXML
    private LineChart<Number, Number> elevationChart;
    @FXML
    private NumberAxis yAxis;
    @FXML
    private NumberAxis xAxis;
    
    //mateo tiene q gastar este metodo al cargar un GPX para refrescar la grafica
    public void loadProfileData(Activity activity){
        elevationChart.getData().clear();
        
        if (activity == null || activity.getTrackPoints().isEmpty()){
            return;
        }
        
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        List<TrackPoint> trackPoints = activity.getTrackPoints();
        
        double accumlatedDistance = 0.0;
        TrackPoint prev = trackPoints.get(0);
        
        double maxEle = Double.MIN_VALUE;
        double minEle = Double.MAX_VALUE;
        double distAtMax = 0;
        double distAtMin = 0;
        
        for(TrackPoint point : trackPoints) {
            accumlatedDistance += GeoUtils.distance(prev, point);
            double distKm = accumlatedDistance / 1000.0;
            double ele = point.getElevation();
                      
            series.getData().add(new XYChart.Data<>(distKm, ele));
            
            if(ele > maxEle){ maxEle = ele; distAtMax = distKm;}
            if(ele < minEle){ minEle = ele; distAtMin = distKm;}
            
            prev = point;
        }
        
        XYChart.Series<Number, Number> maxSeries = new XYChart.Series<>();
        maxSeries.getData().add(new XYChart.Data<>(distAtMax, maxEle));
        
        XYChart.Series<Number, Number> minSeries = new XYChart.Series<>();
        minSeries.getData().add(new XYChart.Data<>(distAtMin, minEle));
        
        elevationChart.setCreateSymbols(true);
        elevationChart.getData().addAll(series, maxSeries, minSeries);
        
        setupHoverInteractivity(trackPoints);
        
        for (XYChart.Data<Number, Number> data : series.getData()) {
            data.getNode().setVisible(false);
        }
        
        maxSeries.getNode().setStyle("-fx-stroke: transparent;");
        minSeries.getNode().setStyle("-fx-stroke: transparent;");
    }
    
    private void setupHoverInteractivity(List<TrackPoint> points) {
        elevationChart.setOnMouseMoved((MouseEvent e) -> {
            //ahora cuando lo haga mateo lo meto
            //le paso la coordenada y el lo pone en el mapa
        });
    }
    
}