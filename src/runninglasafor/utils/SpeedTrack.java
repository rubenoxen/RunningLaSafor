/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package runninglasafor.utils;

import java.util.List;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import upv.ipc.sportlib.MapProjection;
import upv.ipc.sportlib.TrackPoint;
import javafx.scene.shape.Line;

/**
 *
 * @author rubenpuigmur
 */
public class SpeedTrack {    
    public static Group createColoredTrack(List<TrackPoint> track, MapProjection proj) {
        Group trackGroup = new Group();
        if(track == null || track.size() < 2){
            return trackGroup;
        }
        
        double minSpeed = Double.MAX_VALUE;
        double maxSpeed = Double.MIN_VALUE;
        
        for (int i = 0; i < track.size() - 1; i++) {
            double speed = track.get(i).speedTo(track.get(i+1));
            if (speed < minSpeed) minSpeed = speed;
            if (speed > maxSpeed) maxSpeed = speed;
        }
       
        if (maxSpeed == minSpeed) maxSpeed = minSpeed + 1;
        
        for (int i = 0; i < track.size() - 1; i++) {
            TrackPoint p1 = track.get(i);
            TrackPoint p2 = track.get(i+1);
            
            Point2D px1 = proj.project(p1);
            Point2D px2 = proj.project(p2);
            
            Line segment = new Line(px1.getX(), px1.getY(), px2.getX(), px2.getY());
            segment.setStrokeWidth(4.0);
            
            double speed = p1.speedTo(p2);
            double ratio = (speed - minSpeed) / (maxSpeed - minSpeed);
            
            Color color = Color.hsb(ratio * 120, 1.0, 1.0);
            segment.setStroke(color);
            
            segment.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND); 
            segment.setMouseTransparent(true); 
            
            trackGroup.getChildren().add(segment);
        }   
        
    return trackGroup;
    }
}
