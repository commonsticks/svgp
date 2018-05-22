package com.keldee.svgp4.UI;

import android.content.Intent;
import android.icu.text.UnicodeSetSpanner;
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
import com.keldee.svgp4.Route.RouteSettings;

public class RouteCreateActivity extends AppCompatActivity {
    App app;
    Intent routeBuild;
    RouteHolder routeHolder;

    EditText routeName;
    EditText routeFps;
    Button createButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_create);

        app = (App) getApplicationContext();
        routeHolder = app.getRouteHolder();
        routeBuild = new Intent(this, RouteBuildActivity.class);

        routeName = findViewById(R.id.route_create_name);
        routeFps = findViewById(R.id.route_create_fps);
        createButton = findViewById(R.id.route_create_button);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RouteSettings settings;
                String rawName = routeName.getText().toString();
                String rawFps = routeFps.getText().toString();

                int fps;

                if (rawName.isEmpty()) {
                    toast(("Route name cannot be empty"));
                    return;
                }
                if (rawFps.isEmpty())
                    fps = 5;
                else {
                    try {
                        fps = Integer.parseInt(rawFps);
                    } catch (NumberFormatException e) {
                        toast(("Incorrect fps"));
                        return;
                    }
                }

                if (routeHolder.getRouteNames().contains(rawName)) {
                    toast(("Route " + rawName + " already exists"));
                }
                else {
                    settings = new RouteSettings(rawName, fps);
                    routeBuild.putExtra("routeName", rawName);
                    routeBuild.putExtra("routeSettings", settings);
                    startActivity(routeBuild);
                }
            }
        });
    }

    private void toast (String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void toast (String msg, int duration) {
        Toast.makeText(this, msg, duration).show();
    }
}
