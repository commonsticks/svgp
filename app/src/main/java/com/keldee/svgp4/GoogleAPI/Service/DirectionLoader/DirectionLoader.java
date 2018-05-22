package com.keldee.svgp4.GoogleAPI.Service.DirectionLoader;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.keldee.svgp4.GoogleAPI.LoaderThreadCallback;
import com.keldee.svgp4.R;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class DirectionLoader {
    private DirectionLoaderThread thread;
    private LatLng startPoint;
    private LatLng endPoint;
    private ArrayList<LatLng> waypoints;
    private String link;
    private LoaderThreadCallback<ArrayList<LatLng>> callback;
    private Resources resources;
    private String LOG;
    private String apiUrl;
    private String apiKey;
    private boolean ready = false;

    public DirectionLoader(Context context) {
        resources = context.getResources();
        LOG = resources.getString(R.string.DEBUG_LOG_NAME);
        apiUrl = resources.getString(R.string.GOOGLE_DIRECTIONAPI_URL);
        apiKey = resources.getString(R.string.google_maps_key);

    }

    public DirectionLoader init (final ArrayList<LatLng> points, LoaderThreadCallback<ArrayList<LatLng>> callback) {
        if (points.size() < 2)
            return null;
        this.callback = callback;
        startPoint = points.get(0);
        endPoint = points.get(points.size() - 1);
        //wasted an hour to find this shit
//        points.remove(0);
        waypoints = new ArrayList<>(points.subList(1, points.size()));
        link = buildLink();
        ready = true;

        return this;
    }

    public DirectionLoader start () {
        if (!ready)
            return null;

        ArrayList<String> links = new ArrayList<>();
        links.add(link);
        thread = new DirectionLoaderThread(links, callback);
        thread.start();
        ready = false;

        return this;
    }

    public boolean isFinished () {
        return thread.finished();
    }

    private String buildLink () {
        String waypointsString = "";
        for (LatLng point : this.waypoints) {
            waypointsString = waypointsString.concat("via:" + (point.latitude + "," + point.longitude) + "|");
        }
        String link = apiUrl +
                "?" +
                "origin=" + (startPoint.latitude + "," + startPoint.longitude) +
                "&" +
                "destination=" + (endPoint.latitude + "," + endPoint.longitude) +
                "&" +
                "waypoints=" + waypointsString +
                "&" +
                "key=" + apiKey;

        return link;
    }

    private void optimizeWaypoints (int level) {
        for (int i = 0; i < level; i++) {
            for (int j = 0; j < waypoints.size(); j++) {
                waypoints.remove(j);
            }
        }
    }
}
