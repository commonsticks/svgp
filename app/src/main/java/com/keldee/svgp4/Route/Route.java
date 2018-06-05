package com.keldee.svgp4.Route;

import android.util.Log;

import com.keldee.svgp4.GoogleAPI.SVPoint.GCoordinate;
import com.keldee.svgp4.GoogleAPI.SVPoint.SVImage;

import java.util.ArrayList;

import static com.keldee.svgp4.FileSystem.RouteHolder.LOG;

public class Route extends RouteSettings {
    private ArrayList<SVImage> images;
    private ArrayList<GCoordinate> routeOverview;

    public Route(String name) {
        super(name);
        //Maybe????
//        images = new ArrayList<>();
    }

    public Route (RouteSettings rs) {
        super(rs);
    }

    public void setImages (ArrayList<SVImage> images) {
        this.images = images;
    }

    public void setRouteOverview(ArrayList<GCoordinate> routeOverview) {
        this.routeOverview = routeOverview;
    }

    public void normalizeRoute () {
        SVImage image;
        GCoordinate f;
        GCoordinate t;
        int heading;

        if (images.size() <= 1)
            return;

        for (int i = 0; i < images.size() - 1; i++) {
            image = images.get(i);
            f = (GCoordinate) image.getCoordinate();
            t = (GCoordinate) images.get(i + 1).getCoordinate();
            heading = (int) bearTowardPoint(f, t);
//            Log.d(LOG, "heading image:" + i + " to: " + heading);
            if (heading < 0)
                heading = 360 + heading;
            image.setHeading(Integer.toString(heading));
        }
//        images.get(images.size() - 1).setHeading(Integer.toString(heading));
    }

    private double bearTowardPoint (GCoordinate f, GCoordinate t) {
        double fLat = Math.toRadians(f.latitude);
        double tLat = Math.toRadians(t.latitude);
        double dy = Math.toRadians(t.longitude - f.longitude);

        return Math.toDegrees(Math.atan2( (Math.sin(dy) * Math.cos(tLat)), (Math.cos(fLat) * Math.sin(tLat) - Math.sin(fLat) * Math.cos(tLat) * Math.cos(dy))));
    }

    public ArrayList<SVImage> getImages() {
        return images;
    }

    public ArrayList<GCoordinate> getRouteOverview() {
        return routeOverview;
    }

    public void printImages () {
        for (SVImage img : images) {
            Log.d(LOG, img.makeHttpParamsString());
        }
    }
}