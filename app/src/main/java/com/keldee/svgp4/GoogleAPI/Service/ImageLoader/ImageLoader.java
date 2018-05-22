package com.keldee.svgp4.GoogleAPI.Service.ImageLoader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Log;

import com.keldee.svgp4.GoogleAPI.LoaderThread;
import com.keldee.svgp4.GoogleAPI.LoaderThreadCallback;
import com.keldee.svgp4.GoogleAPI.SVPoint.SVImage;
import com.keldee.svgp4.R;
import java.util.ArrayList;
import java.util.HashMap;

public class ImageLoader {
    private static String LOG;
    //alt+enter4ever
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, ImageLoaderThread> tasks = new HashMap<>();
    private ArrayList<SVImage> images;
    private Resources resources;
    private String apiUrl;
    private String apiKey;

    public ImageLoader (Context context) {
        this.resources = context.getResources();
        LOG = resources.getString(R.string.DEBUG_LOG_NAME);
        apiUrl = resources.getString(R.string.GOOGLE_IMAGEAPI_URL);
        apiKey = resources.getString(R.string.google_maps_key);
    }

    public int addTask (ArrayList<SVImage> images, LoaderThreadCallback<Bitmap> callback) {
        ArrayList<String> links = new ArrayList<>();
        int hash;
        this.images = images;

        for (int i = 0; i < images.size(); i++)
            links.add(buildUrl(i));
        ImageLoaderThread thread = new ImageLoaderThread(links, callback);
        hash = thread.hashCode();
        tasks.put(thread.hashCode(), thread);
        return hash;
    }

    public boolean runTask (int hash) {
        if (tasks.containsKey(hash))
            tasks.get(hash).start();
        else
            return false;
        return true;
    }

    public boolean stopTask (int hash) {
        if (tasks.containsKey(hash)) {
            /*
            try {
                tasks.get(hash).join();
            } catch (InterruptedException e) {
                Log.e(LOG, "ImageLoader: couldn't stop task: ", e);
            }
            */
            tasks.get(hash).finish();
            tasks.remove(hash);
        }
        return true;
    }

    public void onStop () {
        //TODO maybe join() every running task?
    }

    private String buildUrl(int index) {
        return apiUrl +
                "?" +
                images.get(index).makeHttpParamsString() +
                "&" +
                "key=" + apiKey;
    }
}
