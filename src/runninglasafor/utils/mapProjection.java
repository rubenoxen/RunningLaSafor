/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package runninglasafor.utils;

import javafx.geometry.Point2D;
import upv.ipc.sportlib.MapRegion;
import upv.ipc.sportlib.TrackPoint;

/**
 *
 * @author mateo
 */
public class mapProjection {
    private final MapRegion region;
    private final double width;
    private final double height;

    public mapProjection(MapRegion region, double width, double height) {
        this.region = region;
        this.width = width;
        this.height = height;
    }

    public Point2D project(TrackPoint point) {
        
        double minLat = region.getLatMin(); // Latitud inferior
        double maxLat = region.getLatMax();    // Latitud superior
        double minLon = region.getLonMin();   // Longitud izquierda
        double maxLon = region.getLonMax();  // Longitud derecha

       
        double normalizedX = (point.getLongitude() - minLon) / (maxLon - minLon);
        
        
        double normalizedY = (maxLat - point.getLatitude()) / (maxLat - minLat);

        
        double x = normalizedX * width;
        double y = normalizedY * height;

        return new Point2D(x, y);
    }
}

