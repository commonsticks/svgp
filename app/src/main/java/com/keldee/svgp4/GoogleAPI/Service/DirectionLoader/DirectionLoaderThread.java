package com.keldee.svgp4.GoogleAPI.Service.DirectionLoader;

import com.google.maps.android.PolyUtil;
import com.keldee.svgp4.GoogleAPI.LoaderThread;
import com.keldee.svgp4.GoogleAPI.LoaderThreadCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;

class DirectionLoaderThread extends LoaderThread<ArrayList<LatLng>> {

    DirectionLoaderThread(final ArrayList<String> links, LoaderThreadCallback<ArrayList<LatLng>> callback) {
        super(links, callback);
    }

    @Override
    protected ArrayList<LatLng> handle(InputStream inputStream) {
        Gson gson = new GsonBuilder().registerTypeAdapter(String.class, new PolylineDeserializerTypeAdapter<>()).create();
        String encodedPolyline = gson.fromJson(new InputStreamReader(inputStream), String.class);

        return new ArrayList<>(PolyUtil.decode(encodedPolyline));
    }

    private class PolylineDeserializerTypeAdapter<T> implements JsonDeserializer<T>
    {
        @Override
        public T deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) {
            //TODO check response status
            JsonElement content = je.getAsJsonObject().get("routes").getAsJsonArray().get(0).getAsJsonObject().get("overview_polyline").getAsJsonObject().get("points");
            return new Gson().fromJson(content, type);
        }
    }
}
