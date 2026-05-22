/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package runninglasafor.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import upv.ipc.sportlib.AnnotationType;

/**
 * FXML Controller class
 *
 * @author rubenpuigmur
 */
public class AnnotationController implements Initializable {

    @FXML
    private ComboBox<AnnotationType> typeComboBox;
    @FXML
    private TextField textField;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Button bAccept;
    @FXML
    private Button bCancel;

    /**
     * Initializes the controller class.
     */
    
    private boolean isAccepted = false;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        typeComboBox.getItems().setAll(AnnotationType.values());
        typeComboBox.getSelectionModel().selectFirst();
        
        colorPicker.setValue(Color.RED);
        
        bAccept.setOnAction(e -> {
            isAccepted = true;
            ((Stage) bAccept.getScene().getWindow()).close();
        });
        
        bCancel.setOnAction(e -> {
            isAccepted = false;
            ((Stage) bAccept.getScene().getWindow()).close();
        });
        
    } 
    
    //integrar esto en el mapa
    
    public boolean isAccepted() {
        return isAccepted;
    }
    
    public AnnotationType getSelecType() {
        return typeComboBox.getValue();
    }
    
    public void setPreselectedType(AnnotationType type) {
        if (type != null) {
            typeComboBox.getSelectionModel().select(type);
        }
    }
    
    public String getEnteredText() {
        return textField.getText().trim();
    }
    
    public String getHexColor() {
        String c = colorPicker.getValue().toString();
        return "#" + c.substring(2, 8);
    }
}
