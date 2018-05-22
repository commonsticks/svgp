package com.keldee.svgp4.BasicSettings;

import java.io.Serializable;

public abstract class Coordinate implements Serializable {
    protected String coordinateApiName;

    public Coordinate(String coordinateApiName) {
        this.coordinateApiName = coordinateApiName;
    }

    public String getCoordinateApiName() {
        return coordinateApiName;
    }
}
