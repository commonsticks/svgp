package com.keldee.svgp4.UI;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.keldee.svgp4.GoogleAPI.LoaderThreadCallback;
import com.keldee.svgp4.GoogleAPI.Service.DirectionLoader.DirectionLoader;
import com.keldee.svgp4.R;
import com.keldee.svgp4.Route.RouteBuilder;
import com.keldee.svgp4.Route.RouteSettings;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Random;

public class RouteBuildActivity extends FragmentActivity implements OnMapReadyCallback {
    private DirectionLoader directionLoader;
    private String LOG;
    private String routeName;
    private RouteSettings startRouteSettings;
    private GoogleMap map;
    RouteBuilder routeBuilder;

    private LatLng[] rLocations;

    private LatLng startPoint;
    private LatLng endPoint;

    private ArrayList<LatLng> points;
    private ArrayList<LatLng> overview;
    private ArrayList<Marker> markers;

    private Polyline polyline;

    private Intent routePlayIntent;

    //building states
    private final int BUILD_SCRATCH = 0;
    private final int BUILD_EDIT = 1;
    private int state;

    //useless shit
    private final int OVERVIEW = 0;
    private final int ADD = 1;
    private final int EDIT = 2;
    private int editorState;
    private boolean reloaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_build);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.route_build_activity_map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        routePlayIntent = new Intent(this, PlayerActivity.class);
        directionLoader = new DirectionLoader(this);
        map = googleMap;
        LOG = getResources().getString(R.string.DEBUG_LOG_NAME);
        markers = new ArrayList<>();
        try {
            startRouteSettings = (RouteSettings) getIntent().getSerializableExtra("routeSettings");
        } catch (ClassCastException e) {
            Log.e(LOG, "cannot cast given serializable to RouteSettings");
            startRouteSettings = null;
        }

        if (startRouteSettings == null) {
            Log.e(LOG, "cannot start RouteBuildActivity without RouteSettings");
            finish();
            return;
        }

//        routeName = getIntent().getStringExtra("routeName");
        routeName = startRouteSettings.name;

        /*if (routeName == null) {
            Log.e(LOG, "cannot start RouteBuildActivity without route name");
            finish();
            return;
        }*/

        Log.d(LOG, "starting RouteBuildActivity with route:" + routeName);

        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        setCallbacks();

//        setEditorState(OVERVIEW);
        setEditorState(EDIT);

        routeBuilder = new RouteBuilder(this, startRouteSettings);

        if (!routeBuilder.isEverythingAlright() || routeBuilder.isEmpty())
            build(BUILD_SCRATCH);
        else
            build(BUILD_EDIT);
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
        rLocations = new LatLng[] {
                new LatLng(48.8687751,2.3109356),
                new LatLng(38.5509110,-106.2868251) ,
                //i don't really like Mexican views
                /*new LatLng(26.3744254,-103.9903647)*/ };
        Random r = new Random();
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(rLocations[r.nextInt(rLocations.length)], 15));
        Snackbar.make(findViewById(R.id.route_build_activity_map), R.string.ROUTE_BUILD_SNACKBAR_1, Snackbar.LENGTH_LONG).show();
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
        addMarker(points.get(0), "startPoint", "Start");
        addMarker(points.get(points.size() - 1), "endPoint", "Finish");
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
                Handler handler = new Handler(getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        build(BUILD_EDIT);
//                        finishBuild();
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
        Button reloadRouteButton = findViewById(R.id.reload_route_button);
        Button saveRouteButton = findViewById(R.id.save_route_button);
        Button playRouteButton = findViewById(R.id.play_route_button);

        reloadRouteButton.setOnClickListener(new OnReloadRouteButtonClickCallback());
        saveRouteButton.setOnClickListener(new OnSaveRouteButtonClickCallback());
        playRouteButton.setOnClickListener(new OnPlayRouteButtonClickCallback());

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
        PolylineOptions polylineOptions = new PolylineOptions().addAll(points).color(Color.BLUE).width(4);
        if (polyline != null)
            polyline.remove();
        polyline = map.addPolyline(polylineOptions);
    }


    private class OnMapClickCallback implements GoogleMap.OnMapClickListener {
        @Override
        public void onMapClick(LatLng latLng) {
            switch (state) {
                case BUILD_SCRATCH:
                    if (startPoint == null) {
                        startPoint = latLng;
                        addMarker(latLng, "startPoint");
                        Snackbar.make(findViewById(R.id.route_build_activity_map), R.string.ROUTE_BUILD_SNACKBAR_2, Snackbar.LENGTH_LONG).show();
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
        public void onMarkerDragEnd(Marker marker) {}
    }

    private class OnReloadRouteButtonClickCallback implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            loadDirections();
        }
    }

    private class OnSaveRouteButtonClickCallback implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            finishBuild();
        }
    }

    private class OnPlayRouteButtonClickCallback implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            routePlayIntent.putExtra("routeSettings", new RouteSettings(routeName));
            startActivity(routePlayIntent);
        }
    }

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