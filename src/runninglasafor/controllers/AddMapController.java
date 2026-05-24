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
import runninglasafor.MainApp;
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
        ResourceBundle bundle = ResourceBundle.getBundle("runninglasafor.resources.messages", MainApp.getCurrentLocale());
        fc.setTitle(bundle.getString("addmap.filechooser"));
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter(bundle.getString("addmap.filechooser"), "*.png", "*.jpg", "*.jpeg", "*.bmp")
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
        ResourceBundle bundle = ResourceBundle.getBundle("runninglasafor.resources.messages", MainApp.getCurrentLocale());
        
        String name = trim(nameField.getText());
        String imagePath = trim(imagePathField.getText());
        
        if (name.isEmpty()) {
            errorLabel.setText(bundle.getString("addmap.errorName"));
            return;
        }
        
        File imageFile = new File(imagePath);
        if (imagePath.isEmpty() || !imageFile.exists()) {
            errorLabel.setText(bundle.getString("addmap.errorImage"));
            return;
        }
        
        double latMin, latMax, lonMin, lonMax;
        try {           
            latMin = Double.parseDouble(trim(latMinField.getText()));
            latMax = Double.parseDouble(trim(latMaxField.getText()));
            lonMin = Double.parseDouble(trim(lonMinField.getText()));
            lonMax = Double.parseDouble(trim(lonMaxField.getText()));
        } catch (NumberFormatException e) {
            errorLabel.setText(bundle.getString("addmap.errorCoords"));
            return;
        }
                
        if (latMin >= latMax) {
            errorLabel.setText(bundle.getString("addmap.errorLatRange"));
            return;
        }
        if (lonMin >= lonMax) {
            errorLabel.setText(bundle.getString("addmap.errorLonRange"));
            return;
        }

        createdRegion = SportActivityApp.getInstance()
                .addMapRegion(name, imageFile, latMin, latMax, lonMin, lonMax);

        if (createdRegion == null) {
            errorLabel.setText(bundle.getString("addmap.errorGeneric"));
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
            ResourceBundle bundle = ResourceBundle.getBundle("runninglasafor.resources.messages", MainApp.getCurrentLocale());
            stage.setTitle(bundle.getString("addmap.title"));
            javafx.scene.Scene scene = new javafx.scene.Scene(view);
            scene.getStylesheets().add(
                AddMapController.class.getResource("/runninglasafor/resources/estilos.css").toExternalForm());
            stage.setScene(scene);
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