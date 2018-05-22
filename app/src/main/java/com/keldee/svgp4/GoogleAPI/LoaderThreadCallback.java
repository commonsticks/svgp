package com.keldee.svgp4.GoogleAPI;

public abstract class LoaderThreadCallback<T> {

    public abstract void onLoadComplete ();

    public abstract void onLoad(T loadedObj);

    public abstract void onLoadError ();
}
