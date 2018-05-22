package com.keldee.svgp4.UI.RouteList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.keldee.svgp4.R;

import java.util.ArrayList;

public class RouteListAdapter extends BaseAdapter {
    private ArrayList<RouteListItem> items;
    private LayoutInflater inflater;

    public RouteListAdapter (Context context, ArrayList<RouteListItem> items) {
        inflater = LayoutInflater.from(context);
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public RouteListItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = inflater.inflate(R.layout.item_adapter_route_list, parent, false);

        RouteListItem item = getItem(position);
        CardView cardView = convertView.findViewById(R.id.adapter_list_item_card_view);
        TextView routeName = cardView.findViewById(R.id.adapter_list_item_route_name);
        ImageView routePreview = cardView.findViewById(R.id.adapter_list_item_route_preview);

        routeName.setText(item.getRouteName());
        routeName.setTypeface(Typeface.DEFAULT_BOLD);
        routePreview.setImageBitmap(item.getRoutePreview());

        return convertView;
    }
}
