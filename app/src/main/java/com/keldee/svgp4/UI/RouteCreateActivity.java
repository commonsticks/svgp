package com.keldee.svgp4.UI;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.keldee.svgp4.App;
import com.keldee.svgp4.FileSystem.RouteHolder;
import com.keldee.svgp4.R;
import com.keldee.svgp4.Route.Route;
import com.keldee.svgp4.Route.RouteSettings;
import com.keldee.svgp4.UI.RouteList.RouteListActivity;

/*
    Here's the best code, after RouteHolder, of course
*/

public class RouteCreateActivity extends AppCompatActivity {
    private static String LOG;
    private App app;
    private Intent routeBuildIntent;
    private Intent routeListIntent;
    private RouteHolder routeHolder;
    private Route route;

    private EditText routeName;
    private EditText routeFps;
    private Button button;

    private RouteSettings settings;
    private String rawName;
    private String rawFps;
    private int fps;
    private boolean routeExists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_create);

        app = (App) getApplicationContext();
        LOG = app.getResources().getString(R.string.DEBUG_LOG_NAME);
        routeHolder = app.getRouteHolder();
        routeBuildIntent = new Intent(this, RouteBuildActivity.class);
        routeListIntent = new Intent(this, RouteListActivity.class);

        routeName = findViewById(R.id.route_create_name);
        routeFps = findViewById(R.id.route_create_fps);
        button = findViewById(R.id.route_create_button);

        routeExists = getIntent().getBooleanExtra("routeExists", false);

        if (routeExists) {
            rawName = getIntent().getStringExtra("routeName");
            if (rawName == null) {
                Log.d(LOG, "yet another log, it's from RouteCreateActivity");
                finish();
            }
            route = routeHolder.editRoute(rawName);
            fps = route.videoFPS;
            routeName.setText(rawName);
            //TODO do what android studio want from you, another time
            routeFps.setText(Integer.toString(fps));
            button.setText(R.string.ROUTE_CREATE_BUTTON_SAVE);
        }

        button.setOnClickListener(new CreateButtonCallback());
    }

    private void createRoute () {
        if (routeHolder.getRouteNames().contains(rawName)) {
            toast(("Route " + rawName + " already exists"));
        }
        else {
            settings = new RouteSettings(rawName, fps);
            routeBuildIntent.putExtra("routeName", rawName);
            routeBuildIntent.putExtra("routeSettings", settings);
            startActivity(routeBuildIntent);
        }
    }

    private void saveRoute () {
        settings = new RouteSettings(rawName, fps);
        route.importSettings(settings);
        routeHolder.saveRoute(route.name);
        startActivity(routeListIntent);
    }

    private void toast (String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void toast (String msg, int duration) {
        Toast.makeText(this, msg, duration).show();
    }

    private class CreateButtonCallback implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            rawName = routeName.getText().toString();
            rawFps = routeFps.getText().toString();

            if (rawName.isEmpty()) {
                toast(("Route name cannot be empty"));
                return;
            }
            if (rawFps.isEmpty())
                fps = 5;
            else {
                try {
                    fps = Integer.parseInt(rawFps);
                    if (fps < 1) {
                        toast("minimum fps is 1");
                        return;
                    }
                } catch (NumberFormatException e) {
                    toast(("Incorrect fps"));
                    return;
                }
            }

            if (routeExists)
                saveRoute();
            else
                createRoute();
        }
    }
}
