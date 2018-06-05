package com.keldee.svgp4;

import android.app.Application;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.graphics.Bitmap;

import com.keldee.svgp4.FileSystem.RouteHolder;
import com.keldee.svgp4.GoogleAPI.SVPoint.GCoordinate;
import com.keldee.svgp4.GoogleAPI.SVPoint.GPanoId;
import com.keldee.svgp4.GoogleAPI.SVPoint.SVImage;
import com.keldee.svgp4.GoogleAPI.SVPoint.SVSettings;
import com.keldee.svgp4.GoogleAPI.Service.ImageLoader.ImageLoader;
import com.keldee.svgp4.Route.Route;
import com.keldee.svgp4.Route.RouteSettings;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class App extends Application {
    private ImageLoader imageLoader;
    private RouteHolder routeHolder;

    private String defaultPitch;
    private String defaultHeading;
    private String defaultSize;

    @Override
    public void onCreate() {
        imageLoader = new ImageLoader(this);
        routeHolder = new RouteHolder(this);

        defaultPitch = getResources().getString(R.string.DEFAULT_GOOGLE_SV_PITCH);
        defaultHeading = getResources().getString(R.string.DEFAULT_GOOGLE_SV_HEADING);
        defaultSize = getResources().getString(R.string.DEFAULT_GOOGLE_SV_SIZE);

        super.onCreate();
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public RouteHolder getRouteHolder() {
        return routeHolder;
    }

    public Route generateSampleRoute () {
        SVSettings<GCoordinate, GPanoId> gs1 = new SVSettings<>
                (defaultPitch, defaultHeading, defaultSize, new GCoordinate(55.5655013,12.8917999));
        SVSettings<GCoordinate, GPanoId> gs2 = new SVSettings<>
                (defaultPitch, defaultHeading, defaultSize, new GCoordinate(55.5655220,12.8917107));
        SVSettings<GCoordinate, GPanoId> gs3 = new SVSettings<>
                (defaultPitch, defaultHeading, defaultSize, new GCoordinate(55.5655108,12.8916383));

        SVImage svi1 = new SVImage(gs1);
        SVImage svi2 = new SVImage(gs2);
        SVImage svi3 = new SVImage(gs3);

        ArrayList<SVImage> images = new ArrayList<>();
        images.add(svi1);
        images.add(svi2);
        images.add(svi3);
        RouteSettings rs = new RouteSettings("__test_route");
        Route route = new Route(rs);
        route.setImages(images);
//        route.importSettings(rs);
        route.setImages(images);

        return route;
    }

    public RouteSettings generateDefaultRouteSettings (String name) {
        return new RouteSettings(name);
    }

    public SVSettings<GCoordinate, GPanoId> generateDefaultSVSettings () {
        SVSettings<GCoordinate, GPanoId> svSettings = new SVSettings<>();
        svSettings.setPitch(defaultPitch);
        svSettings.setHeading(defaultHeading);
        svSettings.setSize(defaultSize);

        return svSettings;
    }

    public ArrayList<LatLng> getLatLng (ArrayList<GCoordinate> coordinates) {
        ArrayList<LatLng> res = new ArrayList<>();
        for (GCoordinate coordinate : coordinates) {
            res.add(getLatLng(coordinate));
        }

        return res;
    }

    public LatLng getLatLng (GCoordinate coordinate) {
        return new LatLng(coordinate.latitude, coordinate.longitude);
    }
}
