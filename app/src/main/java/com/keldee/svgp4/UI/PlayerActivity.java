package com.keldee.svgp4.UI;

import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.keldee.svgp4.App;
import com.keldee.svgp4.FileSystem.RouteHolder;
import com.keldee.svgp4.GoogleAPI.LoaderThreadCallback;
import com.keldee.svgp4.GoogleAPI.SVPoint.SVImage;
import com.keldee.svgp4.GoogleAPI.Service.ImageLoader.ImageLoader;
import com.keldee.svgp4.R;
import com.keldee.svgp4.Route.Route;
import com.keldee.svgp4.Route.RouteSettings;

import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {
    private String LOG;
    private App app;
    private ImageLoader imageLoader;
    private RouteHolder routeHolder;
    private Handler handler;
    private ImageView playerView;
    private String routeName;
    private Route route;
    private ArrayList<SVImage> images;
    private ArrayList<Bitmap> buffer;
    private int fps;
    private int spf;
    private final int maxBufferSize = 10;
    private int task = 0;
    private boolean complete = false;

    private Button restartButton;
    private Button backButton;

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOG, "Player: onStop()");
        if (task != 0) {
//            imageLoader.stopTask(task);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (task != 0) {
            imageLoader.stopTask(task);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
//        if (app == null)
//            start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        start();
    }

    private void start () {
        if (initPlayer()) {
            startLoading();
            play();
        }
        else
            finish();
    }

    private boolean initPlayer() {
        LOG = getResources().getString(R.string.DEBUG_LOG_NAME);
        routeName = ((RouteSettings) getIntent().getSerializableExtra("routeSettings")).name;
//        routeName = getIntent().getStringExtra("routeName");
        if (routeName == null) {
            Log.e(LOG, "cannot start PlayerActivity without route name given");
            finish();
            return false;
        }
        app = (App) getApplicationContext();
        playerView = findViewById(R.id.player_picture_holder);
        routeHolder = app.getRouteHolder();
        imageLoader = app.getImageLoader();
        handler = new Handler(getMainLooper());
        buffer = new ArrayList<>();
        route = routeHolder.editRoute(routeName);
        if (route == null) {
            Log.e(LOG, "RouteHolder returned null on route: " + routeName);
            return false;
        }
        images = route.getImages();
        fps = route.videoFPS;
        spf = (int)((1d/fps)*1000);

        return true;
    }

    private void startLoading () {
        task = imageLoader.addTask(images, new LoadCallback());
        imageLoader.runTask(task);
    }

    private void play () {
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int size = buffer.size();
                if (size > 1) {
                    buffer.remove(0);
                }
                if (size > 0)
                    show(buffer.get(0));
                if (complete) {
                    initAfterButtons();
                    return;
                }
                play();
            }
        }, spf);
    }

    private void initAfterButtons () {
        restartButton = findViewById(R.id.player_restart);
        backButton = findViewById(R.id.player_back);

        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recreate();
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        showAfterButtons();
    }

    private void showAfterButtons () {
        restartButton.setVisibility(View.VISIBLE);
    }

    private void sleep (int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Log.d(LOG, "what???", e);
        }
    }

    private void show (Bitmap bitmap) {
        //memory efficiency - the main feature of this app
        playerView.setImageBitmap(bitmap);
    }

    private class LoadCallback extends LoaderThreadCallback<Bitmap> {
        @Override
        public void onLoadComplete() {
            Log.d(LOG, "complete");
            complete = true;
        }

        @Override
        public void onLoad(Bitmap loadedObj) {
            int count = 0;
            while (buffer.size() > maxBufferSize) {
                if (count > 5) {
                    Log.d(LOG, "too long buffer fully loaded, stopping the loading");
                    imageLoader.stopTask(task);
                    return;
                }
                Log.d(LOG, "buffer not ready, pausing loading for " + (spf + 10) + " millis");
                sleep(spf + 10);
                count++;
            }
            buffer.add(loadedObj);
        }

        @Override
        public void onLoadError() {
            Log.d(LOG, "wtf????");
        }
    }
}