package com.keldee.svgp4.UI;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SubMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.keldee.svgp4.App;
import com.keldee.svgp4.FileSystem.RouteHolder;
import com.keldee.svgp4.R;
import com.keldee.svgp4.Route.Route;
import com.keldee.svgp4.Route.RouteSettings;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {
    private MainActivity leKostil;
    private App app;
    private RouteHolder routeHolder;
    private ArrayList<String> routes;
    private ArrayList<MenuListItem> items;
    private String LOG;
    private Intent routePlayIntent;

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private CoordinatorLayout content;
    private NavigationView navigationView;
    private SubMenu routesMenu;
    private Menu menu;

    private GoogleMap map;
    private RouteEditFragment routeEditor;
    private MapController mapController;

    private int orientation;
    private int lastMenuPos = 0;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setDrawerOrientation(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        leKostil = this;
        app = (App) getApplication();
        LOG = getResources().getString(R.string.DEBUG_LOG_NAME);
        routePlayIntent = new Intent(this, PlayerActivity.class);
        routeHolder = app.getRouteHolder();
        if (!routeHolder.isEverythingAlright()) {
//            finish();
            return;
        }
        routes = routeHolder.getRouteNames();
        items = new ArrayList<>();

        content = findViewById(R.id.content_main);
        drawerLayout = findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                float contentOffset = drawerView.getWidth() * slideOffset;
                content.setTranslationX(contentOffset);
            }
        };

        initMap();
        initEditor();

        setDrawerOrientation(getResources().getConfiguration());
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        menu = navigationView.getMenu();
        routesMenu = menu.findItem(R.id.drawer_route_list).getSubMenu();
        loadMenu();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else if (routeEditor.isVisible()) {
            hideRouteEditor();
        }
        else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                finish();
                return false;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Bundle args;

//        if (orientation == Configuration.ORIENTATION_PORTRAIT)
        drawerLayout.closeDrawer(GravityCompat.START);

        switch (id) {
            case R.id.drawer_create_route:
                switchActionbar(new StopFragmentCallback());
                args = new Bundle();
                args.putBoolean("routeExists", false);
                routeEditor.setArguments(args);
                return true;
            case R.id.drawer_play_route:
                if (mapController.mapEmpty) {
                    Toast.makeText(getApplicationContext(), "Choose route to play or create", Toast.LENGTH_LONG).show();
                    return false;
                }
                routePlayIntent.putExtra("routeSettings", new RouteSettings(mapController.getCurrentRoute()));
                startActivity(routePlayIntent);
                return true;
            case R.id.drawer_edit_route:
                if (mapController.mapEmpty) {
                    Toast.makeText(getApplicationContext(), "Choose route to edit or create", Toast.LENGTH_LONG).show();
                    return false;
                }
                switchActionbar(new StopFragmentCallback());
                args = new Bundle();
                args.putBoolean("routeExists", true);
                args.putString("routeName", mapController.routeName);
                routeEditor.setArguments(args);
                return true;
        }

        for (MenuListItem menuListItem : items) {
            if (menuListItem.getItemId() == id) {
                hideRouteEditor();
                mapController.route(routeHolder.editRoute(menuListItem.getName()));
                return true;
            }
        }

        return true;
    }

    private void initMap() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.main_map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                mapController = new MapController(leKostil, map);
                if (!routeHolder.getRouteNames().isEmpty()) {
                    Route r = routeHolder.editRoute(routeHolder.getRouteNames().get(0));
                    mapController.route(r);
                    /*TextView subText = findViewById(R.id.nav_header_sub_text);
                    subText.setText(r.name);*/
                }
                else
                    Toast.makeText(getApplicationContext(), R.string.MAIN_TOAST_START_EMPTY, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initEditor() {
        routeEditor = (RouteEditFragment) getSupportFragmentManager().findFragmentById(R.id.main_editor);
        routeEditor.setReadyCallback(new RouteEditFragment.EditorCallback() {
            @Override
            public void onEditorReady() {
                routeEditor.setInvisible();
            }

            @Override
            public void onRouteChanged(RouteSettings settings) {
                Log.d(LOG, "onRouteChanged: " + settings.name);
                hideRouteEditor();
                loadMenu();
            }

            @Override
            public void onRouteCreated(RouteSettings settings) {
                Log.d(LOG, "onRouteCreated: " + settings.name);
                hideRouteEditor();
                mapController.route(settings);
                loadMenu();

            }
        });
    }

    private void hideRouteEditor () {
        if (routeEditor.isVisible()) {
            switchActionbarBack();
            routeEditor.setInvisible();
        }
    }

    private void switchActionbar (View.OnClickListener callback) {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        drawerToggle.setDrawerIndicatorEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerToggle.setToolbarNavigationClickListener(callback);
    }

    private void switchActionbarBack () {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerToggle.setToolbarNavigationClickListener(null);
    }

    private void setDrawerOrientation(Configuration configuration) {
//        TODO something with this
//        if (true)
//            return;
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            orientation = Configuration.ORIENTATION_LANDSCAPE;
//            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
            drawerLayout.setScrimColor(Color.TRANSPARENT);

        } else if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            orientation = Configuration.ORIENTATION_PORTRAIT;
//            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            drawerLayout.setScrimColor(0x99000000);
        }
    }

    private void loadMenu () {
        String name;

        routes = routeHolder.getRouteNames();

        while (lastMenuPos < routes.size()) {
            name = routes.get(lastMenuPos);
            addMenuRoute(name, lastMenuPos);
            lastMenuPos++;
        }
    }

    private void addMenuRoute (String name, int pos) {
        Random r = new Random();
        MenuItem item = routesMenu.add(R.id.drawer_route_list_group, r.nextInt(10000), pos, name);
        items.add(new MenuListItem(name, item.getItemId()));
    }

    private class MenuListItem {
        private String name;
        private int itemId;

        public MenuListItem(String name, int itemId) {
            this.name = name;
            this.itemId = itemId;
        }

        public String getName() {
            return name;
        }

        public int getItemId() {
            return itemId;
        }
    }

    private class StopFragmentCallback implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            hideRouteEditor();
        }
    }
}
