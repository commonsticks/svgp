package com.keldee.svgp4.UI;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.keldee.svgp4.App;
import com.keldee.svgp4.FileSystem.RouteHolder;
import com.keldee.svgp4.R;
import com.keldee.svgp4.Route.Route;
import com.keldee.svgp4.Route.RouteSettings;

/*
* If you found a bug, please, do not tell anyone about it, keep quiet ^_^
* */

public class RouteEditFragment extends Fragment {
    private App app;
    private static String LOG;
    private RouteHolder routeHolder;
    private Route route;

    private EditText routeName;
//    private EditText routeFps;
    private TextView routeFps;
    private Button button;

    private RouteSettings settings;
    private String rawName;
    private String rawFps;
    private int fps;
    private boolean routeExists;

    private boolean viewCreated;
    private EditorCallback callback;

    public RouteEditFragment() {}

    public void setReadyCallback (EditorCallback callback) {
        this.callback = callback;
        if (viewCreated)
            callback.onEditorReady();
    }

    public void setInvisible () {
        View view;
        if ((view = getView()) != null) {
            view.setVisibility(View.INVISIBLE);
            ActionBar bar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            bar.setTitle(R.string.app_name);
        }
    }

    public void setVisible () {
        View view;
        if ((view = getView()) != null) {
            view.setVisibility(View.VISIBLE);
            ActionBar bar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            bar.setTitle((rawName == null) ? "New route" : rawName);
        }
    }

    private void init () {
        Resources r = getResources();
        String s1 = r.getString(R.string.EDITOR_LIST_DIALOG_ITEM_0);
        String s2 = r.getString(R.string.EDITOR_LIST_DIALOG_ITEM_1);
        String s3 = r.getString(R.string.EDITOR_LIST_DIALOG_ITEM_2);
        String s4 = r.getString(R.string.EDITOR_LIST_DIALOG_ITEM_3);

        routeName = getView().findViewById(R.id.route_create_name);
//        routeFps = getView().findViewById(R.id.route_create_fps);
        routeFps = getView().findViewById(R.id.route_create_fps);
        button = getView().findViewById(R.id.route_create_button);

        routeFps.setText(R.string.EDITOR_LIST_DIALOG_ITEM_1);
        routeFps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpeedListDialogFragment dialog = SpeedListDialogFragment.newInstance(4);
                dialog.show(getActivity().getSupportFragmentManager(), "Choose speed");
                dialog.setCallback(new SpeedListDialogFragment.Listener() {
                    @Override
                    public void onSpeedClicked(int position) {
                        switch (position) {
                            case 0:
                                routeFps.setText(R.string.EDITOR_LIST_DIALOG_ITEM_0);
                                break;
                            case 1:
                                routeFps.setText(R.string.EDITOR_LIST_DIALOG_ITEM_1);
                                break;
                            case 2:
                                routeFps.setText(R.string.EDITOR_LIST_DIALOG_ITEM_2);
                                break;
                            case 3:
                                routeFps.setText(R.string.EDITOR_LIST_DIALOG_ITEM_3);
                                break;
                            default:
                                routeFps.setText("");
                        }
                    }
                });
            }
        });

//        ActionBar bar = ((AppCompatActivity) getActivity()).getSupportActionBar();
//        bar.setTitle((rawName == null) ? "New Route" : rawName);

        if (routeExists) {
            if (rawName == null) {
                Log.d(LOG, "yet another log, it's from RouteCreateFragment");
                return;
            }
            route = routeHolder.editRoute(rawName);
            fps = route.videoFPS;
            routeName.setText(rawName);
            if (fps == 1)
                rawFps = s1;
            else if (fps == 2)
                rawFps = s2;
            else if (fps == 8)
                rawFps = s3;
            else if (fps == 30)
                rawFps = s4;

            routeFps.setText(rawFps);
            button.setText(R.string.ROUTE_CREATE_BUTTON_SAVE);
        }
        else {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle((rawName == null) ? "New Route" : rawName);
            routeName.setText("");
            routeFps.setText(s3);
        }

        button.setOnClickListener(new ButtonCallback());
        setVisible();
    }



    @Override
    public void setArguments(@Nullable Bundle args) {
        super.setArguments(args);
        routeExists = args.getBoolean("routeExists", false);
        if (routeExists)
            rawName = args.getString("routeName");
        init();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (App) getContext().getApplicationContext();
        LOG = app.getResources().getString(R.string.DEBUG_LOG_NAME);
        routeHolder = app.getRouteHolder();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (callback != null)
            callback.onEditorReady();
        setInvisible();
        viewCreated = true;
        return inflater.inflate(R.layout.fragment_route_edit, container, false);
    }

    private void createRoute () {
        if (!routeHolder.getRouteNames().contains(rawName))
            settings = new RouteSettings(rawName, fps);
        else
            toast(("Route " + rawName + " already exists"));

    }

    private void saveRoute () {
        settings = new RouteSettings(rawName, fps);
        route.importSettings(settings);
        routeHolder.saveRoute(route.name);
    }

    private void toast (String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
    }

    private void toast (String msg, int duration) {
        Toast.makeText(getContext(), msg, duration).show();
    }

    private class ButtonCallback implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Resources r = getResources();
            rawName = routeName.getText().toString();
            rawFps = routeFps.getText().toString();
            String s1 = r.getString(R.string.EDITOR_LIST_DIALOG_ITEM_0);
            String s2 = r.getString(R.string.EDITOR_LIST_DIALOG_ITEM_1);
            String s3 = r.getString(R.string.EDITOR_LIST_DIALOG_ITEM_2);
            String s4 = r.getString(R.string.EDITOR_LIST_DIALOG_ITEM_3);

            if (rawName.isEmpty()) {
                toast(("Route name cannot be empty"));
                return;
            }

            if (rawFps.equals(s1)) {
                rawFps = "1";
                fps = 1;
            }
            else if (rawFps.equals(s2)) {
                rawFps = "2";
                fps = 2;
            }
            else if (rawFps.equals(s3)) {
                rawFps = "8";
                fps = 8;
            }
            else if (rawFps.equals(s4)) {
                rawFps = "30";
                fps = 30;
            }
            else {
                rawFps = "8";
                fps = 8;
                return;
            }

//            setInvisible();

            if (routeExists) {
                saveRoute();
                callback.onRouteChanged(settings);
            }
            else {
                createRoute();
                callback.onRouteCreated(settings);
            }
        }
    }

    public interface EditorCallback {
        void onEditorReady ();

        void onRouteChanged (RouteSettings settings);

        void onRouteCreated (RouteSettings settings);
    }
}
