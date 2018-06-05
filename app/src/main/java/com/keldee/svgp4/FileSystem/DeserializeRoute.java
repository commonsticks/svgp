package com.keldee.svgp4.FileSystem;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.keldee.svgp4.Route.Route;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import static com.keldee.svgp4.FileSystem.RouteHolder.LOG;

public class DeserializeRoute extends AsyncTask<File, String, ArrayList<Route>> {
    @Override
    protected ArrayList<Route> doInBackground(File... files) {
        ArrayList<Route> result = new ArrayList<>();
        for (File file : files) {
            result.add(deserializeRoute(file));
        }
        return result;
    }

    @Nullable
    private Route deserializeRoute(File file) {
        Route route;
        try {
            Log.d(LOG, "deserialize route from " + file.getAbsolutePath());
            FileInputStream fileInputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            route = (Route) objectInputStream.readObject();
            Log.d(LOG, "deserialized route \"" + route.name + "\" empty:" + route.isEmpty());

            return route;
        } catch (FileNotFoundException e) {
            Log.e(LOG, "FileNotFoundException. couldn't deserialize route", e);
//            return null;
        } catch (IOException e) {
            Log.e(LOG, "IOException. couldn't deserialize route", e);
//            return null;
        } catch (ClassNotFoundException e) {
            Log.e(LOG, "ClassNotFoundException. couldn't deserialize route", e);
//            return null;
        }
        return null;
    }
}