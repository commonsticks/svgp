package com.keldee.svgp4.FileSystem;

import android.os.AsyncTask;
import android.util.Log;
import com.keldee.svgp4.Route.Route;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;

import static com.keldee.svgp4.FileSystem.RouteHolder.LOG;

public class SerializeRoute extends AsyncTask<Route, String, boolean[]> {
    private boolean inited = false;
    File destination = new File("");
    String routeExtensionName = "";

    @Override
    protected boolean[] doInBackground(Route[] routes) {
        boolean[] result = new boolean[routes.length];
        if (inited) {
            for (int i = 0; i < routes.length; i++)
                result[i] = serializeRoute(routes[i]);
        }
        else
            Arrays.fill(result, false);
        return result;
    }

    public SerializeRoute init (File destination, String extension) {
        this.destination = destination;
        this.routeExtensionName = extension;
        inited = true;

        return this;
    }

    private boolean serializeRoute (Route route) {
        File dest = new File(destination, route.name.concat(".").concat(routeExtensionName));
        try {
            Log.d(LOG, "serialize route \"" + route.name + "\" to " + dest);
//            if (dest.isFile()) {
//                Log.d(LOG, "file:" + dest.getAbsolutePath() + " already exists, overriding result:" + dest.delete());
//            }
            FileOutputStream fileOutputStream = new FileOutputStream(dest);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(route);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (FileNotFoundException e) {
            Log.e(LOG, "FileNotFoundException.", e);
        } catch (IOException e) {
            Log.e(LOG, "IOException.", e);
        } catch (ConcurrentModificationException e) {
            Log.e(LOG, "ConcurrentModificationException.", e);
        }
        return true;
    }
}
