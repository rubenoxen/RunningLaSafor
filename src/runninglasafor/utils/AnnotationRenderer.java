/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package runninglasafor.utils;

import java.util.List;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import upv.ipc.sportlib.Annotation;
import upv.ipc.sportlib.GeoPoint;
import upv.ipc.sportlib.MapProjection;

/**
 *
 * @author rubenpuigmur
 */
public class AnnotationRenderer {
    
    //anotacion guardada
    public static Node createVisualAnnotation(Annotation annotation, MapProjection proj){
        Group group = new Group();
        List<GeoPoint> geoPoints = annotation.getGeoPoints();
        
        if (geoPoints == null || geoPoints.isEmpty()){
            return group;
        }
        
        Point2D p1 = proj.project(geoPoints.get(0));
        Color color = Color.web(annotation.getColor());
        double strokeWidth = annotation.getStrokeWidth();
        
        switch(annotation.getType()){
            case POINT:
                Circle point = new Circle(p1.getX(), p1.getY(), 5, color);
                group.getChildren().add(point);
                if (annotation.getText() != null && !annotation.getText().trim().isEmpty()) {
                    Text text = new Text(p1.getX() + 8, p1.getY() + 4, annotation.getText());
                    text.setFill(color);
                    text.setFont(Font.font("System", FontWeight.BOLD, 12));
                    group.getChildren().add(text);
                }
                break;
                
            case TEXT: 
                Text textOnly = new Text(p1.getX(),p1.getY(),annotation.getText());
                textOnly.setFill(color);
                textOnly.setFont(Font.font("System", FontWeight.BOLD, 14));
                group.getChildren().add(textOnly);
                break;
                
            case LINE:
                if (geoPoints.size() >= 2) {
                    Point2D p2 = proj.project(geoPoints.get(1));
                    Line line = new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
                    line.setStroke(color);
                    line.setStrokeWidth(strokeWidth);
                    group.getChildren().add(line);
                }
                break;
                
            case CIRCLE:
                if (geoPoints.size() >= 2) {
                    Point2D p2 = proj.project(geoPoints.get(1));
                    double radius = p1.distance(p2);
                    Circle circle = new Circle(p1.getX(), p1.getY(), radius);
                    circle.setStroke(color);
                    circle.setStrokeWidth(strokeWidth);
                    circle.setFill(Color.TRANSPARENT);
                    group.getChildren().add(circle);
                }
                break;
        }
        
        return group;
    }
}
