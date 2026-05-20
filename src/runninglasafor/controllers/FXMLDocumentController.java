/*
 * ============================================================
 *  PROYECTO EJEMPLO – IPC 2026
 *  Asignatura: Interfaces Persona-Computador
 *  Universitat Politècnica de València
 * ============================================================
 *
 *  DESCRIPCIÓN GENERAL
 *  -------------------
 *  Este controlador gestiona la vista principal de la aplicación
 *  de puntos de interés (POI) sobre un mapa.
 *
 *  Funcionalidades implementadas:
 *   1. Carga y visualización de una imagen de mapa.
 *   2. Zoom interactivo mediante un Slider.
 *   3. Añadir POIs (texto) y anotaciones (círculos) con clic derecho.
 *   4. Listado de POIs en un ListView con CellFactory personalizada.
 *   5. Centrado animado del mapa al seleccionar un POI de la lista.
 *   6. Modo inserción: activar con botón y colocar POI con siguiente clic.
 *
 *  PATRÓN UTILIZADO: MVC (Model-View-Controller)
 *   - Modelo : clase Poi  (datos del punto de interés)
 *   - Vista  : FXMLDocument.fxml  (layout declarativo)
 *   - Control: esta clase (lógica de interacción)
 *
 * ============================================================
 */
package runninglasafor.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import mapademo.Poi;

// IMPORTANTE: Asegúrate de que estas rutas son correctas en tu proyecto
import runninglasafor.utils.SpeedTrack;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.MapProjection;
import upv.ipc.sportlib.TrackPoint;

public class FXMLDocumentController implements Initializable {

    @FXML private Group zoomGroup;
    @FXML private Pane mapPane;
    @FXML private ScrollPane map_scrollpane;
    @FXML private Slider zoom_slider;
    @FXML private Label mousePosition;
    @FXML private ListView<Poi> map_listview; // Lista de POIs (Puntos)
    @FXML private ListView<Activity> activityListView; // Lista de Actividades (Rutas)
    
    private ContextMenu mapContextMenu;
    private boolean insertionMode = false;
    private Group currentRouteGroup; // Para guardar la ruta dibujada y poder borrarla

    // =========================================================
    //  GESTIÓN DE ZOOM
    // =========================================================

    private void zoom(double scaleValue) {
        double scrollH = map_scrollpane.getHvalue();
        double scrollV = map_scrollpane.getVvalue();

        zoomGroup.setScaleX(scaleValue);
        zoomGroup.setScaleY(scaleValue);

        map_scrollpane.setHvalue(scrollH);
        map_scrollpane.setVvalue(scrollV);
    }

    @FXML void zoomIn(ActionEvent event) { zoom_slider.setValue(zoom_slider.getValue() + 0.1); }
    @FXML void zoomOut(ActionEvent event) { zoom_slider.setValue(zoom_slider.getValue() - 0.1); }

    // =========================================================
    //  DIBUJAR RUTA (ACTIVIDADES)
    // =========================================================

    public void mostrarActividad(Activity actividad) {
        if (actividad == null || mapPane == null) return;

        // 1. Limpiar la ruta anterior para no acumular líneas
        if (currentRouteGroup != null) {
            mapPane.getChildren().remove(currentRouteGroup);
        }

        // 2. Crear proyección (GPS -> Píxeles)
        // Usamos las dimensiones del Pane donde se dibuja
        MapProjection proj = new MapProjection(actividad.getSuggestedMap(), 
                                               mapPane.getPrefWidth(), 
                                               mapPane.getPrefHeight());

        // 3. Crear el grupo de líneas de colores
        currentRouteGroup = SpeedTrack.createColoredTrack(actividad.getTrackPoints(), proj);
        
        // 4. Añadir al mapa
        mapPane.getChildren().add(currentRouteGroup);
    }

    // =========================================================
    //  INICIALIZACIÓN
    // =========================================================

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Slider Zoom
        zoom_slider.setMin(0.5);
        zoom_slider.setMax(1.5);
        zoom_slider.setValue(1.0);
        zoom_slider.valueProperty().addListener((obs, oldVal, newVal) -> zoom(newVal.doubleValue()));

        // Context Menu
        MenuItem miText = new MenuItem("📝 Añadir texto");
        MenuItem miCircle = new MenuItem("⭕ Añadir círculo");
        mapContextMenu = new ContextMenu(miText, miCircle);

        // --- Configurar Lista de POIs ---
        map_listview.setCellFactory(lv -> new ListCell<Poi>() {
            @Override
            protected void updateItem(Poi poi, boolean empty) {
                super.updateItem(poi, empty);
                setText((empty || poi == null) ? null : poi.getCode() + " – " + poi.getPosition());
            }
        });

        // --- Configurar Lista de ACTIVIDADES ---
        activityListView.setCellFactory(lv -> new ListCell<Activity>() {
            @Override
            protected void updateItem(Activity act, boolean empty) {
                super.updateItem(act, empty);
                setText((empty || act == null) ? null : act.getName() + " (" + act.getName()+ ")");
            }
        });

        // Evento al seleccionar una Actividad
        activityListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                dibujarTrackSimple(newVal); 
            }
        });

        // Cargar mapa e hilos iniciales
        buildMap(new File("maps/upv.jpg"));
        
        // Carga de datos "antes" (Punto del guion)
        List<Activity> misActividades = cargarActividades(); 
        if (misActividades != null) {
            activityListView.getItems().addAll(misActividades);
        }
    }

    private List<Activity> cargarActividades() {
        // Aquí iría tu lógica real de carga de archivos GPX
        List<Activity> lista = new ArrayList<>();
        return new ArrayList<>(); 
    }

    // =========================================================
    //  CONSTRUCCIÓN DEL MAPA
    // =========================================================

    private void buildMap(File imgFile) {
        if (!imgFile.exists()) return;

        Image img = new Image(imgFile.toURI().toString());
        double W = img.getWidth();
        double H = img.getHeight();

        mapPane.setPrefSize(W, H);
        mapPane.setMinSize(W, H);
        mapPane.getChildren().clear(); // Limpiar antes de cargar nuevo
        
        ImageView iv = new ImageView(img);
        mapPane.getChildren().add(iv);

        mapPane.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                onMapRightClick(e.getX(), e.getY());
            } else if (e.getButton() == MouseButton.PRIMARY && insertionMode) {
                insertionMode = false;
                mapPane.setStyle("");
                addPoi(e.getX(), e.getY());
            }
        });
    }

    // =========================================================
    //  INTERFAZ Y BOTONES
    // =========================================================

    @FXML
    private void handleToggleList(ActionEvent event) {
        boolean isVisible = activityListView.isVisible();
        activityListView.setVisible(!isVisible);
        activityListView.setManaged(!isVisible);
    }

    @FXML
    private void showPosition(MouseEvent event) {
        mousePosition.setText("X: " + (int) event.getX() + ", Y: " + (int) event.getY());
    }

    private void onMapRightClick(double x, double y) {
        mapContextMenu.hide();
        mapContextMenu.getItems().get(0).setOnAction(e -> addPoi(x, y));
        mapContextMenu.getItems().get(1).setOnAction(e -> addCircle(x, y));
        mapContextMenu.show(mapPane.getScene().getWindow(), 
                           mapPane.localToScreen(x, y).getX(), 
                           mapPane.localToScreen(x, y).getY());
    }

    private void addPoi(double x, double y) {
        // Tu lógica de diálogo de POI (se mantiene igual que tu código original)
        // ...
    }

    private void addCircle(double x, double y) {
        Circle circle = new Circle(10, Color.RED);
        circle.setCenterX(x);
        circle.setCenterY(y);
        mapPane.getChildren().add(circle);
    }
    
    public void dibujarTrackSimple(Activity actividad) {
    if (actividad == null || mapPane == null) return;

    
    List<TrackPoint> puntos = actividad.getTrackPoints();
    if (puntos == null || puntos.isEmpty()) return;

    if (currentRouteGroup != null) {
        mapPane.getChildren().remove(currentRouteGroup);
    }

    // 2. Crear proyección (GPS a Píxeles)
    MapProjection proj = new MapProjection(
        actividad.getSuggestedMap(), 
        mapPane.getPrefWidth(), 
        mapPane.getPrefHeight()
    );

    
    currentRouteGroup = SpeedTrack.createColoredTrack(puntos, proj);

    
    TrackPoint startTP = actividad.getStartPoint(); 
    TrackPoint endTP = actividad.getEndPoint();

    
    javafx.geometry.Point2D startPix = proj.project(startTP); 
    javafx.geometry.Point2D endPix = proj.project(endTP);

    
    Circle startCircle = new Circle(6, Color.CHARTREUSE); 
    startCircle.setCenterX(startPix.getX());
    startCircle.setCenterY(startPix.getY());
    startCircle.setStroke(Color.BLACK);

    
    Circle endCircle = new Circle(6, Color.RED);
    endCircle.setCenterX(endPix.getX());
    endCircle.setCenterY(endPix.getY());
    endCircle.setStroke(Color.BLACK);

   
    currentRouteGroup.getChildren().addAll(startCircle, endCircle);

    
    mapPane.getChildren().add(currentRouteGroup);
}
    
}