/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package runninglasafor.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import runninglasafor.MainApp;
import runninglasafor.utils.AnnotationRenderer;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.MapProjection;
import upv.ipc.sportlib.MapRegion;
import runninglasafor.utils.SpeedTrack;
import runninglasafor.utils.ZoomUtils;
import upv.ipc.sportlib.Annotation;
import upv.ipc.sportlib.AnnotationType;
import upv.ipc.sportlib.GeoPoint;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.TrackPoint;

public class MapViewController implements Initializable {
    
    @FXML private ScrollPane mapScrollPane;
    @FXML private Group zoomGroup;
    @FXML private Pane mapPane;
    @FXML private ImageView mapImageView;
    @FXML private Slider zoomSlider;
    
    private MapProjection currentProj;
    private Activity currentActivity;
    private Circle highlightMarker; 
    
    private AnnotationType pendingAnnotationType;
    private GeoPoint pendingFirstPoint;
    private boolean awaitingSecondClick = false;
    private Circle tempFirstMarker;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            zoomGroup.setScaleX(newVal.doubleValue());
            zoomGroup.setScaleY(newVal.doubleValue());
        });
        
        mapPane.setOnContextMenuRequested(e -> {
            if (currentProj == null || currentActivity == null) return;

            ContextMenu menu = new ContextMenu();
            ResourceBundle bundle = ResourceBundle.getBundle(
                    "runninglasafor.resources.messages", MainApp.getCurrentLocale());

            GeoPoint geo = currentProj.unproject(e.getX(), e.getY());

            MenuItem miPoint = new MenuItem(bundle.getString("annotation.addPoint"));
            MenuItem miText  = new MenuItem(bundle.getString("annotation.addText"));
            MenuItem miLine  = new MenuItem(bundle.getString("annotation.addLine"));
            MenuItem miCircle = new MenuItem(bundle.getString("annotation.addCircle"));

            miPoint.setOnAction(ev -> crearAnotacion(List.of(geo), AnnotationType.POINT));
            miText.setOnAction(ev  -> crearAnotacion(List.of(geo), AnnotationType.TEXT));
            miLine.setOnAction(ev  -> iniciarSegundoPunto(geo, AnnotationType.LINE));
            miCircle.setOnAction(ev -> iniciarSegundoPunto(geo, AnnotationType.CIRCLE));

            menu.getItems().addAll(miPoint, miText, miLine, miCircle);
            menu.show(mapPane, e.getScreenX(), e.getScreenY());
        });
        
        mapPane.setOnMouseClicked(e -> {
            if (!awaitingSecondClick) return;
            if (e.getButton() != javafx.scene.input.MouseButton.PRIMARY) return;
            if (currentProj == null || currentActivity == null) return;

            GeoPoint secondPoint = currentProj.unproject(e.getX(), e.getY());
            completarAnotacionDosPuntos(secondPoint);
        });
        
        mapPane.setOnKeyPressed(ke -> {
            if (awaitingSecondClick
                    && ke.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                awaitingSecondClick = false;
                mapPane.setCursor(javafx.scene.Cursor.DEFAULT);
                if (tempFirstMarker != null) {
                    mapPane.getChildren().remove(tempFirstMarker);
                    tempFirstMarker = null;
                }
                pendingFirstPoint = null;
                pendingAnnotationType = null;
            }
        });
        
        mapPane.setFocusTraversable(true);
    }

    @FXML private void zoomIn() { zoomSlider.setValue(zoomSlider.getValue() + 0.1); }
    @FXML private void zoomOut() { zoomSlider.setValue(zoomSlider.getValue() - 0.1); }
    
    private void iniciarSegundoPunto(GeoPoint firstPoint, AnnotationType type) {
        this.pendingFirstPoint = firstPoint;
        this.pendingAnnotationType = type;
        this.awaitingSecondClick = true;
        
        mapPane.setCursor(javafx.scene.Cursor.CROSSHAIR);
        
        Point2D p1 = currentProj.project(firstPoint);
        tempFirstMarker = new Circle(p1.getX(), p1.getY(), 6, Color.ORANGE);
        tempFirstMarker.setStroke(Color.WHITE);
        tempFirstMarker.setStrokeWidth(1.5);
        tempFirstMarker.setOpacity(0.85);
        mapPane.getChildren().add(tempFirstMarker);
    }

    private void completarAnotacionDosPuntos(GeoPoint secondPoint) {        
        mapPane.setCursor(javafx.scene.Cursor.DEFAULT);
        awaitingSecondClick = false;
       
        if (tempFirstMarker != null) {
            mapPane.getChildren().remove(tempFirstMarker);
            tempFirstMarker = null;
        }
        
        List<GeoPoint> puntos = List.of(pendingFirstPoint, secondPoint);
        crearAnotacion(puntos, pendingAnnotationType);

        pendingFirstPoint = null;
        pendingAnnotationType = null;
    }

    private void crearAnotacion(List<GeoPoint> puntos, AnnotationType tipoForzado) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/runninglasafor/views/Annotation.fxml"));
            Parent view = loader.load();
            AnnotationController ctrl = loader.getController();
            
            ctrl.setPreselectedType(tipoForzado);

            Stage stage = new Stage();
            stage.initOwner(mapPane.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            ResourceBundle bundle = ResourceBundle.getBundle(
                    "runninglasafor.resources.messages", MainApp.getCurrentLocale());
            stage.setTitle(bundle.getString("annotation.title"));
            Scene scene = new Scene(view);
            scene.getStylesheets().add(
                getClass().getResource("/resources/estilos.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();

            if (ctrl.isAccepted()) {
                Annotation ann = new Annotation(
                        ctrl.getSelecType(), ctrl.getEnteredText(),
                        ctrl.getHexColor(), 2.0, puntos);
                SportActivityApp.getInstance().addAnnotation(currentActivity, ann);
                loadActivityWithMap(currentActivity, currentProj.getRegion());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void loadActivity(Activity activity) {
        if (activity == null) return;
        this.currentActivity = activity;
        MapRegion region = activity.getSuggestedMap();
        loadMapRegion(region);
        drawRouteAndMarkers(activity, region);
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
    
    public void loadActivityWithMap(Activity activity, MapRegion region) {
        if (activity == null || region == null) return;
        this.currentActivity = activity;        
        loadMapRegion(region);
                
        if (mapPane.getChildren().size() > 1) {
            mapPane.getChildren().remove(1, mapPane.getChildren().size());
        }
        
        drawRouteAndMarkers(activity, region);
    }
    
    private void drawRouteAndMarkers(Activity activity, MapRegion region) {
        if (mapPane.getPrefWidth() <= 0) return;
        
        this.currentProj = new MapProjection(region, mapPane.getPrefWidth(), mapPane.getPrefHeight());
                
        Group route = SpeedTrack.createColoredTrack(activity.getTrackPoints(), currentProj);
        ZoomUtils.applyZoomCorrection(route, zoomSlider.valueProperty(), 4.0);
        mapPane.getChildren().add(route);
                
        TrackPoint startTP = activity.getStartPoint();
        TrackPoint endTP = activity.getEndPoint();
        
        for (Annotation ann : activity.getAnnotations()) {
            Group visual = (Group) AnnotationRenderer.createVisualAnnotation(ann, currentProj);
            ZoomUtils.applyZoomCorrection(visual, zoomSlider.valueProperty(), ann.getStrokeWidth());
            mapPane.getChildren().add(visual);
        }
        
        if (startTP != null) {
            Point2D startPix = currentProj.project(startTP);
            Circle startCircle = new Circle(6, Color.CHARTREUSE);
            startCircle.setCenterX(startPix.getX());
            startCircle.setCenterY(startPix.getY());
            startCircle.setStroke(Color.BLACK);
            startCircle.setStrokeWidth(1.5);
            startCircle.radiusProperty().bind(javafx.beans.binding.Bindings.divide(6.0, zoomSlider.valueProperty()));
            startCircle.strokeWidthProperty().bind(javafx.beans.binding.Bindings.divide(1.5, zoomSlider.valueProperty()));
            mapPane.getChildren().add(startCircle);
        }

        if (endTP != null) {
            Point2D endPix = currentProj.project(endTP);
            Circle endCircle = new Circle(6, Color.RED);
            endCircle.setCenterX(endPix.getX());
            endCircle.setCenterY(endPix.getY());
            endCircle.setStroke(Color.BLACK);
            endCircle.setStrokeWidth(1.5);
            endCircle.radiusProperty().bind(javafx.beans.binding.Bindings.divide(6.0, zoomSlider.valueProperty()));
            endCircle.strokeWidthProperty().bind(javafx.beans.binding.Bindings.divide(1.5, zoomSlider.valueProperty()));
            mapPane.getChildren().add(endCircle);
        }
    }
    
    public void highlightPoint(TrackPoint tp) {
        if (currentProj == null || tp == null) return;

        if (highlightMarker != null) {
            mapPane.getChildren().remove(highlightMarker);
        }

        Point2D pix = currentProj.project(tp);
        highlightMarker = new Circle(8, Color.YELLOW);
        highlightMarker.setCenterX(pix.getX());
        highlightMarker.setCenterY(pix.getY());
        highlightMarker.setStroke(Color.BLACK);
        highlightMarker.setStrokeWidth(2.0);
        highlightMarker.setOpacity(0.8);
        highlightMarker.radiusProperty().bind(
                javafx.beans.binding.Bindings.divide(8.0, zoomSlider.valueProperty()));
        highlightMarker.strokeWidthProperty().bind(
                javafx.beans.binding.Bindings.divide(2.0, zoomSlider.valueProperty()));

        mapPane.getChildren().add(highlightMarker);
    }
    
    public void clearHighlight() {
        if (highlightMarker != null) {
            mapPane.getChildren().remove(highlightMarker);
            highlightMarker = null;
        }
    }
}