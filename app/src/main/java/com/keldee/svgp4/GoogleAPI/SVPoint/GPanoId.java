package com.keldee.svgp4.GoogleAPI.SVPoint;

import com.keldee.svgp4.BasicSettings.Coordinate;

public class GPanoId extends Coordinate {
    public String pano;
    private final static String name = "pano";

    public GPanoId (String pano) {
        super (name);
        this.pano = pano;
    }

    @Override
    public String toString() {
        return pano;
    }
}
