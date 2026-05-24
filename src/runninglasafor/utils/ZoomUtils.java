/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package runninglasafor.utils;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;

/**
 *
 * @author rubenpuigmur
 */
public class ZoomUtils {
    
    public static void applyZoomCorrection(Group group, DoubleProperty zoomProperty, double baseStrokeWidth){
        
        for(Node node : group.getChildren()){
            
            String tipoNodo = node.getClass().getSimpleName();
            
            if(tipoNodo.equals("Circle")){
                Circle c = (Circle) node;
                c.strokeWidthProperty().bind(Bindings.divide(baseStrokeWidth, zoomProperty));
                
                if(c.getRadius() <= 5){
                    c.radiusProperty().bind(Bindings.divide(5, zoomProperty));
                }
            } else if(tipoNodo.equals("Line") || tipoNodo.equals("Polyline")) {
                Shape forma = (Shape) node;
                forma.strokeWidthProperty().bind(Bindings.divide(baseStrokeWidth, zoomProperty));
            } else if(tipoNodo.equals("Text")) {
                Text t = (Text) node;
                
                t.scaleXProperty().bind(Bindings.divide(1, zoomProperty));
                t.scaleYProperty().bind(Bindings.divide(1, zoomProperty));
            }
        }
    }
}