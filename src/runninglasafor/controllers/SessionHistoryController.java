/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package runninglasafor.controllers;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import runninglasafor.MainApp;
import upv.ipc.sportlib.SportActivityApp;
/**
 * FXML Controller class
 *
 * @author mateo
 */
public class SessionHistoryController implements Initializable {

    @FXML private TableView<SportActivityApp> tableActivities;
    @FXML private TableColumn<SportActivityApp, String> colDate;
    @FXML private TableColumn<SportActivityApp, String> colName;
    @FXML private TableColumn<SportActivityApp, String> colDistance;
    @FXML private TableColumn<SportActivityApp, String> colDuration;

    private MainApp mainApp;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        colDate.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return new SimpleStringProperty(cellData.getValue().getTimestamp().format(formatter));
        });
        
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        colDistance.setCellValueFactory(cellData -> {
            double km = cellData.getValue().getDistance() / 1000.0;
            return new SimpleStringProperty(String.format("%.2f km", km));
        });
        
        colDuration.setCellValueFactory(cellData -> {
            long seconds = cellData.getValue().getDuration().getSeconds();
            long h = seconds / 3600;
            long m = (seconds % 3600) / 60;
            long s = seconds % 60;
            return new SimpleStringProperty(String.format("%02d:%02d:%02d", h, m, s));
        });
        
        public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        
        // Si esto da error en rojo, mira el paso 2 abajo
        if (mainApp != null && mainApp.getLib() != null) {
            tableActivities.setItems(FXCollections.observableArrayList(
                mainApp.getLib().getUserActivities(mainApp.getLoggedUser())
            ));
        }
    }
    }    

    @FXML
    private void handleViewDetail(ActionEvent event) {
        
        SportActivityApp selected = tableActivities.getSelectionModel().getSelectedItem();
        if (selected != null) {
           
            mainApp.showActivityDetail(selected);
        } else {
           Alert alert = new Alert(AlertType.WARNING);
           alert.setTitle("Ninguna actividad seleccionada");
           alert.setHeaderText(null); // Lo ponemos a null para que el diseño quede más limpio
           alert.setContentText("Por favor, selecciona una actividad de la tabla para poder ver sus detalles en el mapa.");
            
            
           alert.showAndWait();
        }
    }
    
    private void calcularAcumulados(List<SportActivityApp> actividades) {
    double distanciaTotalMetros = 0;
    long segundosTotales = 0;
    double ascensoTotal = 0;
    double descensoTotal = 0;

    for (SportActivityApp actividad : actividades) {
        distanciaTotalMetros += actividad.getTotalDistance(); 
        segundosTotales += actividad.getDuration().getSeconds(); 
        ascensoTotal += actividad.getElevationGain(); 
        descensoTotal += actividad.getElevationLoss(); 
    }

   
    lblTotalDistancia.setText(String.format("%.2f km", distanciaTotalMetros / 1000.0));
    lblTotalAscenso.setText(String.format("%.0f m", ascensoTotal));
    lblTotalDescenso.setText(String.format("%.0f m", descensoTotal));
    
    
    long h = segundosTotales / 3600;
    long m = (segundosTotales % 3600) / 60;
    lblTotalTiempo.setText(String.format("%d h %d min", h, m));
}
    
}
