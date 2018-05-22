package com.keldee.svgp4.GoogleAPI;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public abstract class LoaderThread<T> extends Thread {
    private static String LOG = "SVDEBUG";
    private ArrayList<String> links;
    private LoaderThreadCallback<T> callback;
    private boolean taskComplete = false;

    public LoaderThread (final ArrayList<String> links, LoaderThreadCallback<T> callback) {
        this.links = links;
        this.callback = callback;
    }

    @Override
    public void run() {
        T obj;
        InputStream inputStream;
        for (int i = 0; i < links.size(); i++) {
            //I swear it's not my line! Someone else, lazy, trying to make my code worse and more unstable!
            if (taskComplete) return;
            inputStream = load(links.get(i));
            if (inputStream == null) {
                callback.onLoadError();
                continue;
            }
            obj = handle(inputStream);
            if (obj != null)
                callback.onLoad(obj);
            else
                callback.onLoadError();
        }
        callback.onLoadComplete();
        taskComplete = true;
    }

    protected abstract T handle (InputStream inputStream);

    private InputStream load (String link) {
        InputStream inputStream = null;
        Log.d(LOG, "loading " + link);
        try {
            inputStream = new URL(link).openConnection().getInputStream();
        } catch (MalformedURLException e) {
            Log.d(LOG, "MalformedURLException. ", e);
        } catch (IOException e) {
            //TODO ask unmindful user to check internet connection
            Log.d(LOG, "IOException. ", e);
        }

        return inputStream;
    }

    //god, please, bless this method to work
    public void finish () {
        taskComplete = true;
    }

    public boolean finished () {
//        return this.getState().equals(State.TERMINATED);
        return taskComplete;
    }
}
