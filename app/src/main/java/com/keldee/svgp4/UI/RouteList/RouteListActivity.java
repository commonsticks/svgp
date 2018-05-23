package com.keldee.svgp4.UI.RouteList;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.keldee.svgp4.App;
import com.keldee.svgp4.FileSystem.RouteHolder;
import com.keldee.svgp4.GoogleAPI.LoaderThreadCallback;
import com.keldee.svgp4.GoogleAPI.SVPoint.SVImage;
import com.keldee.svgp4.GoogleAPI.Service.ImageLoader.ImageLoader;
import com.keldee.svgp4.R;
import com.keldee.svgp4.Route.RouteSettings;
import com.keldee.svgp4.UI.PlayerActivity;
import com.keldee.svgp4.UI.RouteBuildActivity;
import com.keldee.svgp4.UI.RouteCreateActivity;

import java.util.ArrayList;

public class RouteListActivity extends AppCompatActivity {
    private App app;
    private RouteHolder routeHolder;
    private ImageLoader imageLoader;
    private ListView listView;
    private RouteListAdapter adapter;
    private ArrayList<String> routes;
    private ArrayList<RouteListItem> items;
    private Bitmap imageNotAvailable;
    private String LOG;
    private Intent routeBuildIntent;
    private Intent routeCreateIntent;
    Intent routePlayIntent;

    private final int CHOOSE_EDIT = 1;
    private int chooseState;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.route_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.route_list_menu_edit:
                Log.d(LOG, "Edit route option selected");
                Toast.makeText(this, "Choose route to edit", Toast.LENGTH_LONG).show();
                chooseState = CHOOSE_EDIT;
                break;
            case R.id.route_list_menu_about:
                Log.d(LOG, "About page option selected");
                break;
            default:
                return true;
        }
        return false;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (adapter != null) {
            init();
            loadPreviews(routes);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(routeCreateIntent);
            }
        });

        init();
        loadPreviews(routes);
    }

    private void init () {
        app = (App) getApplication();
        LOG = getResources().getString(R.string.DEBUG_LOG_NAME);
        routeHolder = app.getRouteHolder();
        if (!routeHolder.isEverythingAlright()) {
            finish();
            return;
        }
        chooseState = 0;
        imageLoader = app.getImageLoader();
        routeBuildIntent = new Intent(this, RouteBuildActivity.class);
        routeCreateIntent = new Intent(this, RouteCreateActivity.class);
        routePlayIntent = new Intent(this, PlayerActivity.class);
        items = new ArrayList<>();
        imageNotAvailable = BitmapFactory.decodeResource(getResources(), R.drawable.image_not_available);

        listView = findViewById(R.id.routeList);
        routes = routeHolder.getRouteNames();
    }

    private void setAdapter () {
        adapter = new RouteListAdapter(this, items);
        listView.setAdapter(adapter);
        setCallbacks();
    }

    private void setCallbacks () {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Handler handler = new Handler(getMainLooper());
                final String clickedRouteName;
                RouteListItem clickedItem = (RouteListItem) adapterView.getItemAtPosition(i);
                clickedRouteName = clickedItem.getRouteName();
                Log.d(LOG, "clicked route name:" + clickedRouteName);
                switch (chooseState) {
                    case CHOOSE_EDIT:
                        routeCreateIntent.putExtra("routeExists", true);
                        routeCreateIntent.putExtra("routeName", clickedRouteName);
                        startActivity(routeCreateIntent);
                        break;
                    default:
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
//                        routeBuildIntent.putExtra("routeName", clickedRouteName);
                                routeBuildIntent.putExtra("routeSettings", new RouteSettings(clickedRouteName));
                                startActivity(routeBuildIntent);
                            }
                        });
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Handler handler = new Handler(getMainLooper());
                String clickedRouteName;
                RouteListItem clickedItem = (RouteListItem) parent.getItemAtPosition(position);
                clickedRouteName = clickedItem.getRouteName();
                Log.d(LOG, "long clicked route name:" + clickedRouteName);
                routePlayIntent.putExtra("routeSettings", new RouteSettings(clickedRouteName));
                startActivity(routePlayIntent);
                return true;
            }
        });
    }

    private void loadPreviews (final ArrayList<String> routes) {
        Handler handler = new Handler(getMainLooper());
        if (routes.isEmpty()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    setAdapter();
                }
            });
            return;
        }
        final int pos = routes.size() - 1;
        ArrayList<SVImage> routeImages = routeHolder.editRoute(routes.get(pos)).getImages();
        ArrayList<SVImage> firstImage;
        if (routeImages.isEmpty()) {
            items.add(new RouteListItem(routes.get(pos), imageNotAvailable));
            routes.remove(pos);
            loadPreviews(routes);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    setAdapter();
                }
            });
            return;
        }

        firstImage = new ArrayList<>(routeImages.subList(0, 1));
        routeHolder.cancelEditRoute(routes.get(pos));
        imageLoader.runTask(imageLoader.addTask(firstImage, new LoaderThreadCallback<Bitmap>() {
            @Override
            public void onLoadComplete() {
                routes.remove(pos);
                loadPreviews(routes);
            }

            @Override
            public void onLoad(Bitmap loadedObj) {
                items.add(new RouteListItem(routes.get(pos), loadedObj));
            }

            @Override
            public void onLoadError() {
                items.add(new RouteListItem(routes.get(pos), imageNotAvailable));
            }
        }));
    }
}