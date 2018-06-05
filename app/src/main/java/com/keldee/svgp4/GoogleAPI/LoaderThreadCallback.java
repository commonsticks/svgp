package com.keldee.svgp4.GoogleAPI;

/*public abstract class LoaderThreadCallback<T> {

    public abstract void onLoadComplete ();

    public abstract void onLoad(T loadedObj);

    public abstract void onLoadError ();
}*/

public interface LoaderThreadCallback<T> {

    void onLoadComplete ();

    void onLoad(T loadedObj);

    void onLoadError ();
}
