package com.keldee.svgp4.UI.RouteList;

import android.graphics.Bitmap;

class RouteListItem {
    private String routeName;
    private Bitmap routePreview;

    public RouteListItem(String routeName, Bitmap routePreview) {
        this.routeName = routeName;
        this.routePreview = routePreview;
    }

    public String getRouteName() {
        return routeName;
    }

    public Bitmap getRoutePreview() {
        return routePreview;
    }

    @Override
    public String toString() {
        return routeName + " : " + routePreview.toString();
    }
}
