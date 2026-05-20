/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package runninglasafor.controllers;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.MapProjection;
import upv.ipc.sportlib.MapRegion;
import runninglasafor.utils.SpeedTrack;
import runninglasafor.utils.ZoomUtils;

public class MapViewController implements Initializable {
    
    @FXML private ScrollPane mapScrollPane;
    @FXML private Group zoomGroup;
    @FXML private Pane mapPane;
    @FXML private ImageView mapImageView;
    @FXML private Slider zoomSlider;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            zoomGroup.setScaleX(newVal.doubleValue());
            zoomGroup.setScaleY(newVal.doubleValue());
        });
    }

    @FXML private void zoomIn() { zoomSlider.setValue(zoomSlider.getValue() + 0.1); }
    @FXML private void zoomOut() { zoomSlider.setValue(zoomSlider.getValue() - 0.1); }

    public void loadActivity(Activity activity) {
        if (activity == null) return;
        
        // 1. Cargar el mapa sugerido por la actividad
        MapRegion region = activity.getSuggestedMap();
        loadMapRegion(region);
        
        if (mapPane.getPrefWidth() > 0) {
            MapProjection proj = new MapProjection(region, mapPane.getPrefWidth(), mapPane.getPrefHeight());
            Group route = SpeedTrack.createColoredTrack(activity.getTrackPoints(), proj);
                        
            ZoomUtils.applyZoomCorrection(route, zoomSlider.valueProperty(), 4.0);
            
            mapPane.getChildren().add(route);
        }
    }

    private void loadMapRegion(MapRegion region) {
        if (region == null) return;
        File imgFile = new File(region.getImagePath());
        if (imgFile.exists()) {
            Image img = new Image(imgFile.toURI().toString());
            mapImageView.setImage(img);
            mapPane.setPrefSize(img.getWidth(), img.getHeight());
        }
    }
}