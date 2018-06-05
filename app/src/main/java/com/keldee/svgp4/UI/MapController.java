package com.keldee.svgp4.UI;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.keldee.svgp4.GoogleAPI.LoaderThreadCallback;
import com.keldee.svgp4.GoogleAPI.Service.DirectionLoader.DirectionLoader;
import com.keldee.svgp4.R;
import com.keldee.svgp4.Route.RouteBuilder;
import com.keldee.svgp4.Route.RouteSettings;

import java.util.ArrayList;
import java.util.Random;

public class MapController {
    private Context context;
    private String LOG;
    private DirectionLoader directionLoader;
    private RouteBuilder routeBuilder;
    private GoogleMap map;

    private LatLng[] rLocations = new LatLng[] {
            new LatLng(48.8687751,2.3109356),
            new LatLng(38.5509110,-106.2868251),
            new LatLng(34.0389791,-118.2740209),
            new LatLng(55.7451833,37.5336330),
            new LatLng(25.2245205,-80.4308237)
    };

    public String routeName;
    private RouteSettings routeSettings;

    private LatLng startPoint;
    private LatLng endPoint;

    private ArrayList<LatLng> points;
    private ArrayList<LatLng> overview;
    private ArrayList<Marker> markers;

    private Polyline polyline;

    //building states
    private final int BUILD_SCRATCH = 0;
    private final int BUILD_EDIT = 1;
    private int state;

    //editor state will be used later, maybe
    private final int OVERVIEW = 0;
    private final int ADD = 1;
    private final int EDIT = 2;
    private int editorState;

    private boolean reloaded = false;
    public boolean mapEmpty = true;



    public MapController(Context context, GoogleMap map) {
        this.context = context;
        this.map = map;
        init();
    }

    private void init () {
        directionLoader = new DirectionLoader(context);
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        LOG = context.getResources().getString(R.string.DEBUG_LOG_NAME);
        markers = new ArrayList<>();
    }

    public void route (RouteSettings rs) {
        removeMarkers();
        resetPolyline();

        startPoint = endPoint = null;


        reloaded = false;
        routeSettings = rs;
        routeName = routeSettings.name;

        setCallbacks();
        setEditorState(EDIT);

        routeBuilder = new RouteBuilder(context, routeSettings);

        mapEmpty = false;

        if (!routeBuilder.isEverythingAlright() || routeBuilder.isEmpty())
            build(BUILD_SCRATCH);
        else
            build(BUILD_EDIT);

        Log.d(LOG, "MapController: editing: " + routeName);
    }

    public String getCurrentRoute () {
        return routeName;
    }

    private void setState (int state) {
        this.state = state;
    }

    public void setEditorState (int editorState) {
        this.editorState = editorState;
        switch (editorState) {
            case ADD:
            case EDIT:
//                setEditMarkers();
                break;
            case OVERVIEW:
//                removeMarkers(new MarkerTag("point"));
        }
    }

    private void build (int state) {
        setState(state);
        switch (state) {
            case BUILD_SCRATCH:
                scratchBuild();
                break;
            case BUILD_EDIT:
                editBuild();
                break;
        }
    }

    private void scratchBuild () {
        points = new ArrayList<>();
        overview = new ArrayList<>();
        Random r = new Random();
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(rLocations[r.nextInt(rLocations.length)], 15));
//        Snackbar.make(findViewById(R.id.route_build_activity_map_fragment), R.string.ROUTE_BUILD_SNACKBAR_1, Snackbar.LENGTH_LONG).show();
    }

    private void postScratchBuild () {
//        overview = generateMarkersOverview();
        loadDirections();
    }

    private void editBuild () {
        if (!reloaded) {
            points = routeBuilder.getRouteLatLng();
            overview = routeBuilder.getRouteOverview();
        }
        else
            overview = generateMarkersOverview();
        removeMarkers();
        startPoint = points.get(0);
        endPoint = points.get(points.size() - 1);
        addMarker(startPoint, "startPoint", "Start");
        addMarker(endPoint, "endPoint", "Finish");
        setMarkers();
        resetPolyline();
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(points.get(0), 15));
    }

    private void finishBuild () {
        if (state != BUILD_EDIT) {
            Log.e(LOG, "cannot finish building route because it's not ready");
            return;
        }

        routeBuilder.clearPoints();
        routeBuilder.clearOverview();
        for (LatLng point : points) {
            routeBuilder.addPoint(point);
        }
        for (LatLng waypoint : overview) {
            routeBuilder.addOverviewPoint(waypoint);
        }
        routeBuilder.finish();
    }

    private void loadDirections() {
        overview = generateMarkersOverview();
        if (overview.size() < 2)
            return;
        directionLoader.init(overview, new LoaderThreadCallback<ArrayList<LatLng>>() {
            @Override
            public void onLoadComplete() {
                Handler handler = new Handler(context.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        build(BUILD_EDIT);
                        finishBuild();
                    }
                });
            }

            @Override
            public void onLoad(ArrayList<LatLng> loadedObj) {
                points = loadedObj;
                reloaded = true;
            }

            @Override
            public void onLoadError() {}
        });
        directionLoader.start();
    }

    private void setCallbacks () {
        map.setOnMapClickListener(new OnMapClickCallback());
        map.setOnMarkerDragListener(new OnMarkerDragCallback());
    }

    private void setMarkers() {
        if (overview.size() > 1) {
            for (int i = 1; i < overview.size() - 1; i++) {
                addMarker(overview.get(i), "point");
            }
        }
    }
    private Marker addMarker (LatLng position, String tagName, String title) {
        Marker m = addMarker(position, tagName);
        m.setTitle(title);

        return m;
    }
    private Marker addMarker (LatLng position, String tagName) {
        Marker m = map.addMarker(new MarkerOptions().position(position));
        m.setDraggable(true);
        m.setTag(new MarkerTag(tagName, position));
        markers.add(m);

        return m;
    }
    private void removeMarkers () {
        for (Marker marker : markers) {
            marker.remove();
        }
        markers.clear();
    }
    private void removeMarkers (MarkerTag tag) {
        for (Marker marker : markers) {
            Object markerTag = marker.getTag();
            if (markerTag != null && markerTag.toString().equals(tag.toString())) {
                marker.remove();
                markers.remove(marker);
            }
        }
    }
    private ArrayList<LatLng> generateMarkersOverview () {
        ArrayList<LatLng> newOverview = new ArrayList<>();
        for (Marker marker : markers) {
            newOverview.add(marker.getPosition());
        }

        return newOverview;
    }

    private void resetPolyline () {
        if (polyline != null) {
            polyline.remove();
        }

        if (startPoint != null && endPoint != null) {
            PolylineOptions polylineOptions;
            polylineOptions = new PolylineOptions().addAll(points).color(Color.BLUE).width(4);
            polyline = map.addPolyline(polylineOptions);
        }
    }


    private class OnMapClickCallback implements GoogleMap.OnMapClickListener {
        @Override
        public void onMapClick(LatLng latLng) {
            switch (state) {
                case BUILD_SCRATCH:
                    if (startPoint == null) {
                        startPoint = latLng;
                        addMarker(latLng, "startPoint");
//                        Snackbar.make(findViewById(R.id.route_build_activity_map_fragment), R.string.ROUTE_BUILD_SNACKBAR_2, Snackbar.LENGTH_LONG).show();
                    }
                    else if (endPoint == null) {
                        addMarker(latLng, "endPoint");
                        endPoint = latLng;
                        postScratchBuild();
                    }
                    break;
                case BUILD_EDIT:
                    switch (editorState) {
                        case ADD:

                            break;
                        case EDIT:
                            break;
                    }
                    break;
            }
        }
    }
    private class OnMarkerClickCallback implements GoogleMap.OnMarkerClickListener {
        @Override
        public boolean onMarkerClick(Marker marker) {
            if (state != BUILD_EDIT)
                return false;

            switch (editorState) {
                case EDIT:
                    break;
                case ADD:
                    break;
            }

            return true;
        }
    }

    private class OnMarkerDragCallback implements GoogleMap.OnMarkerDragListener {
        @Override
        public void onMarkerDragStart(Marker marker) {}

        @Override
        public void onMarkerDrag(Marker marker) {}

        @Override
        public void onMarkerDragEnd(Marker marker) {
            loadDirections();
        }
    }

    @Deprecated
    private class MarkerTag {
        private String tag;
        private LatLng point;

        public MarkerTag(String tag, LatLng point) {
            this.tag = tag;
            this.point = point;
        }

        public MarkerTag(String tag) {
            this.tag = tag;
        }

        public LatLng getPoint() {
            return point;
        }

        @Override
        public String toString() {
            return tag;
        }
    }
}
