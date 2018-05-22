package com.keldee.svgp4.GoogleAPI.Service.ImageLoader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.keldee.svgp4.GoogleAPI.LoaderThread;
import com.keldee.svgp4.GoogleAPI.LoaderThreadCallback;

import java.io.InputStream;
import java.util.ArrayList;

class ImageLoaderThread extends LoaderThread<Bitmap> {
    ImageLoaderThread(ArrayList<String> links, LoaderThreadCallback<Bitmap> callback) {
        super(links, callback);
    }

    @Override
    protected Bitmap handle (InputStream inputStream) {
        return BitmapFactory.decodeStream(inputStream);
    }
}
