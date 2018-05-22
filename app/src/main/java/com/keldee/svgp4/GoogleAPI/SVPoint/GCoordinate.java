package com.keldee.svgp4.GoogleAPI.SVPoint;

import com.keldee.svgp4.BasicSettings.Coordinate;

public class GCoordinate extends Coordinate {
    public double latitude;
    public double longitude;
    private final static String name = "location";

    public GCoordinate (double latitude, double longitude) {
        super(name);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return latitude+","+longitude;
    }

    public String toString(boolean a) {
        return latitude+","+longitude;
    }
}
