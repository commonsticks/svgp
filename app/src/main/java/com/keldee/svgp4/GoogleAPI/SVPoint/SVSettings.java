package com.keldee.svgp4.GoogleAPI.SVPoint;

import com.keldee.svgp4.BasicSettings.Settings;

public class SVSettings<T1, T2> extends Settings {
    protected String pitch;
    protected String heading;
    protected String size;
    protected T1 coordinate;
    protected T2 _coordinate;
    private boolean altCoordinate = false;

    public SVSettings () {
        super(true);
    }

    public SVSettings(String pitch, String heading, String size, T1 coordinate) {
        this.pitch = pitch;
        this.heading = heading;
        this.coordinate = coordinate;
        this.size = size;
    }

    public SVSettings (SVSettings<T1, T2> settings) {
        resetFields(settings);
    }

    public void setAltCoordinate (T2 _coordinate) {
        this._coordinate = _coordinate;
        altCoordinate = true;
    }

    //NEEDED TO ADD EVERY FIELD
    @Override
    protected <T> void resetFields (T settings) {
        SVSettings<T1, T2> r = (SVSettings) settings;
        this.pitch = r.pitch;
        this.heading = r.heading;
        this.size = r.size;
        this.coordinate = r.coordinate;
        if (r.hasAltCoordinate())
            setAltCoordinate(r._coordinate);
        setNotEmpty();
    }

    public boolean hasAltCoordinate () {
        return altCoordinate;
    }

    public String getPitch() {
        return pitch;
    }

    public String getHeading() {
        return heading;
    }

    public String getSize() {
        return size;
    }

    public T1 getCoordinate() {
        return coordinate;
    }

    public T2 getAltCoordinate() {
        return _coordinate;
    }

    public void setPitch(String pitch) {
        this.pitch = pitch;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setCoordinate(T1 coordinate) {
        this.coordinate = coordinate;
    }
}