package com.keldee.svgp4.Route;

import android.content.Context;
import android.util.Log;

import com.keldee.svgp4.App;
import com.keldee.svgp4.FileSystem.RouteHolder;
import com.keldee.svgp4.GoogleAPI.SVPoint.GCoordinate;
import com.keldee.svgp4.GoogleAPI.SVPoint.GPanoId;
import com.keldee.svgp4.GoogleAPI.SVPoint.SVImage;
import com.keldee.svgp4.GoogleAPI.SVPoint.SVSettings;
import com.keldee.svgp4.R;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class RouteBuilder {
    private Context context;
    private App app;
    private String routeName;
    private Route route;
    private ArrayList<SVImage> images;
    private ArrayList<LatLng> points;
    private ArrayList<GCoordinate> overview;
    private int uselessVariable = 0;

    private RouteHolder routeHolder;
    private String LOG;
    private boolean everythingIsAlright;

    public RouteBuilder (Context context, RouteSettings routeSettings) {
        this.context = context;
        this.app = (App) context.getApplicationContext();
        routeHolder = app.getRouteHolder();
        LOG = context.getResources().getString(R.string.DEBUG_LOG_NAME);
        everythingIsAlright = true;
        this.routeName = routeSettings.name;

        route = routeHolder.editRoute(routeName);
        if (route == null) {
            Log.e(LOG, "RouteBuilder: route:" + routeName + " doesn't exist, creating empty route instead");
            route = new Route(routeSettings);
            if(!routeHolder.addRoute(route))
                Log.wtf(LOG, "Unbelievable! Route \"" + routeName + "\" already exists!");
            images = new ArrayList<>();
            route.setImages(images);
            points = new ArrayList<>();
            overview = new ArrayList<>();
            everythingIsAlright = false;
        }
        else {
            images = route.getImages();
            if ((overview = route.getRouteOverview()) == null)
                overview = new ArrayList<>();

            points = getRouteLatLng();

            //this is a line that were breaking the app for weeks
            //I'll just leave it here
            //uselessVariable = (points.size() == 0) ? 0 : points.size() - 1;
        }
    }

    public void finish () {
        Log.d(LOG, "RouteBuilder: finish building route:" + routeName);
        for (int i = uselessVariable; i < points.size(); i++) {
            SVSettings<GCoordinate, GPanoId> svSettings = app.generateDefaultSVSettings();
            svSettings.setCoordinate(new GCoordinate(points.get(i).latitude, points.get(i).longitude));
            images.add(new SVImage(svSettings));
        }

        route.setImages(images);
        route.setRouteOverview(overview);
        route.normalizeRoute();
        routeHolder.saveRoute(routeName);
    }

    public void addPoint (LatLng point) {
        points.add(point);
    }

    public void addOverviewPoint (LatLng overviewPoint) {
        overview.add(new GCoordinate(overviewPoint.latitude, overviewPoint.longitude));
    }

    public void deletePoint (LatLng latLng) {
        points.remove(latLng);
    }

    public void clearPoints () {
        //TODO check safety of this call
        images.clear();
        points.clear();
    }

    public void clearOverview () {
        overview.clear();
    }

    public ArrayList<LatLng> getRouteLatLng() {
        if (isEmpty())
            return new ArrayList<>();

        ArrayList<LatLng> res = new ArrayList<>();
        for (SVImage image : images) {
            res.add(getLatLng((GCoordinate) image.getCoordinate()));
        }

        return res;
    }

    public ArrayList<LatLng> getRouteOverview() {
        if (route.getRouteOverview() != null)
            return getLatLng(route.getRouteOverview());
        return new ArrayList<>();
    }

    private ArrayList<LatLng> getLatLng (ArrayList<GCoordinate> coordinates) {
        ArrayList<LatLng> res = new ArrayList<>();
        for (GCoordinate coordinate : coordinates) {
            res.add(getLatLng(coordinate));
        }

        return res;
    }

    private LatLng getLatLng (GCoordinate coordinate) {
        return new LatLng(coordinate.latitude, coordinate.longitude);
    }

    private GCoordinate loadAltCoordinate () {
        //TODO load metadata for SVImage
        //or not
        return new GCoordinate(0, 0);
    }

    public boolean isEmpty () {
        return images.isEmpty();
    }

    public boolean isEverythingAlright() {
        return everythingIsAlright;
    }
}
