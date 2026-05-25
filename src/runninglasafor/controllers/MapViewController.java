/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package runninglasafor.controllers;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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
import javafx.scene.control.SeparatorMenuItem;
import java.util.Optional;
import javafx.util.StringConverter;

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
        
        // instanciamos contextmenu dinamico para pedir opciones con el clic derecho
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

            MenuItem miDelete = new MenuItem(bundle.getString("annotation.delete"));
            miDelete.setDisable(currentActivity.getAnnotations().isEmpty());
            miDelete.setOnAction(ev -> deleteAnnotation());

            menu.getItems().addAll(miPoint, miText, miLine, miCircle, 
                                   new SeparatorMenuItem(), miDelete); 
            menu.show(mapPane, e.getScreenX(), e.getScreenY());
        });
        
        mapPane.setOnMouseClicked(e -> {
            // control de flujo manual: si no estabamos pidiendo segundo punto abortamos
            if (!awaitingSecondClick) return;
            if (e.getButton() != javafx.scene.input.MouseButton.PRIMARY) return;
            if (currentProj == null || currentActivity == null) return;

            GeoPoint secondPoint = currentProj.unproject(e.getX(), e.getY());
            completarAnotacionDosPuntos(secondPoint);
        });
        
        mapPane.setOnKeyPressed(ke -> {
            // maquina de estados para limpiar flags
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
        
        // instanciamos una marca temporal normal para feedback visual
        Point2D p1 = currentProj.project(firstPoint);
        tempFirstMarker = new Circle(p1.getX(), p1.getY(), 6, Color.ORANGE);
        tempFirstMarker.setStroke(Color.WHITE);
        tempFirstMarker.setStrokeWidth(1.5);
        tempFirstMarker.setOpacity(0.85);       
        tempFirstMarker.setMouseTransparent(true); 
        mapPane.getChildren().add(tempFirstMarker);
        mapPane.requestFocus(); 
    }

    private void completarAnotacionDosPuntos(GeoPoint secondPoint) {        
        mapPane.setCursor(javafx.scene.Cursor.DEFAULT);
        awaitingSecondClick = false;
       
        if (tempFirstMarker != null) {
            mapPane.getChildren().remove(tempFirstMarker);
            tempFirstMarker = null;
        }
        
        if (pendingFirstPoint != null && pendingAnnotationType != null) {
            List<GeoPoint> puntos = List.of(pendingFirstPoint, secondPoint);
            crearAnotacion(puntos, pendingAnnotationType);
        }

        pendingFirstPoint = null;
        pendingAnnotationType = null;
    }

    private void crearAnotacion(List<GeoPoint> puntos, AnnotationType tipoForzado) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(
                    "runninglasafor.resources.messages", MainApp.getCurrentLocale());
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/runninglasafor/views/Annotation.fxml"), bundle);
            Parent view = loader.load();
            AnnotationController ctrl = loader.getController();

            ctrl.setPreselectedType(tipoForzado);
            
            boolean isDark = mapPane.getScene().getRoot().getStyleClass().contains("theme-dark");
            if (isDark) {
                view.getStyleClass().add("theme-dark");
            }

            Stage stage = new Stage();
            stage.initOwner(mapPane.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle(bundle.getString("annotation.title"));

            Scene scene = new Scene(view);
            scene.getStylesheets().add(
                    getClass().getResource("/runninglasafor/resources/estilos.css").toExternalForm());
            
            if (isDark) {
                scene.setFill(Color.web("#1E1849"));
            }

            stage.setScene(scene);
            stage.showAndWait();

            if (ctrl.isAccepted()) {
                Annotation ann = new Annotation(ctrl.getSelecType(), ctrl.getEnteredText(),
                        ctrl.getHexColor(), 2.0, puntos);
                SportActivityApp.getInstance().addAnnotation(currentActivity, ann);
                loadActivityWithMap(currentActivity, currentProj.getRegion());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void loadActivity(Activity activity) {
        if (activity == null) return;
        this.currentActivity = activity;
        MapRegion region = activity.getSuggestedMap();
        if (region == null) {
            region = SportActivityApp.getInstance().findMapForActivity(activity);
        }
        if (region == null) {
            clearMap();
            return;
        }
        loadActivityWithMap(activity, region);
    }

    private boolean loadMapRegion(MapRegion region) {
        if (region == null) {
            clearMap();
            return false;
        }
        File imgFile = new File(region.getImagePath());
        if (imgFile.exists()) {
            Image img = new Image(imgFile.toURI().toString());
            mapImageView.setImage(img);
            mapImageView.setEffect(null);
            mapPane.setPrefSize(img.getWidth(), img.getHeight());
            return true;
        }
        clearMap();
        return false;
    }

    public void loadActivityWithMap(Activity activity, MapRegion region) {
        if (activity == null || region == null) return;
        this.currentActivity = activity;        
        if (!loadMapRegion(region)) return;
                
        clearOverlays();
        
        drawRouteAndMarkers(activity, region);
    }

    private void clearMap() {
        currentProj = null;
        mapImageView.setImage(null);
        clearOverlays();
    }

    private void clearOverlays() {
        if (mapPane.getChildren().size() > 1) {
            mapPane.getChildren().remove(1, mapPane.getChildren().size());
        }
        highlightMarker = null;
        tempFirstMarker = null;
    }
    
    private void drawRouteAndMarkers(Activity activity, MapRegion region) {
        if (region == null || mapPane.getPrefWidth() <= 0 || mapPane.getPrefHeight() <= 0) return;
        
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
            // instanciamos marcadores basandose en conversion project
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
        
        Platform.runLater(() -> {
            if (startTP != null && mapPane.getWidth() > 0) {
                Point2D startPix = currentProj.project(startTP);                
                mapScrollPane.setHvalue(startPix.getX() / mapPane.getWidth());
                mapScrollPane.setVvalue(startPix.getY() / mapPane.getHeight());
            }
        });
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
    
    public void refreshTheme() {
        if (mapImageView.getImage() == null) return;
        mapImageView.setEffect(null);
    }
    
    private void deleteAnnotation() {
        if (currentActivity == null || currentActivity.getAnnotations().isEmpty()) return;

        ResourceBundle bundle = ResourceBundle.getBundle(
                "runninglasafor.resources.messages", MainApp.getCurrentLocale());

        javafx.scene.control.Dialog<Annotation> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle(bundle.getString("annotation.delete.title"));
        dialog.setHeaderText(bundle.getString("annotation.delete.header"));

        javafx.scene.control.ComboBox<Annotation> comboBox = new javafx.scene.control.ComboBox<>(
                javafx.collections.FXCollections.observableArrayList(currentActivity.getAnnotations()));
                
        comboBox.setConverter(new StringConverter<Annotation>() {
            @Override
            public String toString(Annotation ann) {
                if (ann == null) return "";
                String text = ann.getText();
                if (text != null && !text.trim().isEmpty()) {
                    return ann.getType().name() + ": \"" + text + "\"";
                }
                return ann.getType().name() + " (" + ann.getColor() + ")";
            }

            @Override
            public Annotation fromString(String string) {
                return null;
            }
        });

        if (!currentActivity.getAnnotations().isEmpty()) {
            comboBox.getSelectionModel().selectFirst();
        }

        comboBox.setPrefWidth(350);

        dialog.getDialogPane().setContent(comboBox);
        dialog.getDialogPane().getButtonTypes().addAll(
                javafx.scene.control.ButtonType.OK, 
                javafx.scene.control.ButtonType.CANCEL);

        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/runninglasafor/resources/estilos.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("custom-alert");
        MainApp.applyTheme(dialog.getDialogPane());

        dialog.setResultConverter(buttonType -> {
            if (buttonType == javafx.scene.control.ButtonType.OK) {
                return comboBox.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        Optional<Annotation> result = dialog.showAndWait();

        result.ifPresent(ann -> {
            SportActivityApp.getInstance().removeAnnotation(ann);
            loadActivityWithMap(currentActivity, currentProj.getRegion());
        });
    }
}
