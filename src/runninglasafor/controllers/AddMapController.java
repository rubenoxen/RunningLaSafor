/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */

package runninglasafor.controllers;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import upv.ipc.sportlib.MapRegion;
import upv.ipc.sportlib.SportActivityApp;

public class AddMapController implements Initializable {

    @FXML private TextField nameField;
    @FXML private TextField imagePathField;
    @FXML private TextField latMinField;
    @FXML private TextField latMaxField;
    @FXML private TextField lonMinField;
    @FXML private TextField lonMaxField;

    @FXML private Button browseButton;
    @FXML private Button acceptButton;
    @FXML private Button cancelButton;

    @FXML private Label errorLabel;

    private boolean accepted = false;
    private MapRegion createdRegion = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        errorLabel.setText("");
    }

    public boolean isAccepted() {
        return accepted;
    }

    public MapRegion getCreatedMapRegion() {
        return createdRegion;
    }
    
    @FXML
    private void onBrowse() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Selecciona la imagen del mapa");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );
        Stage stage = (Stage) browseButton.getScene().getWindow();
        File file = fc.showOpenDialog(stage);
        if (file != null) {
            imagePathField.setText(file.getAbsolutePath());
        }
    }
    
    @FXML
    private void onCancel() {
        accepted = false;
        closeWindow();
    }
    
    @FXML
    private void onAccept() {
        
        String name = trim(nameField.getText());
        String imagePath = trim(imagePathField.getText());
        
        if (name.isEmpty()) {
            errorLabel.setText("Introduce un nombre para el mapa.");
            return;
        }
        
        File imageFile = new File(imagePath);
        if (imagePath.isEmpty() || !imageFile.exists()) {
            errorLabel.setText("Selecciona una imagen válida.");
            return;
        }
        
        double latMin, latMax, lonMin, lonMax;
        try {
            latMin = Double.parseDouble(trim(latMinField.getText()));
            latMax = Double.parseDouble(trim(latMaxField.getText()));
            lonMin = Double.parseDouble(trim(lonMinField.getText()));
            lonMax = Double.parseDouble(trim(lonMaxField.getText()));
        } catch (NumberFormatException e) {
            errorLabel.setText("Las coordenadas deben ser números decimales (usa punto).");
            return;
        }
        
        if (latMin >= latMax) {
            errorLabel.setText("Latitud mínima debe ser menor que la máxima.");
            return;
        }
        if (lonMin >= lonMax) {
            errorLabel.setText("Longitud mínima debe ser menor que la máxima.");
            return;
        }

        createdRegion = SportActivityApp.getInstance()
                .addMapRegion(name, imageFile, latMin, latMax, lonMin, lonMax);

        if (createdRegion == null) {
            errorLabel.setText("No se ha podido registrar el mapa. Revisa los datos.");
            return;
        }

        accepted = true;
        closeWindow();
    }
    
    private void closeWindow() {
        ((Stage) acceptButton.getScene().getWindow()).close();
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
    
    public static Optional<MapRegion> showDialog(javafx.stage.Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(
                AddMapController.class.getResource("/runninglasafor/views/AddMap.fxml"));
            javafx.scene.Parent view = loader.load();
            AddMapController ctrl = loader.getController();

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.initOwner(owner);
            stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            stage.setTitle("Añadir mapa al sistema");
            stage.setScene(new javafx.scene.Scene(view));
            stage.showAndWait();

            if (ctrl.isAccepted()) {
                return Optional.of(ctrl.getCreatedMapRegion());
            }
            return Optional.empty();
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}