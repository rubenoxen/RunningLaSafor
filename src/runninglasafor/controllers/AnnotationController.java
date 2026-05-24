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
import runninglasafor.MainApp;
import upv.ipc.sportlib.AnnotationType;

/**
 * FXML Controller class
 *
 * @author rubenpuigmur
 */
public class AnnotationController implements Initializable {

    @FXML private ComboBox<AnnotationType> typeComboBox;
    @FXML private TextField textField;
    @FXML private ColorPicker colorPicker;
    @FXML private Button bAccept;
    @FXML private Button bCancel;

    private boolean isAccepted = false;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {       
        typeComboBox.getItems().setAll(AnnotationType.values());
        ResourceBundle bundle = ResourceBundle.getBundle("runninglasafor.resources.messages", MainApp.getCurrentLocale());
        
        // converter para traducir los tipos de anotacion en el combo
        typeComboBox.setConverter(new javafx.util.StringConverter<AnnotationType>() {
            @Override
            public String toString(AnnotationType t) {
                if (t == null) return "";
                switch (t) {
                    case POINT: return bundle.getString("annotation.addPoint");
                    case TEXT: return bundle.getString("annotation.addText");
                    case LINE: return bundle.getString("annotation.addLine");
                    case CIRCLE: return bundle.getString("annotation.addCircle");
                    default: return t.name();
                }
            }
            @Override
            public AnnotationType fromString(String s) { return null; }
        });
        typeComboBox.getSelectionModel().selectFirst();
        
        colorPicker.setValue(Color.RED);
        
        // cierres con flags para saber si el usuario guardo o cancelo
        bAccept.setOnAction(e -> {
            isAccepted = true;
            ((Stage) bAccept.getScene().getWindow()).close();
        });
        
        bCancel.setOnAction(e -> {
            isAccepted = false;
            ((Stage) bCancel.getScene().getWindow()).close();
        });
    } 
    
    public boolean isAccepted() { return isAccepted; }
    
    public AnnotationType getSelecType() { return typeComboBox.getValue(); }
    
    public void setPreselectedType(AnnotationType type) {
        if (type != null) {
            typeComboBox.getSelectionModel().select(type);
            typeComboBox.setDisable(true);
        }
    }
    
    public String getEnteredText() { return textField.getText().trim(); }
    
    public String getHexColor() {
        // conversion de color de javafx a hex para la libreria
        String c = colorPicker.getValue().toString();
        return "#" + c.substring(2, 8);
    }
}
