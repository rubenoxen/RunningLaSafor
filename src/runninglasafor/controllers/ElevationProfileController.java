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
        
        for(TrackPoint point : trackPoints) {
            accumlatedDistance += GeoUtils.distance(prev, point);
            
            series.getData().add(new XYChart.Data<>(accumlatedDistance / 1000.0, point.getElevation()));
            
            prev = point;
        }
        
        elevationChart.getData().add(series);
        setupHoverInteractivity(trackPoints);
    }
    
    private void setupHoverInteractivity(List<TrackPoint> points) {
        elevationChart.setOnMouseMoved((MouseEvent e) -> {
            //ahora cuando lo haga mateo lo meto
            //le paso la coordenada y el lo pone en el mapa
        });
    }
    
}