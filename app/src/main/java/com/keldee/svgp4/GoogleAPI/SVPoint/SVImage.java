package com.keldee.svgp4.GoogleAPI.SVPoint;

import android.graphics.Bitmap;
import com.keldee.svgp4.BasicSettings.Coordinate;

public class SVImage extends SVSettings {
    private transient Bitmap bitmap;

    public SVImage (SVSettings settings) {
        super(settings);
    }

    public String makeHttpParamsString() {
        return "pitch=" + pitch + "&" +
                "heading=" + heading + "&" +
                "size=" + size + "&" +
                (((Coordinate) coordinate).getCoordinateApiName()) + "=" + (coordinate.toString()) +
                (hasAltCoordinate() ? ("&" + ((Coordinate) coordinate).getCoordinateApiName()) + "=" +  _coordinate.toString() : "");
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
