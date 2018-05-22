package com.keldee.svgp4.Route;

import com.keldee.svgp4.BasicSettings.Settings;

public class RouteSettings extends Settings {
    public String name;
    public int videoFPS = 3;

    public RouteSettings (String name) {
        super(true);
        this.name = name;
    }

    public RouteSettings (RouteSettings rs) {
        importSettings(rs);
    }

    //NEEDED TO ADD EVERY FIELD
    public RouteSettings(String name, int videoFPS) {
        this.name = name;
        this.videoFPS = videoFPS;
    }

    @Override
    protected <T> void resetFields (T settings) {
        RouteSettings r = (RouteSettings) settings;
        setNotEmpty();
        this.name = r.name;
        this.videoFPS = r.videoFPS;
    }
}
